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
import java.net.URL;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.ResourceFactory;

public class ResourceHandlerFromClasspath
{
    public static void main(String[] args) throws Exception
    {
        // Figure out what path to serve content from
        ClassLoader cl = ResourceHandlerFromClasspath.class.getClassLoader();
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

        Server server = ResourceHandlerFromClasspath.newServer(8080, webRootUri);
        server.start();
        server.join();
    }

    public static Server newServer(int port, URI resourcesRoot)
    {
        Server server = new Server(port);

        ResourceFactory resourceFactory = ResourceFactory.of(server);
        ResourceHandler handler = new ResourceHandler();
        handler.setBaseResource(resourceFactory.newResource(resourcesRoot));
        handler.setDirAllowed(true);

        server.setHandler(handler);
        return server;
    }
}
