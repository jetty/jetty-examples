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
import java.time.Duration;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.websocket.server.JettyServerUpgradeRequest;
import org.eclipse.jetty.websocket.server.JettyServerUpgradeResponse;
import org.eclipse.jetty.websocket.server.JettyWebSocketCreator;
import org.eclipse.jetty.websocket.server.config.JettyWebSocketServletContainerInitializer;

public class WebSocketServerViaFilter
{
    public static class TimeSocketCreator implements JettyWebSocketCreator
    {
        @Override
        public Object createWebSocket(JettyServerUpgradeRequest jettyServerUpgradeRequest, JettyServerUpgradeResponse jettyServerUpgradeResponse)
        {
            return new JettyTimeSocket();
        }
    }

    public static void main(String[] args) throws URISyntaxException, MalformedURLException
    {
        Server server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(8080);
        server.addConnector(connector);

        // The location of the webapp base resource (for resources and static file serving)
        ClassLoader cl = WebSocketServerViaFilter.class.getClassLoader();
        // We look for a file, as ClassLoader.getResource() is not
        // designed to look for directories (we resolve the directory later)
        URL f = cl.getResource("static-root/index.html");
        if (f == null)
        {
            throw new RuntimeException("Unable to find resource directory");
        }
        URI webRootUri = f.toURI().resolve("./").normalize();
        System.err.println("WebRoot is " + webRootUri);

        // Setup the basic application "context" for this application at "/"
        // This is also known as the handler tree (in jetty speak)
        ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        contextHandler.setContextPath("/");
        contextHandler.setBaseResource(Resource.newResource(webRootUri));
        contextHandler.setWelcomeFiles(new String[]{"index.html"});
        server.setHandler(contextHandler);

        // Add the websocket filter
        JettyWebSocketServletContainerInitializer.configure(contextHandler, (context, configurator) ->
        {
            configurator.setIdleTimeout(Duration.ofMillis(5000));
            configurator.addMapping("/time/", new TimeSocketCreator());
        });

        // Add time servlet
        contextHandler.addServlet(TimeServlet.class, "/time/");

        // Add default servlet
        ServletHolder holderDefault = new ServletHolder("default", DefaultServlet.class);
        holderDefault.setInitParameter("dirAllowed", "true");
        contextHandler.addServlet(holderDefault, "/");

        try
        {
            server.start();
            server.join();
        }
        catch (Throwable t)
        {
            t.printStackTrace(System.err);
        }
    }
}
