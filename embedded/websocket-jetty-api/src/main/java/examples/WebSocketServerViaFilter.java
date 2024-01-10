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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.servlet.ServletException;

import org.eclipse.jetty.http.pathmap.ServletPathSpec;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.websocket.server.WebSocketUpgradeFilter;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;

public class WebSocketServerViaFilter
{
    public static class TimeSocketCreator implements WebSocketCreator
    {
        @Override
        public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp)
        {
            return new JettyTimeSocket();
        }
    }

    public static void main(String[] args) throws ServletException, URISyntaxException, MalformedURLException
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
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.setBaseResource(Resource.newResource(webRootUri));
        context.setWelcomeFiles(new String[]{"index.html"});
        server.setHandler(context);

        // Add the websocket filter
        WebSocketUpgradeFilter wsfilter = WebSocketUpgradeFilter.configure(context);
        // Configure websocket behavior
        wsfilter.getFactory().getPolicy().setIdleTimeout(5000);
        // Add websocket mapping
        wsfilter.addMapping(new ServletPathSpec("/time/"), new TimeSocketCreator());

        // Add time servlet
        context.addServlet(TimeServlet.class, "/time/");

        // Add default servlet
        ServletHolder holderDefault = new ServletHolder("default", DefaultServlet.class);
        holderDefault.setInitParameter("dirAllowed", "true");
        context.addServlet(holderDefault, "/");

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
