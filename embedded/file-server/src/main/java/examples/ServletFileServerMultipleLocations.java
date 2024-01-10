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
import java.nio.file.Paths;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;

/**
 * Using a {@link ServletContextHandler} serve static file content from multiple locations.
 *
 * <p>
 *     You have 2 url-patterns that static content is served from.
 *     <ul>
 *         <li>{@code /*} - the root url-pattern, serving content from {@code static-root/} in classloader</li>
 *         <li>{@code /alt/*} - the url-pattern serving content from {@code webapps/alt-root/} in file system</li>
 *     </ul>
 * </p>
 */
public class ServletFileServerMultipleLocations
{
    public static void main(String[] args) throws Exception
    {
        Server server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(8080);
        server.addConnector(connector);

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
        URI webRootUri = f.toURI().resolve("./").normalize();
        System.err.println("Main Base Resource is " + webRootUri);

        // Setup the basic application "context" for this application at "/"
        // This is also known as the handler tree (in jetty speak)
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.setBaseResource(Resource.newResource(webRootUri));
        context.setWelcomeFiles(new String[]{"index.html", "index.htm", "alt-index.html"});
        server.setHandler(context);

        // Find altPath
        Path altPath = Paths.get("webapps/alt-root").toRealPath();
        System.err.println("Alt Base Resource is " + altPath);

        // add special pathspec of "/alt/" content mapped to the altPath
        ServletHolder holderAlt = new ServletHolder("static-alt", DefaultServlet.class);
        holderAlt.setInitParameter("resourceBase", altPath.toUri().toASCIIString());
        holderAlt.setInitParameter("dirAllowed", "true");
        holderAlt.setInitParameter("pathInfoOnly", "true");
        context.addServlet(holderAlt, "/alt/*");

        // Lastly, the default servlet for root content (always needed, to satisfy servlet spec)
        // It is important that this is last.
        ServletHolder holderDef = new ServletHolder("default", DefaultServlet.class);
        holderDef.setInitParameter("dirAllowed", "true");
        context.addServlet(holderDef, "/");

        server.start();
        server.join();
    }
}
