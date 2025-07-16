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
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jetty.ee10.webapp.WebAppContext;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.TypeUtil;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmbedMe
{
    private static final Logger LOG = LoggerFactory.getLogger(EmbedMe.class);

    public static void main(String[] args) throws Exception
    {
        int port = 8080;
        Server server = newServer(port);
        server.start();
        server.join();
    }

    public static Server newServer(int port) throws IOException
    {
        Server server = new Server(port);

        WebAppContext context = new WebAppContext();
        context.setContextPath("/");
        context.setWelcomeFiles(new String[]{"index.html", "welcome.html"});

        StringBuilder containerIncludeJarPattern = new StringBuilder();
        // Allow discovery of Jakarta Servlet API (needed for annotation scanning)
        containerIncludeJarPattern.append(".*/jakarta.servlet-api-[^/]*\\.jar$");

        ResourceFactory resourceFactory = ResourceFactory.of(context);
        URI location = TypeUtil.getLocationOfClass(TestServlet.class);
        if (location != null)
        {
            Path path = Path.of(location);

            // Allow discovery of application specific servlet annotations
            containerIncludeJarPattern.append("|.*/").append(path.getFileName());
            if (Files.isDirectory(path))
                containerIncludeJarPattern.append("/.*");
            containerIncludeJarPattern.append("$");

            Resource warResource = resourceFactory.newResource(location);
            LOG.info("Using Base Resource: {}", warResource);
            context.setBaseResource(warResource);
        }
        else
        {
            throw new FileNotFoundException("Unable to locate WAR Base");
        }

        context.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern", containerIncludeJarPattern.toString());
        context.setParentLoaderPriority(true);

        server.setHandler(context);
        // Uncomment to see dump of server configuration
        // server.setDumpAfterStart(true);
        return server;
    }
}
