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

package examples.browser;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.jetty.ee10.servlet.DefaultServlet;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.ee10.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceFactory;
import org.eclipse.jetty.util.resource.Resources;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Set up a JakartaEE WebSocket Server that can be used by a browser
 */
public class JakartaBrowserMain
{
    private static final Logger LOG = LoggerFactory.getLogger(JakartaBrowserMain.class);

    public static void main(String[] args) throws Exception
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

        Server server = newServer(port, sslPort);
        server.start();
        server.join();
    }

    public static Server newServer(int port, int sslPort) throws MalformedURLException, URISyntaxException
    {
        Server server = new Server();

        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        server.addConnector(connector);

        // Setup SSL
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStoreResource(findKeyStore(ResourceFactory.of(server)));
        sslContextFactory.setKeyStorePassword("OBF:1vny1zlo1x8e1vnw1vn61x8g1zlu1vn4");
        sslContextFactory.setKeyManagerPassword("OBF:1u2u1wml1z7s1z7a1wnl1u2g");
        sslContextFactory.setSniRequired(false);

        // Setup HTTPS Configuration
        HttpConfiguration httpsConf = new HttpConfiguration();
        httpsConf.setSecurePort(sslPort);
        httpsConf.setSecureScheme("https");
        SecureRequestCustomizer secureRequestCustomizer = new SecureRequestCustomizer();
        // Disable SNI requirements, to allow for testing against IP or localhost
        secureRequestCustomizer.setSniRequired(false);
        secureRequestCustomizer.setSniHostCheck(false);
        httpsConf.addCustomizer(secureRequestCustomizer); // adds ssl info to request object

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
        URL f = cl.getResource("browser-root/index.html");
        if (f == null)
        {
            throw new RuntimeException("Unable to find resource directory");
        }
        URI webRootUri = f.toURI().resolve("./").normalize();
        System.err.println("WebRoot is " + webRootUri);

        // Setup ServletContext
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        ResourceFactory resourceFactory = ResourceFactory.of(context);
        context.setBaseResource(resourceFactory.newResource(webRootUri));
        ServletHolder holder = context.addServlet(DefaultServlet.class, "/");
        holder.setInitParameter("dirAllowed", "true");
        server.setHandler(context);

        JakartaWebSocketServletContainerInitializer.configure(context,
            (servletContext, wsContainer) -> wsContainer.addEndpoint(JakartaBrowserSocket.class));

        LOG.info("{} setup on (http) port {} and (https) port {}", JakartaBrowserMain.class.getName(), port, sslPort);

        return server;
    }

    private static Resource findKeyStore(ResourceFactory resourceFactory)
    {
        String resourceName = "ssl/keystore";
        Resource resource = resourceFactory.newClassLoaderResource(resourceName, true);
        if (!Resources.isReadableFile(resource))
        {
            throw new RuntimeException("Unable to read " + resourceName);
        }
        return resource;
    }
}
