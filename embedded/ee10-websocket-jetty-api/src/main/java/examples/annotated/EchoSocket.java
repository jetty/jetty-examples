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

import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketOpen;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebSocket
public class EchoSocket
{
    private static final Logger LOG = LoggerFactory.getLogger(EchoSocket.class);
    private Session session;

    @OnWebSocketClose
    public void onWebSocketClose(int statusCode, String reason)
    {
        this.session = null;
        LOG.info("WebSocket Close: {} - {}", statusCode, reason);
    }

    @OnWebSocketOpen
    public void onWebSocketOpen(Session session)
    {
        this.session = session;
        LOG.info("WebSocket Open: {}", session);
        this.session.sendText("You are now connected to " + this.getClass().getName(), Callback.NOOP);
    }

    @OnWebSocketError
    public void onWebSocketError(Throwable cause)
    {
        LOG.warn("WebSocket Error", cause);
    }

    @OnWebSocketMessage
    public void onWebSocketText(String message)
    {
        LOG.info("Echoing back text message [{}]", message);
        this.session.sendText(message, Callback.NOOP);
    }
}
