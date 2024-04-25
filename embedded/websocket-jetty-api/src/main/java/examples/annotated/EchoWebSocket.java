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

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

@WebSocket
public class EchoWebSocket
{
    private static final Logger LOG = Log.getLogger(EchoWebSocket.class);
    private Session session;
    private RemoteEndpoint remote;

    @OnWebSocketClose
    public void onWebSocketClose(int statusCode, String reason)
    {
        this.session = null;
        this.remote = null;
        LOG.info("WebSocket Close: {} - {}",statusCode,reason);
    }

    @OnWebSocketConnect
    public void onWebSocketConnect(Session session)
    {
        this.session = session;
        this.remote = this.session.getRemote();
        LOG.info("WebSocket Connect: {}",session);
        this.remote.sendStringByFuture("You are now connected to " + this.getClass().getName());
    }

    @OnWebSocketError
    public void onWebSocketError(Throwable cause)
    {
        LOG.warn("WebSocket Error",cause);
    }

    @OnWebSocketMessage
    public void onWebSocketText(String message)
    {
        if (this.session != null && this.session.isOpen() && this.remote != null)
        {
            LOG.info("Echoing back text message [{}]",message);
            this.remote.sendStringByFuture(message);
        }
    }
}
