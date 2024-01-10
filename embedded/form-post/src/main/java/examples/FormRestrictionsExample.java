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
import java.net.URI;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.client.util.MultiPartContentProvider;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.Fields;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.component.LifeCycle;

public class FormRestrictionsExample
{
    private Server server;

    public static void main(String[] args) throws Exception
    {
        FormRestrictionsExample example = new FormRestrictionsExample();

        HttpClient client = new HttpClient();
        try
        {
            example.startServer(9090);
            URI serverURI = example.getServerURI();

            client.start();
            example.submitVariousForms(client, serverURI.resolve("/form/post-only"));
            example.submitVariousForms(client, serverURI.resolve("/form/conjoined"));
            example.submitVariousForms(client, serverURI.resolve("/form/service"));
        }
        finally
        {
            client.stop();
            example.stopServer();
        }
    }

    public URI getServerURI()
    {
        return server.getURI();
    }

    public void startServer(int port) throws Exception
    {
        server = new Server();

        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setFormEncodedMethods("POST");

        ServerConnector connector = new ServerConnector(server, new HttpConnectionFactory(httpConfig));
        connector.setPort(port);
        server.addConnector(connector);

        ServletContextHandler servletContextHandler = new ServletContextHandler();
        servletContextHandler.setContextPath("/");

        String tempDir = System.getProperty("java.io.tmpDir");
        MultipartConfigElement multipartConfig = new MultipartConfigElement(tempDir, -1, -1, 500_000);

        servletContextHandler.addServlet(PostFormOnlyServlet.class, "/form/post-only")
            .getRegistration().setMultipartConfig(multipartConfig);
        servletContextHandler.addServlet(ConjoinedFormServlet.class, "/form/conjoined")
            .getRegistration().setMultipartConfig(multipartConfig);
        servletContextHandler.addServlet(ServiceFormServlet.class, "/form/service")
            .getRegistration().setMultipartConfig(multipartConfig);

        HandlerList handlers = new HandlerList();
        handlers.addHandler(servletContextHandler);
        handlers.addHandler(new DefaultHandler());

        server.setHandler(handlers);
        server.start();
    }

    public void stopServer()
    {
        LifeCycle.stop(server);
    }

    public void submitVariousForms(HttpClient client, URI uri)
    {
        for (String httpMethod : Arrays.asList("GET", "POST", "PUT"))
        {
            submitForm(httpMethod + " with Query Params Only",
                client.newRequest(uri)
                    .method(httpMethod)
                    .path(uri.getRawPath() + "?UserName=Esteban+de+Dorantes"));

            Fields wwwForm = new Fields();
            wwwForm.add("UserName", "Álvar Núñez Cabeza de Vaca");

            submitForm(httpMethod + " with application/x-www-form-urlencoded",
                client.newRequest(uri)
                    .method(httpMethod)
                    .content(new FormContentProvider(wwwForm)));

            MultiPartContentProvider multipartForm = new MultiPartContentProvider();
            multipartForm.addFieldPart("UserName", new StringContentProvider("Andrés Dorantes de Carranza"), null);
            multipartForm.close();

            submitForm(httpMethod + " with multipart/form-data",
                client.newRequest(uri)
                    .method(httpMethod)
                    .content(multipartForm));
        }
    }

    private void submitForm(String description, Request request)
    {
        try
        {
            ContentResponse response = request.header("Accept", "text/plain").send();
            if (response.getStatus() == HttpStatus.OK_200)
            {
                System.out.printf("%-17s - %-44s -> OK: %s%n", request.getPath(), description, response.getContentAsString().trim());
            }
            else
            {
                String reason = response.getReason();
                String[] content = response.getContentAsString().split("\n");
                if (StringUtil.isNotBlank(content[0]))
                    reason = content[0];
                System.out.printf("%-17s - %-44s -> Status [%d]: %s%n", request.getPath(), description, response.getStatus(), reason);
            }
        }
        catch (InterruptedException | TimeoutException | ExecutionException e)
        {
            System.out.printf("%-17s - %-44s - ERROR: %s: %s%n", request.getPath(), description, e.getClass().getName(), e.getMessage());
        }
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
            String userName = request.getParameter("UserName");
            if (userName == null)
            {
                response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "Form not valid");
                return;
            }

            response.setCharacterEncoding("utf-8");
            response.setContentType("text/plain");
            response.getWriter().printf("Got (PostOnly) UserName [%s]%n", userName);
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
            String userName = request.getParameter("UserName");
            if (userName == null)
            {
                response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "Form not valid");
                return;
            }
            response.setCharacterEncoding("utf-8");
            response.setContentType("text/plain");
            response.getWriter().printf("Got (Conjoined) UserName [%s]%n", userName);
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
            String userName = request.getParameter("UserName");
            if (userName == null)
            {
                response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "Form not valid");
                return;
            }
            response.setCharacterEncoding("utf-8");
            response.setContentType("text/plain");
            response.getWriter().printf("Got (Service) UserName [%s]%n", userName);
        }
    }
}
