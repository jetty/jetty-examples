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
import java.io.StringWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MultiPart;
import org.eclipse.jetty.http.MultiPartFormData;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.handler.SecuredRedirectHandler;
import org.eclipse.jetty.util.BufferUtil;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceFactory;
import org.eclipse.jetty.util.resource.Resources;
import org.eclipse.jetty.util.ssl.SslContextFactory;

public class MultipartMimeUploadExample
{
    public static void main(String[] args) throws Exception
    {
        Server server = new Server();
        int httpPort = 8080;
        int httpsPort = 8443;
        ResourceFactory resourceFactory = ResourceFactory.of(server);

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
        sslContextFactory.setKeyStoreResource(findKeyStore(resourceFactory));
        sslContextFactory.setKeyStorePassword("OBF:1vny1zlo1x8e1vnw1vn61x8g1zlu1vn4");
        sslContextFactory.setKeyManagerPassword("OBF:1u2u1wml1z7s1z7a1wnl1u2g");
        sslContextFactory.setSniRequired(false);

        // Setup HTTPS Configuration
        HttpConfiguration httpsConf = new HttpConfiguration(httpConf);
        SecureRequestCustomizer secureRequestCustomizer = new SecureRequestCustomizer();
        secureRequestCustomizer.setSniRequired(false); // set to true for production
        secureRequestCustomizer.setSniHostCheck(false); // allow "localhost" to be used
        httpsConf.addCustomizer(secureRequestCustomizer); // adds ssl info to request object

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
        Handler.Sequence handlers = new Handler.Sequence();
        handlers.addHandler(new SecuredRedirectHandler());
        handlers.addHandler(newUploadHandler(outputDir));
        handlers.addHandler(newServletUploadHandler(multipartConfig, outputDir));
        handlers.addHandler(newResourceHandler(resourceFactory));
        handlers.addHandler(new DefaultHandler());
        server.setHandler(handlers);

        server.start();
        server.join();
    }

    private static Resource findKeyStore(ResourceFactory resourceFactory)
    {
        String resourceName = "ssl/keystore";
        Resource resource = resourceFactory.newClassLoaderResource(resourceName);
        if (!Resources.isReadableFile(resource))
        {
            throw new RuntimeException("Unable to read " + resourceName);
        }
        return resource;
    }

    private static Handler newUploadHandler(Path outputDir) throws IOException
    {
        return new UploadHandler("/handler/upload", outputDir);
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

    private static Handler newResourceHandler(ResourceFactory resourceFactory)
    {
        Resource baseResource = resourceFactory.newClassLoaderResource("static-upload/");
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
    }

    public static class UploadHandler extends Handler.Abstract
    {
        private final String contextPath;
        private final Path outputDir;

        public UploadHandler(String contextPath, Path outputDir) throws IOException
        {
            super();
            this.contextPath = contextPath;
            this.outputDir = outputDir.resolve("handler");
            ensureDirExists(this.outputDir);
        }

        @Override
        public boolean handle(Request request, Response response, Callback callback) throws Exception
        {
            if (!request.getHttpURI().getPath().startsWith(contextPath))
            {
                // not meant for us, skip it.
                return false;
            }

            if (!request.getMethod().equalsIgnoreCase("POST"))
            {
                // Not a POST method
                Response.writeError(request, response, callback, HttpStatus.METHOD_NOT_ALLOWED_405);
                return true;
            }

            String contentType = request.getHeaders().get(HttpHeader.CONTENT_TYPE);
            if (!HttpField.getValueParameters(contentType, null).equals("multipart/form-data"))
            {
                // Not a content-type supporting multi-part
                Response.writeError(request, response, callback, HttpStatus.NOT_ACCEPTABLE_406);
                return true;
            }

            String boundary = MultiPart.extractBoundary(contentType);
            MultiPartFormData.Parser formData = new MultiPartFormData.Parser(boundary);
            formData.setFilesDirectory(outputDir);

            try
            {
                String responseBody = process(formData.parse(request).join()); // May block waiting for multipart form data.
                response.setStatus(HttpStatus.OK_200);
                response.write(true, BufferUtil.toBuffer(responseBody), callback);
            }
            catch (Exception x)
            {
                Response.writeError(request, response, callback, x);
            }
            return true;
        }

        private String process(MultiPartFormData.Parts parts) throws IOException
        {
            StringWriter body = new StringWriter();
            PrintWriter out = new PrintWriter(body);

            for (MultiPart.Part part : parts)
            {
                out.printf("Got Part[%s].length=%s%n", part.getName(), part.getLength());
                HttpFields headers = part.getHeaders();
                for (HttpField field: headers)
                    out.printf("Got Part[%s].header[%s]=%s%n", part.getName(), field.getName(), field.getValue());
                out.printf("Got Part[%s].fileName=%s%n", part.getName(), part.getFileName());
                String filename = part.getFileName();
                if (StringUtil.isNotBlank(filename))
                {
                    // ensure we don't have "/" and ".." in the raw form.
                    filename = URLEncoder.encode(filename, StandardCharsets.UTF_8);

                    Path outputFile = outputDir.resolve(filename);
                    try (InputStream inputStream = Content.Source.asInputStream(part.getContentSource());
                         OutputStream outputStream = Files.newOutputStream(outputFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))
                    {
                        IO.copy(inputStream, outputStream);
                        out.printf("Saved Part[%s] to %s%n", part.getName(), outputFile);
                    }
                }
            }

            return body.toString();
        }
    }
}
