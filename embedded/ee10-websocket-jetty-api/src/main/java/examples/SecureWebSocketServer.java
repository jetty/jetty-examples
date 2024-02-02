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

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.websocket.server.JettyWebSocketServlet;
import org.eclipse.jetty.ee10.websocket.server.JettyWebSocketServletFactory;
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
import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create a Secure WebSocket Server and host an Echo WebSocket endpoint on "/echo"
 * <p>
 * Note: testing this can be tricky on modern browsers with "wss://localhost:8443/echo"
 * as they will reject either connecting to localhost, or reject any self-signed certificate.
 * </p>
 */
public class SecureWebSocketServer
{
    public static class EchoSocketServlet extends JettyWebSocketServlet
    {
        @Override
        protected void configure(JettyWebSocketServletFactory jettyWebSocketServletFactory)
        {
            jettyWebSocketServletFactory.register(EchoSocket.class);
        }
    }

    public static class EchoSocket
    {
        private static final Logger LOG = LoggerFactory.getLogger(EchoSocket.class);

        @OnWebSocketMessage
        public void onWebSocketText(Session session, String message)
        {
            session.sendText(message, Callback.NOOP);
        }

        @OnWebSocketError
        public void onWebSocketError(Throwable cause)
        {
            LOG.warn("WebSocket error", cause);
        }
    }

    public static void main(String[] args)
    {
        Server server = new Server();
        int httpsPort = 8443;

        // Setup SSL
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStoreResource(findKeyStore(ResourceFactory.of(server)));
        sslContextFactory.setKeyStorePassword("OBF:1vny1zlo1x8e1vnw1vn61x8g1zlu1vn4");
        sslContextFactory.setKeyManagerPassword("OBF:1u2u1wml1z7s1z7a1wnl1u2g");

        // Setup HTTPS Configuration
        HttpConfiguration httpsConf = new HttpConfiguration();
        httpsConf.setSecurePort(httpsPort);
        httpsConf.setSecureScheme("https");
        httpsConf.addCustomizer(new SecureRequestCustomizer()); // adds ssl info to request object

        // Establish the Secure ServerConnector
        ServerConnector httpsConnector = new ServerConnector(server,
            new SslConnectionFactory(sslContextFactory, "http/1.1"),
            new HttpConnectionFactory(httpsConf));
        httpsConnector.setPort(httpsPort);

        server.addConnector(httpsConnector);

        // Setup the basic application "context" for this application at "/"
        // This is also known as the handler tree (in jetty speak)
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        // Add a WebSocket as a Servlet to the context
        context.addServlet(EchoSocketServlet.class, "/echo");

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

    private static Resource findKeyStore(ResourceFactory resourceFactory)
    {
        String resourceName = "ssl/keystore";
        Resource resource = resourceFactory.newClassLoaderResource(resourceName);
        if (Resources.isReadableFile(resource))
        {
            throw new RuntimeException("Unable to read " + resourceName);
        }
        return resource;
    }
}
