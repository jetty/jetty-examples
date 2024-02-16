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

package examples.adapter;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.api.util.WSURI;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MainTest
{
    private Server server;
    private WebSocketClient wsClient;

    @BeforeEach
    public void startServerAndClient() throws Exception
    {
        server = Main.newServer(0);
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
    public void testEcho() throws Exception
    {
        ClientEchoSocket clientEchoSocket = new ClientEchoSocket();
        Future<Session> fut = wsClient.connect(clientEchoSocket, WSURI.toWebsocket(server.getURI().resolve("/echo")));
        Session session = fut.get(5, TimeUnit.SECONDS);
        session.getRemote().sendStringByFuture("Hello from " + this.getClass().getName());

        String msg = clientEchoSocket.messageQueue.poll(5, TimeUnit.SECONDS);

        assertThat(msg, is("You are now connected to examples.adapter.EchoSocket"));

        session.close(StatusCode.NORMAL, "Goodbye");
        assertTrue(clientEchoSocket.closeLatch.await(5, TimeUnit.SECONDS));
    }

    public static class ClientEchoSocket extends WebSocketAdapter
    {
        private static final Logger LOG = Log.getLogger(ClientEchoSocket.class);
        private final LinkedBlockingDeque<String> messageQueue = new LinkedBlockingDeque<>();
        private final CountDownLatch closeLatch = new CountDownLatch(1);

        public void onWebSocketClose(int statusCode, String reason)
        {
            super.onWebSocketClose(statusCode,reason);
            LOG.info("WebSocket Close: {} - {}",statusCode,reason);
            closeLatch.countDown();
        }

        public void onWebSocketConnect(Session session)
        {
            super.onWebSocketConnect(session);
            LOG.info("WebSocket Connect: {}",session);
        }

        public void onWebSocketError(Throwable cause)
        {
            LOG.warn("WebSocket Error",cause);
        }

        public void onWebSocketText(String message)
        {
            LOG.info("Text Message [{}]",message);
            messageQueue.offer(message);
        }
    }
}
