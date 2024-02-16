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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.eclipse.jetty.http.HttpTester;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.SecuredRedirectHandler;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceFactory;
import org.eclipse.jetty.util.resource.Resources;
import org.eclipse.jetty.util.ssl.SslContextFactory;

public class SecuredRedirectHandlerExample
{
    public static void main(String[] args) throws Exception
    {
        SecuredRedirectHandlerExample example = new SecuredRedirectHandlerExample();
        try
        {
            int httpPort = 8080;
            int httpsPort = 8443;

            example.startServer(httpPort, httpsPort);
            URI serverURI = example.getServerURI();

            /* Issue an HTTP (not secure) request to http://localhost:8080/foo.
             * Expecting to see a 302 Found redirect with a `Location`
             * response header containing the secure `https` location of
             * the same request URI - https://localhost:8443/foo
             */
            String rawRequest = "GET /foo HTTP/1.1\r\n" +
                "Host: localhost:" + httpPort + "\r\n" +
                "Connection: close\r\n" +
                "\r\n";

            example.testRequest(serverURI.getHost(), httpPort, rawRequest);
        }
        finally
        {
            example.stopServer();
        }
    }

    private Server server;

    public void startServer(int httpPort, int httpsPort) throws Exception
    {
        server = new Server();

        // Setup HTTP Connector
        HttpConfiguration httpConf = new HttpConfiguration();
        httpConf.setSecurePort(httpsPort);
        httpConf.setSecureScheme("https");

        // Establish the HTTP ServerConnector
        ServerConnector httpConnector = new ServerConnector(server,
            new HttpConnectionFactory(httpConf));
        httpConnector.setPort(httpPort);
        server.addConnector(httpConnector);

        // Setup SSL
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStoreResource(findKeyStore(ResourceFactory.of(server)));
        sslContextFactory.setKeyStorePassword("OBF:1vny1zlo1x8e1vnw1vn61x8g1zlu1vn4");
        sslContextFactory.setKeyManagerPassword("OBF:1u2u1wml1z7s1z7a1wnl1u2g");

        // Setup HTTPS Configuration
        HttpConfiguration httpsConf = new HttpConfiguration(httpConf);
        httpsConf.addCustomizer(new SecureRequestCustomizer()); // adds ssl info to request object

        // Establish the HTTPS ServerConnector
        ServerConnector httpsConnector = new ServerConnector(server,
            new SslConnectionFactory(sslContextFactory, "http/1.1"),
            new HttpConnectionFactory(httpsConf));
        httpsConnector.setPort(httpsPort);

        server.addConnector(httpsConnector);

        // Add a Handlers for requests
        Handler.Sequence handlers = new Handler.Sequence();
        handlers.addHandler(new SecuredRedirectHandler());
        handlers.addHandler(new HelloHandler("Hello Secure World"));
        server.setHandler(handlers);

        server.start();
    }

    public void stopServer()
    {
        LifeCycle.stop(server);
    }

    public URI getServerURI()
    {
        return server.getURI();
    }

    private void testRequest(String host, int port, String rawRequest) throws IOException
    {
        try (Socket client = new Socket(host, port))
        {
            OutputStream out = client.getOutputStream();
            out.write(rawRequest.getBytes(StandardCharsets.UTF_8));
            out.flush();

            InputStream in = client.getInputStream();
            HttpTester.Response response = HttpTester.parseResponse(in);

            System.out.println(response);
            System.out.println(response.getContent());
        }
    }

    private static Resource findKeyStore(ResourceFactory resourceFactory)
    {
        String resourceName = "ssl/keystore";
        Resource resource = resourceFactory.newClassLoaderResource(resourceName);
        if (!Resources.isReadableFile(resource))
        {
            throw new RuntimeException("Unable to read " + resourceName);
        }
        return resource;
    }
}
