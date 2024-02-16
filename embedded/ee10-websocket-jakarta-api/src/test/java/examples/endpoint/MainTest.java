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

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import jakarta.websocket.ClientEndpointConfig;
import jakarta.websocket.CloseReason;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.MessageHandler;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.LifeCycle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MainTest
{
    private Server server;
    private WebSocketContainer wsClient;

    @BeforeEach
    public void startServerAndClient() throws Exception
    {
        server = Main.newServer(0);
        server.start();
        wsClient = ContainerProvider.getWebSocketContainer();
    }

    @AfterEach
    public void stopAll()
    {
        LifeCycle.stop(server);
    }

    @Test
    public void testEcho() throws Exception
    {
        EchoEndpoint echoSocket = new EchoEndpoint();
        ClientEndpointConfig endpointConfig = ClientEndpointConfig.Builder.create().build();
        URI wsURI = new URI("ws", server.getURI().getAuthority(), "/echo", null, null);
        try (Session session = wsClient.connectToServer(echoSocket, endpointConfig, wsURI))
        {
            session.getBasicRemote().sendText("Hello from " + this.getClass().getName());

            String msg = echoSocket.messageQueue.poll(5, TimeUnit.SECONDS);

            assertThat(msg, is("You are now connected to examples.endpoint.EchoSocket"));

            session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Goodbye"));
            assertTrue(echoSocket.closeLatch.await(5, TimeUnit.SECONDS));
        }
    }

    public static class EchoEndpoint extends Endpoint implements MessageHandler.Whole<String>
    {
        private static final Logger LOG = LoggerFactory.getLogger(EchoEndpoint.class);
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
