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

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.util.component.LifeCycle;

public class VirtualHostsExample
{
    public static void main(String[] args) throws Exception
    {
        VirtualHostsExample example = new VirtualHostsExample();
        try
        {
            example.startServer();
            example.testRequest("foo.company.com", "/hello");
            example.testRequest("bar.company.com", "/hello");
        }
        finally
        {
            example.stopServer();
        }
    }

    private Server server;

    private void stopServer()
    {
        LifeCycle.stop(server);
    }

    private void startServer() throws Exception
    {
        server = new Server(8080);
        HandlerCollection handlers = new HandlerCollection();
        server.setHandler(handlers);

        handlers.addHandler(createContext("/", "foo.company.com", HelloFooServlet.class));
        handlers.addHandler(createContext("/", "bar.company.com", HelloBarServlet.class));

        server.start();
    }

    private ContextHandler createContext(String contextPath, String host, Class<? extends HttpServlet> virtualHostSpecificServlet)
    {
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath(contextPath);

        context.addServlet(virtualHostSpecificServlet, "/hello");
        context.addServlet(DefaultServlet.class, "/");

        context.setVirtualHosts(new String[]{host});
        return context;
    }

    private void testRequest(String host, String path)
    {
        try (Socket client = new Socket("localhost", 8080))
        {
            System.out.printf("%n-- testRequest [%s] [%s] --%n", host, path);
            String req = String.format("GET %s HTTP/1.1\r\nHost: %s\r\nConnection: close\r\n\r\n", path, host);
            System.out.print(req);
            client.getOutputStream().write(req.getBytes(StandardCharsets.UTF_8));
            String response = IO.toString(client.getInputStream());
            System.out.print(response);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static class HelloFooServlet extends HttpServlet
    {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
        {
            resp.setCharacterEncoding("utf-8");
            resp.setContentType("text/plain");
            resp.getWriter().println("Greetings from [HelloFooServlet]");
        }
    }

    public static class HelloBarServlet extends HttpServlet
    {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
        {
            resp.setCharacterEncoding("utf-8");
            resp.setContentType("text/plain");
            resp.getWriter().println("Salutations from [HelloBarServlet]");
        }
    }
}
