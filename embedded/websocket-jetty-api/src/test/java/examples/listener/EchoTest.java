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

package examples.listener;

import java.net.URI;
import java.util.List;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.websocket.api.util.WSURI;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public class EchoTest
{
    private Server server;
    private WebSocketClient wsClient;

    @BeforeEach
    public void startServerAndClient() throws Exception
    {
        server = EchoServer.newServer(0);
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
        URI uri = WSURI.toWebsocket(server.getURI().resolve("/echo"));
        List<String> msgs = EchoClient.performEcho(wsClient, uri);
        String[] expected = {
            "You are now connected to " + EchoWebSocket.class.getName()
        };
        assertThat(msgs, contains(expected));
    }
}
