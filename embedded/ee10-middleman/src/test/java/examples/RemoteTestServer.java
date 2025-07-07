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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;

import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.UriCompliance;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.Callback;

public class RemoteTestServer implements AutoCloseable
{
    private final Server server;
    private final URI uri;

    public RemoteTestServer(int port) throws Exception
    {
        server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        connector.getContainedBeans(HttpConfiguration.class)
            .forEach((httpConfig) ->
                httpConfig.setUriCompliance(UriCompliance.UNSAFE));
        server.addConnector(connector);

        server.setHandler(new EchoHandler());
        server.start();
        uri = server.getURI().resolve("/");
    }

    @Override
    public void close() throws Exception
    {
        server.stop();
    }

    public URI getURI()
    {
        return uri;
    }

    private static class EchoHandler extends Handler.Abstract
    {
        @Override
        public boolean handle(Request request, Response response, Callback callback) throws Exception
        {
            response.setStatus(200);
            response.getHeaders().put(HttpHeader.CONTENT_TYPE, "text/plain;charset=utf-8");
            try (StringWriter stringWriter = new StringWriter();
                 PrintWriter out = new PrintWriter(stringWriter))
            {
                out.println("EchoHandler ---");
                out.printf("request.method=%s%n", request.getMethod());
                out.printf("request.uri=%s%n", request.getHttpURI().asString());
                for (HttpField field : request.getHeaders())
                {
                    out.printf("request.field[%s]=%s%n", field.getName(), String.join(", ", field.getValueList()));
                }
                Content.Sink.write(response, true, stringWriter.toString(), callback);
            }
            return true;
        }
    }
}
