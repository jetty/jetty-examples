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

package examples.browser;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.http.HttpClientTransportOverHTTP;
import org.eclipse.jetty.io.ClientConnector;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.javax.client.internal.JavaxWebSocketClientContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JakartaBrowserMainTest
{
    private Server server;
    private URI httpURI;
    private URI httpsURI;
    private WebSocketContainer wsClient;

    @BeforeEach
    public void startServerAndClient() throws Exception
    {
        server = JakartaBrowserMain.newServer(0, 0);
        server.start();

        URI serverURI = server.getURI();
        int httpPort = -1;
        int httpsPort = -1;

        for (Connector connector : server.getConnectors())
        {
            if (connector instanceof ServerConnector)
            {
                ServerConnector serverConnector = (ServerConnector)connector;
                SslConnectionFactory sslConnectionFactory = serverConnector.getConnectionFactory(SslConnectionFactory.class);
                if (sslConnectionFactory != null)
                {
                    httpsPort = serverConnector.getLocalPort();
                }
                else
                {
                    httpPort = serverConnector.getLocalPort();
                }
            }
        }

        httpURI = new URI("http", null, serverURI.getHost(), httpPort, "/", null, null);
        httpsURI = new URI("https", null, serverURI.getHost(), httpsPort, "/", null, null);

        SslContextFactory.Client ssl = new SslContextFactory.Client();
        ssl.setTrustAll(true);
        ClientConnector clientConnector = new ClientConnector();
        clientConnector.setSslContextFactory(ssl);

        HttpClient httpClient = new HttpClient(new HttpClientTransportOverHTTP(clientConnector));
        httpClient.start();
        wsClient = new JavaxWebSocketClientContainer(httpClient);
        LifeCycle.start(wsClient);
    }

    @AfterEach
    public void stopAll()
    {
        LifeCycle.stop(server);
        LifeCycle.stop(wsClient);
    }

    @ParameterizedTest
    @ValueSource(strings = {"http", "https"})
    public void testInfo(String scheme) throws Exception
    {
        ToolEndpoint echoSocket = new ToolEndpoint();
        URI wsURI;

        if (scheme.equals("http"))
            wsURI = new URI("ws", httpURI.getAuthority(), "/", null, null);
        else
            wsURI = new URI("wss", httpsURI.getAuthority(), "/", null, null);

        try (Session session = wsClient.connectToServer(echoSocket, wsURI))
        {
            session.getBasicRemote().sendText("info:");

            String msg = echoSocket.messageQueue.poll(5, TimeUnit.SECONDS);
            assertThat(msg, is("Using javax.websocket"));
            msg = echoSocket.messageQueue.poll(5, TimeUnit.SECONDS);
            assertThat(msg, startsWith("Client User-Agent: Jetty/"));
            msg = echoSocket.messageQueue.poll(5, TimeUnit.SECONDS);
            assertThat(msg, startsWith("Client requested no Sec-WebSocket-Extensions"));

            session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Goodbye"));
            assertTrue(echoSocket.closeLatch.await(5, TimeUnit.SECONDS));
        }
    }

    @ClientEndpoint(subprotocols = "tool")
    public static class ToolEndpoint
    {
        private static final Logger LOG = LoggerFactory.getLogger(ToolEndpoint.class);
        private final LinkedBlockingDeque<String> messageQueue = new LinkedBlockingDeque<>();
        private final CountDownLatch closeLatch = new CountDownLatch(1);

        @OnClose
        public void onClose(Session session, CloseReason closeReason)
        {
            LOG.info("WebSocket Close: {}", closeReason);
            closeLatch.countDown();
        }

        @OnError
        public void onError(Session session, Throwable cause)
        {
            LOG.warn("WebSocket Error", cause);
        }

        @OnOpen
        public void onOpen(Session session, EndpointConfig config)
        {
            LOG.info("WebSocket Open: {}", session);
        }

        @OnMessage
        public void onMessage(String message)
        {
            LOG.info("Text Message [{}]", message);
            messageQueue.offer(message);
        }
    }
}
