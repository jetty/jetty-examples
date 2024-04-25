//
// ========================================================================
// Copyright (c) 1995 Mort Bay Consulting Pty Ltd and others.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v. 2.0 which is available at
// https://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
// which is available at https://www.apache.org/licenses/LICENSE-2.0.
//
// SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
// ========================================================================
//

package examples.endpoint;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class EchoClient
{
    public static void main(String[] args) throws Exception
    {
        URI uri = URI.create("ws://localhost:8080/echo");

        if (args.length == 1)
            uri = new URI(args[0]);

        WebSocketContainer client = ContainerProvider.getWebSocketContainer();

        try
        {
            EchoClient.performEcho(client, uri);
        }
        finally
        {
            LifeCycle.stop(client);
        }
    }

    public static List<String> performEcho(WebSocketContainer client, URI uri) throws IOException, InterruptedException, DeploymentException
    {
        List<String> ret = new ArrayList<>();
        EchoClientEndpoint echoSocket = new EchoClientEndpoint();
        try (Session session = client.connectToServer(echoSocket, uri))
        {
            session.getBasicRemote().sendText("Hello from " + EchoClient.class.getName());

            String msg = echoSocket.messageQueue.poll(5, TimeUnit.SECONDS);
            ret.add(msg);
            session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Goodbye"));
            if (!echoSocket.closeLatch.await(5, TimeUnit.SECONDS))
                throw new IOException("Failed to receive WebSocket close");
        }
        return ret;
    }

    public static class EchoClientEndpoint extends Endpoint implements MessageHandler.Whole<String>
    {
        private static final Logger LOG = Log.getLogger(EchoClientEndpoint.class);
        private final LinkedBlockingDeque<String> messageQueue = new LinkedBlockingDeque<>();
        private final CountDownLatch closeLatch = new CountDownLatch(1);

        @Override
        public void onClose(Session session, CloseReason closeReason)
        {
            LOG.info("WebSocket Close: {}", closeReason);
            closeLatch.countDown();
        }

        @Override
        public void onError(Session session, Throwable cause)
        {
            LOG.warn("WebSocket Error", cause);
        }

        @Override
        public void onOpen(Session session, EndpointConfig config)
        {
            LOG.info("WebSocket Open: {}", session);
            session.addMessageHandler(this);
        }

        @Override
        public void onMessage(String message)
        {
            LOG.info("Text Message [{}]", message);
            messageQueue.offer(message);
        }
    }
}
