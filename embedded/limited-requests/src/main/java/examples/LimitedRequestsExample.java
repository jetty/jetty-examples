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

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class LimitedRequestsExample
{
    public static void main(String[] args) throws Exception
    {
        Server server = newServer(8080);
        server.start();
        server.join();
    }

    public static Server newServer(int port)
    {
        Server server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);

        LimitedRequestsListener limitedRequestsListener = new LimitedRequestsListener();
        limitedRequestsListener.setMaxRequests(5);
        connector.addBean(limitedRequestsListener);

        server.addConnector(connector);

        server.setHandler(new HelloHandler());
        return server;
    }

    private static class HelloHandler extends AbstractHandler
    {
        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
        {
            response.setStatus(200);
            response.setCharacterEncoding("utf-8");
            response.setContentType("text/plain");
            response.getWriter().println("Hello World");
            baseRequest.setHandled(true);
        }
    }
}
