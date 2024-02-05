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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.ee10.webapp.WebAppContext;
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

    public static Server newServer(int port)
    {
        Server server = new Server(port);

        WebAppContext context = new WebAppContext();
        Resource baseResource = findBaseResource(context);
        LOG.info("Using BaseResource: {}", baseResource);
        context.setBaseResource(baseResource);
        context.setContextPath("/");
        context.setWelcomeFiles(new String[]{"index.html", "welcome.html"});
        context.setParentLoaderPriority(true);
        server.setHandler(context);
        return server;
    }

    private static Resource findBaseResource(WebAppContext context)
    {
        ResourceFactory resourceFactory = ResourceFactory.of(context);

        try
        {
            // Look for resource in classpath (this is the best choice when working with a jar/war archive)
            ClassLoader classLoader = context.getClass().getClassLoader();
            URL webXml = classLoader.getResource("/WEB-INF/web.xml");
            if (webXml != null)
            {
                URI uri = webXml.toURI().resolve("../..").normalize();
                LOG.info("Found WebResourceBase (Using ClassLoader reference) {}", uri);
                return resourceFactory.newResource(uri);
            }
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException("Bad ClassPath reference for: WEB-INF", e);
        }

        // Look for resource in common file system paths
        try
        {
            Path pwd = Path.of(System.getProperty("user.dir")).toAbsolutePath();
            Path targetDir = pwd.resolve("target");
            if (Files.isDirectory(targetDir))
            {
                try (Stream<Path> listing = Files.list(targetDir))
                {
                    Path embeddedServletServerDir = listing
                        .filter(Files::isDirectory)
                        .filter((path) -> path.getFileName().toString().startsWith("embedded-servlet-server-"))
                        .findFirst()
                        .orElse(null);
                    if (embeddedServletServerDir != null)
                    {
                        LOG.info("Found WebResourceBase (Using /target/ Path) {}", embeddedServletServerDir);
                        return resourceFactory.newResource(embeddedServletServerDir);
                    }
                }
            }

            // Try the source path next
            Path srcWebapp = pwd.resolve("src/main/webapp/");
            if (Files.exists(srcWebapp))
            {
                LOG.info("WebResourceBase (Using /src/main/webapp/ Path) {}", srcWebapp);
                return resourceFactory.newResource(srcWebapp);
            }
        }
        catch (Throwable t)
        {
            throw new RuntimeException("Unable to find web resource in file system", t);
        }

        throw new RuntimeException("Unable to find web resource ref");
    }
}
