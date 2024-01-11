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
import java.net.HttpURLConnection;
import java.net.URI;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.ee10.servlet.DefaultServlet;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.util.IO;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Example of using {@link HttpServletResponse#setStatus(int)} instead of {@link HttpServletResponse#sendError(int)}
 * to handle HTTP error codes.
 */
public class ErrorStatusExample
{
    public static void main(String[] args) throws Exception
    {
        Server server = new Server(9090);
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        context.addServlet(RangeHandlingServlet.class, "/demo");
        context.addServlet(DefaultServlet.class, "/"); // handle static content and errors for this context
        Handler.Sequence handlers = new Handler.Sequence();
        handlers.addHandler(context);
        handlers.addHandler(new DefaultHandler()); // handle non-context errors
        server.setHandler(context);
        server.start();

        try
        {
            demonstrateErrorHandling(server.getURI().resolve("/"));
        }
        finally
        {
            server.stop();
        }
    }

    private static void demonstrateErrorHandling(URI serverBaseUri) throws IOException
    {
        HttpURLConnection http = (HttpURLConnection)serverBaseUri.resolve("/demo").toURL().openConnection();
        dumpRequestResponse(http);
        System.out.println();
        try (InputStream in = http.getInputStream())
        {
            System.out.println(IO.toString(in, UTF_8));
        }
    }

    private static void dumpRequestResponse(HttpURLConnection http) throws IOException
    {
        System.out.println();
        System.out.println("----");
        System.out.printf("%s %s HTTP/1.1%n", http.getRequestMethod(), http.getURL());
        System.out.println("----");
        System.out.printf("%s%n", http.getHeaderField(null));
        http.getHeaderFields().entrySet().stream()
            .filter(entry -> entry.getKey() != null)
            .forEach((entry) -> System.out.printf("%s: %s%n", entry.getKey(), http.getHeaderField(entry.getKey())));
    }

    public static class RangeHandlingServlet extends HttpServlet
    {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        {
            // To avoid triggering the standard Servlet Context Error Handling, we use .setStatus().
            // Using .sendError() will trigger Servlet Context Error Handling.
            resp.setStatus(416);
            resp.setHeader("Content-Range", "*/100");
            resp.addHeader("X-Example", "Yeah, your range isn't that great");
        }
    }
}
