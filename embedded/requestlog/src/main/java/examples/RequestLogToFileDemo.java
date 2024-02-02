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

import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.AsyncRequestLogWriter;
import org.eclipse.jetty.server.CustomRequestLog;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.Callback;

public class RequestLogToFileDemo
{
    public static void main(String[] args) throws Exception
    {
        Path logsDir = Path.of("target/logs");
        if (!Files.isDirectory(logsDir))
            Files.createDirectories(logsDir);

        Server server = RequestLogToFileDemo.newServer(8080, logsDir);
        server.start();
        server.join();
    }

    public static Server newServer(int port, Path logsDir)
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
                    Content.Sink.write(response, true, "Hello from " + RequestLogToFileDemo.class.getName(), callback);
                }
                return true;
            }
        });

        AsyncRequestLogWriter requestLogWriter = new AsyncRequestLogWriter();
        requestLogWriter.setAppend(true);
        requestLogWriter.setFilename(logsDir.resolve("request.log").toString());
        requestLogWriter.setRetainDays(1);
        RequestLog requestLog = new CustomRequestLog(requestLogWriter, CustomRequestLog.EXTENDED_NCSA_FORMAT);
        server.setRequestLog(requestLog);

        return server;
    }
}
