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
import jakarta.servlet.DispatcherType;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ErrorPageErrorHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.util.StringUtil;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Demonstration on using the {@link ErrorPageErrorHandler} with a {@link ServletContextHandler}
 */
public class ErrorPageErrorHandlerExample
{
    public static void main(String[] args) throws Exception
    {
        Server server = new Server(9090);
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        context.addServlet(RangeHandlingServlet.class, "/demo");
        context.addServlet(ErrorsServlet.class, "/errors");
        context.addServlet(DefaultServlet.class, "/"); // handle static content and errors for this context

        ErrorPageErrorHandler errorPageErrorHandler = new ErrorPageErrorHandler();
        errorPageErrorHandler.addErrorPage(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE, "/errors");
        context.setErrorHandler(errorPageErrorHandler);

        HandlerList handlers = new HandlerList();
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
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
        {
            if (assertCanHandleRange(req, resp))
            {
                // process range
                // we would do something useful here.
            }
        }

        private boolean assertCanHandleRange(HttpServletRequest req, HttpServletResponse resp) throws IOException
        {
            // Normally we would test if range is possible here.
            // But we want to demonstrate error handling, so lets remember the range that failed
            // in the request attributes for later response.
            req.setAttribute("Error.Bad-Range", "bytes */100");
            // Trigger Servlet Error Handling
            resp.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
            return false; // for purposes of this example, we will return false to demonstrate error handling
        }
    }

    /**
     * The Servlet designated responsible for Error Handling.
     * This Servlet is designed to only respond during a Dispatch with {@link DispatcherType#ERROR}
     */
    public static class ErrorsServlet extends HttpServlet
    {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        {
            if (req.getDispatcherType() != DispatcherType.ERROR)
            {
                // direct access of errors servlet is a 404.
                // it should only be accessed by ERROR dispatch.
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            int statusCode = (int)req.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
            switch (statusCode)
            {
                case HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE:
                    resp.setStatus(statusCode);
                    String badRange = (String)req.getAttribute("Error.Bad-Range");
                    if (StringUtil.isNotBlank(badRange))
                    {
                        resp.setHeader("Content-Range", badRange);
                    }
                    resp.setHeader("X-Example", "Proof that Error Dispatch did what it's designed to do");
                    return;
                default:
                    resp.setStatus(statusCode);
                    return;
            }
        }
    }
}
