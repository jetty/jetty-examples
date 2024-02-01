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

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.jetty.ee10.servlet.DefaultServlet;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.resource.ResourceFactory;

/**
 * Using a {@link ServletContextHandler} serve static file content from single location
 */
public class ServletFileServerSingleLocation
{
    public static void main(String[] args) throws Exception
    {
        URI webRootUri = findDefaultBaseResource();
        System.err.println("WebRoot is " + webRootUri);

        Server server = ServletFileServerSingleLocation.newServer(8080, webRootUri);
        server.start();
        server.join();
    }

    public static URI findDefaultBaseResource() throws URISyntaxException
    {
        // Figure out what path to serve content from
        ClassLoader cl = ServletFileServerMultipleLocations.class.getClassLoader();
        // We look for a file, as ClassLoader.getResource() is not
        // designed to look for directories (we resolve the directory later)
        URL f = cl.getResource("static-root/hello.html");
        if (f == null)
        {
            throw new RuntimeException("Unable to find resource directory");
        }

        // Resolve file to directory
        return f.toURI().resolve("./").normalize();
    }

    public static Server newServer(int port, URI resourcesRoot)
    {
        Server server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        server.addConnector(connector);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        ResourceFactory resourceFactory = ResourceFactory.of(context);
        context.setBaseResource(resourceFactory.newResource(resourcesRoot));
        server.setHandler(context);

        ServletHolder holderPwd = new ServletHolder("default", DefaultServlet.class);
        holderPwd.setInitParameter("dirAllowed", "true");
        context.addServlet(holderPwd, "/");

        return server;
    }
}
