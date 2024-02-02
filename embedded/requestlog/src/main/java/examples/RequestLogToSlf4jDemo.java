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

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.CustomRequestLog;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Slf4jRequestLogWriter;
import org.eclipse.jetty.util.Callback;

public class RequestLogToSlf4jDemo
{
    public static void main(String[] args) throws Exception
    {
        Server server = RequestLogToSlf4jDemo.newServer(8080);
        server.start();
        server.join();
    }

    public static Server newServer(int port)
    {
        Server server = new Server(port);

        Handler.Sequence handlers = new Handler.Sequence();
        server.setHandler(handlers);
        handlers.addHandler(new Handler.Abstract()
        {
            @Override
            public boolean handle(Request request, Response response, Callback callback) throws Exception
            {
                if (request.getHttpURI().getPath().startsWith("/bogus"))
                    Response.writeError(request, response, callback, HttpStatus.NOT_FOUND_404);
                else
                {
                    response.getHeaders().put(HttpHeader.CONTENT_TYPE, "text/plain; charset=utf-8");
                    Content.Sink.write(response, true, "Hello from " + RequestLogToSlf4jDemo.class.getName(), callback);
                }
                return true;
            }
        });

        Slf4jRequestLogWriter requestLoggingWriter = new Slf4jRequestLogWriter();
        requestLoggingWriter.setLoggerName("examples.requests");
        RequestLog requestLog = new CustomRequestLog(requestLoggingWriter, CustomRequestLog.EXTENDED_NCSA_FORMAT);
        server.setRequestLog(requestLog);

        return server;
    }
}
