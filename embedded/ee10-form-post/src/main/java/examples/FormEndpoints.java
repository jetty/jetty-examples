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
import java.nio.file.Files;
import java.nio.file.Path;

import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.DefaultHandler;

public class FormEndpoints
{
    public static void main(String[] args) throws Exception
    {
        Server server = FormEndpoints.newServer(8080);
        server.start();
        server.join();
    }

    public static Server newServer(int port) throws IOException
    {
        Server server = new Server();

        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setFormEncodedMethods("POST");

        ServerConnector connector = new ServerConnector(server, new HttpConnectionFactory(httpConfig));
        connector.setPort(port);
        server.addConnector(connector);

        ServletContextHandler servletContextHandler = new ServletContextHandler();
        servletContextHandler.setContextPath("/");

        Path workDir = Files.createTempDirectory("multipart-work");
        MultipartConfigElement multipartConfig = new MultipartConfigElement(workDir.toString(), -1, -1, 500_000);

        servletContextHandler.addServlet(PostFormOnlyServlet.class, "/form/post-only")
            .getRegistration().setMultipartConfig(multipartConfig);
        servletContextHandler.addServlet(ConjoinedFormServlet.class, "/form/conjoined")
            .getRegistration().setMultipartConfig(multipartConfig);
        servletContextHandler.addServlet(ServiceFormServlet.class, "/form/service")
            .getRegistration().setMultipartConfig(multipartConfig);

        Handler.Sequence handlers = new Handler.Sequence();
        handlers.addHandler(servletContextHandler);
        handlers.addHandler(new DefaultHandler());

        server.setHandler(handlers);
        return server;
    }

    /**
     * Example of an HttpServlet that only deals with submitted forms.
     * Rejects all GET requests, and only allows POST based forms.
     */
    public static class PostFormOnlyServlet extends HttpServlet
    {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
        {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Not allowed to submit form via GET method");
        }

        @Override
        protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
        {
            String member = request.getParameter("Member");
            if (member == null)
            {
                response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "Form not valid");
                return;
            }

            response.setCharacterEncoding("utf-8");
            response.setContentType("text/plain");
            response.getWriter().printf("Got (PostOnly) Member [%s]%n", member);
        }
    }

    /**
     * Example of an old school (circa HTTP/1.0) HttpServlet that treats GET and POST the same.
     * But only support GET and POST for form submission.
     */
    public static class ConjoinedFormServlet extends HttpServlet
    {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
        {
            handleForm(req, resp);
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException
        {
            handleForm(req, resp);
        }

        protected void handleForm(HttpServletRequest request, HttpServletResponse response) throws IOException
        {
            String member = request.getParameter("Member");
            if (member == null)
            {
                response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "Form not valid");
                return;
            }
            response.setCharacterEncoding("utf-8");
            response.setContentType("text/plain");
            response.getWriter().printf("Got (Conjoined) Member [%s]%n", member);
        }
    }

    /**
     * Example of an HttpServlet anti-pattern that treats all HTTP Methods equally.
     */
    public static class ServiceFormServlet extends HttpServlet
    {
        @Override
        protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException
        {
            String member = request.getParameter("Member");
            if (member == null)
            {
                response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "Form not valid");
                return;
            }
            response.setCharacterEncoding("utf-8");
            response.setContentType("text/plain");
            response.getWriter().printf("Got (Service) Member [%s]%n", member);
        }
    }
}
