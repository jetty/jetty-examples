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

package examples.annotated;

import java.net.URL;
import java.util.Objects;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        Server server = new Server(8080);
        server.start();
        server.join();
    }

    public static Server newServer(int port)
    {
        Server server = new Server(port);

        ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletContextHandler.setContextPath("/");
        server.setHandler(servletContextHandler);

        // Add jakarta.websocket support
        JakartaWebSocketServletContainerInitializer.configure(servletContextHandler, (context, container) ->
        {
            // Add echo endpoint to server container
            container.addEndpoint(EchoSocket.class);
        });

        // Add default servlet (to serve the html/css/js)
        // Figure out where the static files are stored.
        URL urlStatics = Thread.currentThread().getContextClassLoader().getResource("echo-root/index.html");
        Objects.requireNonNull(urlStatics, "Unable to find index.html in classpath");
        String urlBase = urlStatics.toExternalForm().replaceFirst("/[^/]*$", "/");
        ServletHolder defHolder = new ServletHolder("default", new DefaultServlet());
        defHolder.setInitParameter("resourceBase", urlBase);
        defHolder.setInitParameter("dirAllowed", "true");
        servletContextHandler.addServlet(defHolder, "/");

        return server;
    }
}
