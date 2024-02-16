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

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServerEndpoint("/echo")
public class EchoSocket
{
    private static final Logger LOG = LoggerFactory.getLogger(EchoSocket.class);
    private Session session;
    private RemoteEndpoint.Async remote;

    @OnClose
    public void onWebSocketClose(CloseReason close)
    {
        this.session = null;
        this.remote = null;
        LOG.info("WebSocket Close: {} - {}", close.getCloseCode(), close.getReasonPhrase());
    }

    @OnOpen
    public void onWebSocketOpen(Session session)
    {
        this.session = session;
        this.remote = this.session.getAsyncRemote();
        LOG.info("WebSocket Connect: {}", session);
        this.remote.sendText("You are now connected to " + this.getClass().getName());
    }

    @OnError
    public void onWebSocketError(Throwable cause)
    {
        LOG.warn("WebSocket Error", cause);
    }

    @OnMessage
    public String onWebSocketText(String message)
    {
        LOG.info("Echoing back text message [{}]", message);
        // Using shortcut approach to sending messages.
        // You could use a void method and use remote.sendText()
        return message;
    }
}
