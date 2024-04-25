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

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;

public class EchoWebSocket implements WebSocketListener
{
    private static final Logger LOG = Log.getLogger(EchoWebSocket.class);
    private Session outbound;

    public void onWebSocketClose(int statusCode, String reason)
    {
        this.outbound = null;
        LOG.info("WebSocket Close: {} - {}",statusCode,reason);
    }

    public void onWebSocketConnect(Session session)
    {
        this.outbound = session;
        LOG.info("WebSocket Connect: {}",session);
        this.outbound.getRemote().sendString("You are now connected to " + this.getClass().getName(),null);
    }

    public void onWebSocketError(Throwable cause)
    {
        LOG.warn("WebSocket Error",cause);
    }

    public void onWebSocketText(String message)
    {
        if ((outbound != null) && (outbound.isOpen()))
        {
            LOG.info("Echoing back text message [{}]",message);
            outbound.getRemote().sendString(message,null);
        }
    }

    @Override
    public void onWebSocketBinary(byte[] arg0, int arg1, int arg2)
    {
        /* ignore */
    }
}
