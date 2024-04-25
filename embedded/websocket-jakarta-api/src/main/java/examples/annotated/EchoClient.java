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

package examples.annotated;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.eclipse.jetty.util.component.LifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    @ClientEndpoint
    public static class EchoClientEndpoint
    {
        private static final Logger LOG = LoggerFactory.getLogger(EchoClientEndpoint.class);
        private final LinkedBlockingDeque<String> messageQueue = new LinkedBlockingDeque<>();
        private final CountDownLatch closeLatch = new CountDownLatch(1);

        @OnClose
        public void onClose(CloseReason closeReason)
        {
            LOG.info("WebSocket Close: {}", closeReason);
            closeLatch.countDown();
        }

        @OnOpen
        public void onOpen(Session session)
        {
            LOG.info("WebSocket Open: {}", session);
        }

        @OnError
        public void onError(Throwable cause)
        {
            LOG.warn("WebSocket Error", cause);
        }

        @OnMessage
        public void onText(String message)
        {
            LOG.info("Text Message [{}]", message);
            messageQueue.offer(message);
        }
    }
}
