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

package examples;

import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.jetty.server.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LimitedRequestsListener implements HttpChannel.Listener
{
    private static final Logger LOG = LoggerFactory.getLogger(LimitedRequestsListener.class);
    private int maxRequests = 10;

    public int getMaxRequests()
    {
        return maxRequests;
    }

    public void setMaxRequests(int maxRequests)
    {
        this.maxRequests = maxRequests;
    }

    @Override
    public void onResponseBegin(Request request)
    {
        long requests = request.getHttpChannel().getRequests();
        // After X responses, forcibly set connection close on response
        if (requests >= maxRequests)
        {
            request.getResponse().setHeader("Connection", "close");
            LOG.debug("Setting [Connection: Close] on Request #{} for {}", requests, request.getHttpChannel().getEndPoint().getTransport());
        }
    }
}