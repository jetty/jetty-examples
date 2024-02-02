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
import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceCollection;

public class MetaInfResourceDemo
{
    public static void main(String[] args) throws Exception
    {
        Server server = MetaInfResourceDemo.newServer(8080);
        server.start();
        server.join();
    }

    public static Server newServer(int port) throws Exception
    {
        Server server = new Server(port);

        HandlerList handlers = new HandlerList();

        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");

        Resource manifestResources = findManifestResources(MetaInfResourceDemo.class.getClassLoader());
        context.setBaseResource(manifestResources);

        // Add something to serve the static files
        // It's named "default" to conform to servlet spec
        ServletHolder staticHolder = new ServletHolder("default", DefaultServlet.class);
        context.addServlet(staticHolder, "/");

        handlers.addHandler(context);
        handlers.addHandler(new DefaultHandler()); // always last handler

        server.setHandler(handlers);
        return server;
    }

    private static Resource findManifestResources(ClassLoader classLoader) throws IOException
    {
        List<URL> hits = Collections.list(classLoader.getResources("META-INF/resources"));
        int size = hits.size();
        Resource[] resources = new Resource[hits.size()];
        for (int i = 0; i < size; i++)
        {
            resources[i] = Resource.newResource(hits.get(i));
        }
        return new ResourceCollection(resources);
    }
}
