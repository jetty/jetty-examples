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
import org.eclipse.jetty.logging.JettyLoggingServiceProvider;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.util.TypeUtil;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceFactory;

public class ServerMain
{
    public static void main(String[] args) throws Exception
    {
        int port = 8080;

        if (args.length >= 1)
        {
            port = Integer.parseInt(args[0]);
        }

        Server server = createServer(port);
        server.start();
        server.join();
    }

    public static Server createServer(int port) throws IOException
    {
        Path webapp = Path.of("target/webapps/ee10-webapp-using-logback.war").toAbsolutePath();

        if (!Files.isRegularFile(webapp))
            throw new FileNotFoundException("Unable to find required webapp: " + webapp);

        Server server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        server.addConnector(connector);

        ContextHandlerCollection contexts = new ContextHandlerCollection();
        server.setHandler(contexts);

        WebAppContext context = new WebAppContext();
        ResourceFactory resourceFactory = context.getResourceFactory();
        Resource warResource = resourceFactory.newResource(webapp);
        context.setWarResource(warResource);
        // Allow discovery of Jakarta Servlet API (needed for annotation scanning)
        context.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern", ".*/jakarta.servlet-api-[^/]*\\.jar$");

        // Important Note:
        // DO NOT use context.setParentLoaderPriority(true) - this will break WebApp ClassLoader Isolation

        /* Optional, not required to function properly.
         * Configure the HiddenClassMatcher of the WebApp to not see the Jetty server logging impl.
         * This is done to fix a warning seen.
         *   SLF4J(E): A service provider failed to instantiate:
         *   org.slf4j.spi.SLF4JServiceProvider: Provider org.eclipse.jetty.logging.JettyLoggingServiceProvider not found
         * Normal class loader isolation will prevent server classes from being seen.
         * However, resources are seen.  What's happening is that the slf4j impl on the webapp sees
         * the service files, but is unable to load the class.
         * The following two lines will ban not just the classes of jetty-slf4j-impl, but the entire
         * jar from being seen on the classloader.
         */
        URI serverLoggingLocation = TypeUtil.getLocationOfClass(JettyLoggingServiceProvider.class);
        context.getHiddenClassMatcher().add(serverLoggingLocation.toASCIIString());

        contexts.addHandler(context);
        return server;
    }
}
