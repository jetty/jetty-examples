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

package examples.time;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketOpen;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.api.util.WSURI;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WebSocketTimeServerTest
{
    private Server server;
    private WebSocketClient wsClient;

    @BeforeEach
    public void startServerAndClient() throws Exception
    {
        server = WebSocketTimeServer.newServer(0);
        server.start();
        wsClient = new WebSocketClient();
        wsClient.start();
    }

    @AfterEach
    public void stopAll()
    {
        LifeCycle.stop(server);
        LifeCycle.stop(wsClient);
    }

    @Test
    public void testTimeEndpoint() throws Exception
    {
        ClientEchoSocket clientEchoSocket = new ClientEchoSocket();
        Future<Session> fut = wsClient.connect(clientEchoSocket, WSURI.toWebsocket(server.getURI().resolve("/time/")));
        Session session = fut.get(5, TimeUnit.SECONDS);
        session.sendText("Hello from " + this.getClass().getName(), Callback.NOOP);

        String expectedYear = LocalDateTime.now().getYear() + "-";
        for (int i = 0; i < 5; i++)
        {
            String msg = clientEchoSocket.messageQueue.poll(5, TimeUnit.SECONDS);
            assertThat(msg, startsWith(expectedYear));
        }

        session.close(StatusCode.NORMAL, "Goodbye", Callback.NOOP);
        assertTrue(clientEchoSocket.closeLatch.await(5, TimeUnit.SECONDS));
    }

    @WebSocket
    public static class ClientEchoSocket
    {
        private static final Logger LOG = LoggerFactory.getLogger(ClientEchoSocket.class);
        private final LinkedBlockingDeque<String> messageQueue = new LinkedBlockingDeque<>();
        private final CountDownLatch closeLatch = new CountDownLatch(1);

        @OnWebSocketClose
        public void onClose(int statusCode, String reason)
        {
            LOG.info("WebSocket Close: {} - {}", statusCode, reason);
            closeLatch.countDown();
        }

        @OnWebSocketOpen
        public void onOpen(Session session)
        {
            LOG.info("WebSocket Open: {}", session);
        }

        @OnWebSocketError
        public void onError(Throwable cause)
        {
            LOG.warn("WebSocket Error", cause);
        }

        @OnWebSocketMessage
        public void onText(String message)
        {
            LOG.info("Text Message [{}]", message);
            messageQueue.offer(message);
        }
    }
}
