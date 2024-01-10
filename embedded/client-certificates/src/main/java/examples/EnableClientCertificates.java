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
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.SecuredRedirectHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;

/**
 * Demonstrates how to enable the JVM handling of Client Certificates.
 *
 * @see javax.net.ssl.SSLEngine#setWantClientAuth(boolean)
 * @see javax.net.ssl.SSLEngine#setNeedClientAuth(boolean)
 */
public class EnableClientCertificates
{
    public static void main(String[] args) throws Exception
    {
        Server server = new Server();
        int httpsPort = 8443;

        // Setup HTTP Connector
        HttpConfiguration httpConf = new HttpConfiguration();
        httpConf.setSecurePort(httpsPort);
        httpConf.setSecureScheme("https");

        // Setup SSL
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStoreResource(findKeyStore());
        sslContextFactory.setKeyStorePassword("OBF:1vny1zlo1x8e1vnw1vn61x8g1zlu1vn4");
        sslContextFactory.setKeyManagerPassword("OBF:1u2u1wml1z7s1z7a1wnl1u2g");
        sslContextFactory.setWantClientAuth(true); // Turn on javax.net.ssl.SSLEngine.wantClientAuth
        sslContextFactory.setNeedClientAuth(true); // Turn on javax.net.ssl.SSLEngine.needClientAuth

        // Setup HTTPS Configuration
        HttpConfiguration httpsConf = new HttpConfiguration();
        httpsConf.setSecureScheme("https");
        httpsConf.setSecurePort(httpsPort);
        httpsConf.addCustomizer(new SecureRequestCustomizer()); // adds ssl info to request object

        // Establish the HTTPS ServerConnector
        ServerConnector httpsConnector = new ServerConnector(server,
            new SslConnectionFactory(sslContextFactory, "http/1.1"),
            new HttpConnectionFactory(httpsConf));
        httpsConnector.setPort(httpsPort);

        server.addConnector(httpsConnector);

        // Add a Handlers for requests
        HandlerList handlers = new HandlerList();
        handlers.addHandler(new SecuredRedirectHandler());
        handlers.addHandler(new HelloHandler("Hello Secure World"));
        server.setHandler(handlers);

        server.start();
        server.join();
    }

    private static Resource findKeyStore() throws URISyntaxException, MalformedURLException
    {
        ClassLoader cl = EnableClientCertificates.class.getClassLoader();
        String keystoreResource = "ssl/keystore";
        URL f = cl.getResource(keystoreResource);
        if (f == null)
        {
            throw new RuntimeException("Unable to find " + keystoreResource);
        }

        return Resource.newResource(f.toURI());
    }
}
