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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.PathResource;
import org.eclipse.jetty.util.resource.ResourceFactory;

public class ResourceHandlerFromFileSystem
{
    public static void main(String[] args) throws Exception
    {
        Server server = new Server(8080);

        Path webRootPath = Paths.get("webapps/alt-root/").toAbsolutePath().normalize();
        if (!Files.isDirectory(webRootPath))
        {
            System.err.println("ERROR: Unable to find " + webRootPath + ".");
            System.exit(-1);
        }
        System.err.println("WebRoot is " + webRootPath);

        ResourceFactory resourceFactory = ResourceFactory.of(server);
        ResourceHandler handler = new ResourceHandler();
        handler.setBaseResource(resourceFactory.newResource(webRootPath));
        handler.setDirAllowed(true);

        server.setHandler(handler);

        server.start();
        server.join();
    }
}
