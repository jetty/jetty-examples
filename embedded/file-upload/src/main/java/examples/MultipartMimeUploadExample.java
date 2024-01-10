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
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.handler.SecuredRedirectHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;

public class MultipartMimeUploadExample
{
    public static void main(String[] args) throws Exception
    {
        Server server = new Server();
        int httpPort = 8080;
        int httpsPort = 8443;

        // Setup HTTP Connector
        HttpConfiguration httpConf = new HttpConfiguration();
        httpConf.setSecurePort(httpsPort);
        httpConf.setSecureScheme("https");

        // Establish the HTTP ServerConnector
        ServerConnector httpConnector = new ServerConnector(server,
            new HttpConnectionFactory(httpConf));
        httpConnector.setPort(httpPort);
        server.addConnector(httpConnector);

        // Setup SSL
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStoreResource(findKeyStore());
        sslContextFactory.setKeyStorePassword("OBF:1vny1zlo1x8e1vnw1vn61x8g1zlu1vn4");
        sslContextFactory.setKeyManagerPassword("OBF:1u2u1wml1z7s1z7a1wnl1u2g");

        // Setup HTTPS Configuration
        HttpConfiguration httpsConf = new HttpConfiguration(httpConf);
        httpsConf.addCustomizer(new SecureRequestCustomizer()); // adds ssl info to request object

        // Establish the HTTPS ServerConnector
        ServerConnector httpsConnector = new ServerConnector(server,
            new SslConnectionFactory(sslContextFactory, "http/1.1"),
            new HttpConnectionFactory(httpsConf));
        httpsConnector.setPort(httpsPort);

        server.addConnector(httpsConnector);

        // Establish output directory
        Path outputDir = Paths.get("target", "upload-dir");
        outputDir = ensureDirExists(outputDir);

        // MultiPartConfig setup - to allow for ServletRequest.getParts() usage
        Path multipartTmpDir = Paths.get("target", "multipart-tmp");
        multipartTmpDir = ensureDirExists(multipartTmpDir);

        String location = multipartTmpDir.toString();
        long maxFileSize = 10 * 1024 * 1024; // 10 MB
        long maxRequestSize = 10 * 1024 * 1024; // 10 MB
        int fileSizeThreshold = 64 * 1024; // 64 KB
        MultipartConfigElement multipartConfig = new MultipartConfigElement(location, maxFileSize, maxRequestSize, fileSizeThreshold);

        // Add a Handlers for requests
        HandlerList handlers = new HandlerList();
        handlers.addHandler(new SecuredRedirectHandler());
        handlers.addHandler(newUploadHandler(multipartConfig, outputDir));
        handlers.addHandler(newServletUploadHandler(multipartConfig, outputDir));
        handlers.addHandler(newResourceHandler());
        handlers.addHandler(new DefaultHandler());
        server.setHandler(handlers);

        server.start();
        server.join();
    }

    private static URI findClassLoaderResource(String resourceName) throws URISyntaxException
    {
        ClassLoader cl = MultipartMimeUploadExample.class.getClassLoader();
        URL f = cl.getResource(resourceName);
        if (f == null)
        {
            throw new RuntimeException("Unable to find " + resourceName);
        }
        return f.toURI();
    }

    private static Resource findKeyStore() throws URISyntaxException, MalformedURLException
    {
        return Resource.newResource(findClassLoaderResource("ssl/keystore"));
    }

    private static Handler newUploadHandler(MultipartConfigElement multipartConfig, Path outputDir) throws IOException
    {
        return new UploadHandler("/handler/upload", multipartConfig, outputDir);
    }

    private static ServletContextHandler newServletUploadHandler(MultipartConfigElement multipartConfig, Path outputDir) throws IOException
    {
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/servlet");

        SaveUploadServlet saveUploadServlet = new SaveUploadServlet(outputDir);
        ServletHolder servletHolder = new ServletHolder(saveUploadServlet);
        servletHolder.getRegistration().setMultipartConfig(multipartConfig);

        context.addServlet(servletHolder, "/upload");

        return context;
    }

    private static Handler newResourceHandler() throws URISyntaxException, MalformedURLException
    {
        URI indexUri = findClassLoaderResource("static-upload/index.html");
        URI staticUploadBaseUri = indexUri.resolve("./").normalize();
        Resource baseResource = Resource.newResource(staticUploadBaseUri);

        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setBaseResource(baseResource);

        return resourceHandler;
    }

    private static Path ensureDirExists(Path path) throws IOException
    {
        Path dir = path.toAbsolutePath();

        if (!Files.exists(dir))
        {
            Files.createDirectories(dir);
        }

        return dir;
    }

    public static void processParts(HttpServletRequest request, HttpServletResponse response, Path outputDir) throws ServletException, IOException
    {
        response.setContentType("text/plain");
        response.setCharacterEncoding("utf-8");

        PrintWriter out = response.getWriter();

        for (Part part : request.getParts())
        {
            out.printf("Got Part[%s].size=%s%n", part.getName(), part.getSize());
            out.printf("Got Part[%s].contentType=%s%n", part.getName(), part.getContentType());
            out.printf("Got Part[%s].submittedFileName=%s%n", part.getName(), part.getSubmittedFileName());
            String filename = part.getSubmittedFileName();
            if (StringUtil.isNotBlank(filename))
            {
                // ensure we don't have "/" and ".." in the raw form.
                filename = URLEncoder.encode(filename, "utf-8");

                Path outputFile = outputDir.resolve(filename);
                try (InputStream inputStream = part.getInputStream();
                     OutputStream outputStream = Files.newOutputStream(outputFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))
                {
                    IO.copy(inputStream, outputStream);
                    out.printf("Saved Part[%s] to %s%n", part.getName(), outputFile);
                }
            }
        }
    }

    public static class SaveUploadServlet extends HttpServlet
    {
        private final Path outputDir;

        public SaveUploadServlet(Path outputDir) throws IOException
        {
            this.outputDir = outputDir.resolve("servlet");
            ensureDirExists(this.outputDir);
        }

        @Override
        protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
        {
            processParts(request, response, outputDir);
        }
    }

    public static class UploadHandler extends AbstractHandler
    {
        private final String contextPath;
        private final MultipartConfigElement multipartConfig;
        private final Path outputDir;

        public UploadHandler(String contextPath, MultipartConfigElement multipartConfig, Path outputDir) throws IOException
        {
            super();
            this.contextPath = contextPath;
            this.multipartConfig = multipartConfig;
            this.outputDir = outputDir.resolve("handler");
            ensureDirExists(this.outputDir);
        }

        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
        {
            if (!target.startsWith(contextPath))
            {
                // not meant for us, skip it.
                return;
            }

            if (!request.getMethod().equalsIgnoreCase("POST"))
            {
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                return;
            }

            // Ensure request knows about MultiPartConfigElement setup.
            request.setAttribute(Request.__MULTIPART_CONFIG_ELEMENT, multipartConfig);
            // Process the request
            processParts(request, response, outputDir);
            baseRequest.setHandled(true);
        }
    }
}
