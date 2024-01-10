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

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.resource.PathResource;

public class GzipRequestResponseExample
{
    public static void main(String[] args) throws Exception
    {
        Server server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(8080);
        server.addConnector(connector);

        GzipHandler gzip = new GzipHandler();
        gzip.setIncludedMethods("GET", "POST");
        gzip.setMinGzipSize(245); // Enable Request Decompression
        gzip.setIncludedMimeTypes("text/plain", "text/css", "text/html",
            "application/javascript");
        server.setHandler(gzip);

        ClassLoader cl = GzipRequestResponseExample.class.getClassLoader();
        // We look for a file, as ClassLoader.getResource() is not
        // designed to look for directories (we resolve the directory later)
        URL f = cl.getResource("static-root/hello.html");
        if (f == null)
        {
            throw new RuntimeException("Unable to find resource directory");
        }

        // Resolve file to directory
        URI webRootUri = f.toURI().resolve("./").normalize();
        System.err.println("WebRoot is " + webRootUri);

        ServletContextHandler context = new ServletContextHandler();
        gzip.setHandler(context);
        context.setContextPath("/");
        context.setBaseResource(new PathResource(webRootUri));
        context.setWelcomeFiles(new String[]{"index.html"});

        // Adding Servlets
        context.addServlet(DefaultServlet.class, "/"); // always last, always on "/"

        server.start();
        server.join();
    }
}
