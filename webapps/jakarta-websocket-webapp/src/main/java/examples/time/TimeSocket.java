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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnOpen;
import jakarta.websocket.RemoteEndpoint;
import jakarta.websocket.SendHandler;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/time/ws", subprotocols = {"time"})
public class TimeSocket implements Runnable
{
    private TimeZone timezone;
    private Session session;

    @OnOpen
    public void onOpen(Session session)
    {
        this.session = session;
        this.timezone = TimeZone.getTimeZone("UTC");
        new Thread(this).start();
    }

    @OnClose
    public void onClose(CloseReason close)
    {
        this.session = null;
    }

    @Override
    public void run()
    {
        RemoteEndpoint.Async remote = session.getAsyncRemote();
        SendHandler noop = result ->
        {
            // do nothing
        };

        while (this.session != null)
        {
            try
            {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
                dateFormat.setTimeZone(timezone);

                String timestamp = dateFormat.format(new Date());
                remote.sendText(timestamp, noop);
                TimeUnit.SECONDS.sleep(1);
            }
            catch (InterruptedException e)
            {
                System.err.println("Send of TEXT message interrupted");
                e.printStackTrace(System.err);
            }
        }
    }
}
