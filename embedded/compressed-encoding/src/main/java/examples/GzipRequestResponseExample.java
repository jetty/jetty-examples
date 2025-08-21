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

import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.jetty.compression.gzip.GzipCompression;
import org.eclipse.jetty.compression.server.CompressionConfig;
import org.eclipse.jetty.compression.server.CompressionHandler;
import org.eclipse.jetty.ee10.servlet.DefaultServlet;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceFactory;
import org.eclipse.jetty.util.resource.Resources;

public class GzipRequestResponseExample
{
    public static void main(String[] args) throws Exception
    {
        int port = 8080;
        Server server = newServer(port);

        server.start();
        server.join();
    }

    public static Server newServer(int port) throws IOException
    {
        Server server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        server.addConnector(connector);

        CompressionHandler compressionHandler = new CompressionHandler();
        compressionHandler.putCompression(new GzipCompression());

        CompressionConfig compressionConfig = CompressionConfig.builder()
            .compressIncludeMethod("GET")
            .compressIncludeMethod("POST")
            .compressIncludeMimeType("text/plain")
            .compressIncludeMimeType("text/css")
            .compressIncludeMimeType("text/html")
            .compressIncludeMethod("application/javascript")
            .build();

        compressionHandler.putConfiguration("/*", compressionConfig);
        server.setHandler(compressionHandler);

        ContextHandlerCollection contexts = new ContextHandlerCollection();
        compressionHandler.setHandler(contexts);

        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        Resource baseResource = ResourceFactory.of(context).newClassLoaderResource("static-root/");
        if (!Resources.isReadableDirectory(baseResource))
            throw new FileNotFoundException("Unable to find classloader resource static-root/");

        context.setBaseResource(baseResource);
        context.setWelcomeFiles(new String[]{"index.html"});

        // Adding Servlets
        ServletHolder defaultHolder = new ServletHolder("default", DefaultServlet.class);
        context.addServlet(defaultHolder, "/"); // always on default url-pattern of "/"

        contexts.addHandler(context);
        return server;
    }
}
