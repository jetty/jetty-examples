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

import examples.logging.Logging;
import org.eclipse.jetty.ee10.cdi.CdiDecoratingListener;
import org.eclipse.jetty.ee10.cdi.CdiServletContainerInitializer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.ee10.servlet.DefaultServlet;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.ee10.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer;
import org.eclipse.jetty.util.resource.ResourceFactory;
import org.slf4j.LoggerFactory;

public class ServerMain
{
    public static void main(String[] args)
    {
        Logging.config();
        try
        {
            new ServerMain().run(8080);
        }
        catch (Throwable t)
        {
            LoggerFactory.getLogger(ServerMain.class).warn("Failed to start Server", t);
        }
    }

    public void run(int port) throws Exception
    {
        Server server = new Server(port);

        URL webRootLocation = this.getClass().getResource("/static-root/index.html");
        if (webRootLocation == null)
        {
            throw new IllegalStateException("Unable to determine webroot URL location");
        }

        URI webRootUri = URI.create(webRootLocation.toURI().toASCIIString().replaceFirst("/index.html$", "/"));
        System.err.printf("Web Root URI: %s%n", webRootUri);

        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        Resource webResource = ResourceFactory.of(context).newResource(webRootUri);
        context.setBaseResource(webResource);
        context.setWelcomeFiles(new String[]{"index.html"});

        context.getMimeTypes().addMimeMapping("txt", "text/plain;charset=utf-8");

        // Enable Weld + CDI
        context.setInitParameter(CdiServletContainerInitializer.CDI_INTEGRATION_ATTRIBUTE, CdiDecoratingListener.MODE);
        context.addServletContainerInitializer(new CdiServletContainerInitializer());
        context.addServletContainerInitializer(new org.jboss.weld.environment.servlet.EnhancedListener());

        // Add WebSocket endpoints
        JakartaWebSocketServletContainerInitializer.configure(context, (servletContext, wsContainer) ->
            wsContainer.addEndpoint(TimeSocket.class));

        // Add Servlet endpoints
        context.addServlet(TimeServlet.class, "/time/");
        context.addServlet(DefaultServlet.class, "/");

        // Add to Server
        server.setHandler(context);

        // Start Server
        server.start();
        server.join();
    }
}
