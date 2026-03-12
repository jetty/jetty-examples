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

import jakarta.websocket.ClientEndpointConfig;
import jakarta.websocket.CloseReason;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.MessageHandler;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;
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
        ClientEndpointConfig endpointConfig = ClientEndpointConfig.Builder.create().build();
        try (Session session = client.connectToServer(echoSocket, endpointConfig, uri))
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
        private static final Logger LOG = LoggerFactory.getLogger(EchoClientEndpoint.class);
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
