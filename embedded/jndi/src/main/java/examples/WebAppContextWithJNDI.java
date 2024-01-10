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
import java.io.InputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.resource.PathResource;
import org.eclipse.jetty.webapp.WebAppContext;

public class WebAppContextWithJNDI
{
    public static void main(String[] args) throws Exception
    {
        WebAppContextWithJNDI example = new WebAppContextWithJNDI();
        try
        {
            example.startServer(8080);
            URI serverURI = example.getServerURI();
            example.testRequest(serverURI.resolve("/jndi-dump"));
        }
        finally
        {
            example.stopServer();
        }
    }

    public static void dumpJndi(PrintStream out) throws NamingException
    {
        InitialContext ctx = new InitialContext();

        List<String> paths = Arrays.asList("val/foo", "entry/foo");
        List<String> prefixes = Arrays.asList("java:comp/env/", "");

        for (String prefix : prefixes)
        {
            for (String path : paths)
            {
                try
                {
                    Integer val = (Integer)ctx.lookup(prefix + path);
                    out.printf("lookup(\"%s%s\") = %s%n", prefix, path, val);
                }
                catch (NameNotFoundException e)
                {
                    out.printf("lookup(\"%s%s\") = NameNotFound: %s%n", prefix, path, e.getMessage());
                }
            }
        }
    }

    private Server server;

    public void startServer(int port) throws Exception
    {
        server = new Server();

        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        server.addConnector(connector);

        WebAppContext context = new WebAppContext();
        context.setContextPath("/");
        // This directory only has WEB-INF/web.xml
        context.setBaseResource(new PathResource(Paths.get("src/main/webroots/jndi-root")));
        context.addServlet(JndiDumpServlet.class, "/jndi-dump");

        new org.eclipse.jetty.plus.jndi.Resource(null, "val/foo", Integer.valueOf(770));
        new org.eclipse.jetty.plus.jndi.Resource(server, "val/foo", Integer.valueOf(880));
        new org.eclipse.jetty.plus.jndi.Resource(context, "val/foo", Integer.valueOf(990));

        new org.eclipse.jetty.plus.jndi.EnvEntry(null, "entry/foo", Integer.valueOf(440), false);
        new org.eclipse.jetty.plus.jndi.EnvEntry(server, "entry/foo", Integer.valueOf(550), false);
        new org.eclipse.jetty.plus.jndi.EnvEntry(context, "entry/foo", Integer.valueOf(660), false);

        server.setHandler(context);
        server.start();
    }

    public void stopServer()
    {
        LifeCycle.stop(server);
    }

    public URI getServerURI()
    {
        return server.getURI();
    }

    public void testRequest(URI uri) throws IOException, NamingException
    {
        System.out.println("-- Dump from WebApp Scope");
        HttpURLConnection http = (HttpURLConnection)uri.toURL().openConnection();
        try (InputStream in = http.getInputStream())
        {
            String body = IO.toString(in, StandardCharsets.UTF_8);
            System.out.println(body);
        }

        System.out.println("-- Dump from Test scope");
        dumpJndi(System.out);
    }

    public static class JndiDumpServlet extends HttpServlet
    {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
        {
            resp.setCharacterEncoding("utf-8");
            resp.setContentType("text/plain");
            PrintStream out = new PrintStream(resp.getOutputStream(), false, StandardCharsets.UTF_8.name());
            try
            {
                dumpJndi(out);
            }
            catch (NamingException e)
            {
                throw new ServletException(e);
            }
        }
    }
}
