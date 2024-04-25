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

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EchoServerEndpoint extends Endpoint implements MessageHandler.Whole<String>
{
    private static final Logger LOG = LoggerFactory.getLogger(EchoServerEndpoint.class);
    private Session session;
    private RemoteEndpoint.Async remote;

    @Override
    public void onClose(Session session, CloseReason close)
    {
        super.onClose(session, close);
        this.session = null;
        this.remote = null;
        LOG.info("WebSocket Close: {} - {}", close.getCloseCode(), close.getReasonPhrase());
    }

    @Override
    public void onOpen(Session session, EndpointConfig config)
    {
        this.session = session;
        this.remote = this.session.getAsyncRemote();
        LOG.info("WebSocket Open: {}", session);
        // attach echo message handler
        session.addMessageHandler(this);
        this.remote.sendText("You are now connected to " + this.getClass().getName());
    }

    @Override
    public void onError(Session session, Throwable cause)
    {
        super.onError(session, cause);
        LOG.warn("WebSocket Error", cause);
    }

    @Override
    public void onMessage(String message)
    {
        LOG.info("Echoing back text message [{}]", message);
        this.remote.sendText(message);
    }
}
