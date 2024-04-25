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

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.api.util.WSURI;
import org.eclipse.jetty.websocket.client.WebSocketClient;

public class EchoClient
{
    public static void main(String[] args) throws Exception
    {
        URI uri = URI.create("ws://localhost:8080/echo");

        if (args.length == 1)
            uri = WSURI.toWebsocket(new URI(args[0]));

        WebSocketClient client = new WebSocketClient();
        client.start();

        try
        {
            EchoClient.performEcho(client, uri);
        }
        finally
        {
            client.stop();
        }
    }

    public static List<String> performEcho(WebSocketClient client, URI uri) throws IOException, ExecutionException, InterruptedException, TimeoutException
    {
        List<String> ret = new ArrayList<>();

        ClientEchoWebSocket clientEchoSocket = new ClientEchoWebSocket();
        Future<Session> fut = client.connect(clientEchoSocket, uri);
        Session session = fut.get(5, TimeUnit.SECONDS);
        session.getRemote().sendStringByFuture("Hello from " + EchoClient.class.getName());

        String msg = clientEchoSocket.messageQueue.poll(5, TimeUnit.SECONDS);
        ret.add(msg);
        session.close(StatusCode.NORMAL, "Goodbye");
        if (!clientEchoSocket.closeLatch.await(5, TimeUnit.SECONDS))
            throw new IOException("Failed to receive WebSocket close");
        return ret;
    }

    public static class ClientEchoWebSocket extends WebSocketAdapter
    {
        private static final Logger LOG = Log.getLogger(ClientEchoWebSocket.class);
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
