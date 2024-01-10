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

import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.javax.server.config.JavaxWebSocketServletContainerInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Set up a JakartaEE WebSocket Server that can be used by a browser
 */
public class JakartaBrowserMain
{
    private static final Logger LOG = LoggerFactory.getLogger(JakartaBrowserMain.class);

    public static void main(String[] args)
    {
        int port = 8080;
        int sslPort = 8443;

        for (int i = 0; i < args.length; i++)
        {
            String a = args[i];
            if ("-p".equals(a) || "--port".equals(a))
            {
                port = Integer.parseInt(args[++i]);
            }
            if ("-ssl".equals(a))
            {
                sslPort = Integer.parseInt(args[++i]);
            }
        }

        try
        {
            JakartaBrowserMain tool = new JakartaBrowserMain();
            tool.setupServer(port, sslPort);
            tool.runForever();
        }
        catch (Throwable t)
        {
            LOG.warn("Failed to start " + JakartaBrowserMain.class.getName(), t);
        }
    }

    private Server server;

    private void runForever() throws Exception
    {
        server.start();
        server.dumpStdErr();
        server.join();
    }

    private void setupServer(int port, int sslPort) throws MalformedURLException, URISyntaxException
    {
        server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        server.addConnector(connector);

        // Setup SSL
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStoreResource(findKeyStore());
        sslContextFactory.setKeyStorePassword("OBF:1vny1zlo1x8e1vnw1vn61x8g1zlu1vn4");
        sslContextFactory.setKeyManagerPassword("OBF:1u2u1wml1z7s1z7a1wnl1u2g");

        // Setup HTTPS Configuration
        HttpConfiguration httpsConf = new HttpConfiguration();
        httpsConf.setSecurePort(sslPort);
        httpsConf.setSecureScheme("https");
        httpsConf.addCustomizer(new SecureRequestCustomizer()); // adds ssl info to request object

        // Establish the ServerConnector
        ServerConnector httpsConnector = new ServerConnector(server,
            new SslConnectionFactory(sslContextFactory, "http/1.1"),
            new HttpConnectionFactory(httpsConf));
        httpsConnector.setPort(sslPort);

        server.addConnector(httpsConnector);

        // The location of the webapp base resource (for resources and static file serving)
        ClassLoader cl = JakartaBrowserMain.class.getClassLoader();
        // We look for a file, as ClassLoader.getResource() is not
        // designed to look for directories (we resolve the directory later)
        URL f = cl.getResource("websocket-statics/index.html");
        if (f == null)
        {
            throw new RuntimeException("Unable to find resource directory");
        }
        URI webRootUri = f.toURI().resolve("./").normalize();
        System.err.println("WebRoot is " + webRootUri);

        // Setup ServletContext
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.setBaseResource(Resource.newResource(webRootUri));
        ServletHolder holder = context.addServlet(DefaultServlet.class, "/");
        holder.setInitParameter("dirAllowed", "true");
        server.setHandler(context);

        JavaxWebSocketServletContainerInitializer.configure(context,
            (servletContext, wsContainer) -> wsContainer.addEndpoint(JakartaBrowserSocket.class));

        LOG.info("{} setup on (http) port {} and (https) port {}", this.getClass().getName(), port, sslPort);
    }

    private static Resource findKeyStore() throws URISyntaxException, MalformedURLException
    {
        ClassLoader cl = JakartaBrowserMain.class.getClassLoader();
        String keystoreResource = "ssl/keystore";
        URL f = cl.getResource(keystoreResource);
        if (f == null)
        {
            throw new RuntimeException("Unable to find " + keystoreResource);
        }

        return Resource.newResource(f.toURI());
    }
}
