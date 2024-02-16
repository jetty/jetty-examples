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

import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketOpen;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebSocket
public class TimeSocket implements Runnable
{
    private static final Logger LOG = LoggerFactory.getLogger(TimeSocket.class);
    private TimeZone timezone;
    private Session session;

    @OnWebSocketOpen
    public void onOpen(Session session)
    {
        this.session = session;
        this.timezone = TimeZone.getTimeZone("UTC");
        new Thread(this).start();
    }

    @OnWebSocketClose
    public void onClose(int closeCode, String closeReasonPhrase)
    {
        this.session = null;
    }

    @Override
    public void run()
    {
        while (this.session != null)
        {
            try
            {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
                dateFormat.setTimeZone(timezone);

                String timestamp = dateFormat.format(new Date());
                this.session.sendText(timestamp, Callback.NOOP);
                TimeUnit.SECONDS.sleep(1);
            }
            catch (InterruptedException e)
            {
                LOG.warn("Send of TEXT message interrupted", e);
            }
        }
    }
}
