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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

public class HelloCommonsLoggingHandler extends Handler.Abstract
{
    private static final Log LOG = LogFactory.getLog(HelloCommonsLoggingHandler.class);
    private final String msg;

    public HelloCommonsLoggingHandler(String msg)
    {
        this.msg  = msg;
    }

    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception
    {
        LOG.info(String.format(
            "Got request from %s for %s",
            Request.getRemoteAddr(request), request.getHttpURI().toString()));
        response.getHeaders().put(HttpHeader.CONTENT_TYPE, "text/plain; charset=utf-8");
        Content.Sink.write(response, true, String.format("%s%n", msg), callback);
        return true;
    }
}
