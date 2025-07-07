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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.http.UriCompliance;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example of using {@link org.eclipse.jetty.ee10.proxy.AsyncMiddleManServlet} in an Embedded Jetty instance.
 */
public class MiddleManMain
{
    private static final Logger LOG = LoggerFactory.getLogger(MiddleManMain.class);

    public static void main(String[] args) throws Exception
    {
        final int port = 8080;

        if (args.length < 1)
        {
            System.err.println("ERROR: Missing <proxyTo>");
            System.err.printf("Usage: java %s <proxyTo>%n", MiddleManMain.class.getName());
            System.exit(-1);
        }

        Map<String, String> initParams = new HashMap<>();
        initParams.put("proxyTo", args[0]);
        Server server = MiddleManMain.createServer(port, initParams);
        server.start();
        LOG.info("Server started on {}", server.getURI().resolve("/"));
        server.join();
    }

    public static Server createServer(int port, Map<String, String> initParams)
    {
        Server server = new Server();

        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        // Configure middleman proxy server to allow any unsafe URI
        connector.getContainedBeans(HttpConfiguration.class)
            .forEach((httpConfig) ->
                httpConfig.setUriCompliance(UriCompliance.UNSAFE));
        server.addConnector(connector);

        ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletContextHandler.setContextPath("/");
        servletContextHandler.getServletHandler().setDecodeAmbiguousURIs(true);

        ServletHolder middleManHolder = new ServletHolder("middleman", CustomAsyncMiddleManServlet.class);
        initParams.forEach(middleManHolder::setInitParameter);
        servletContextHandler.addServlet(middleManHolder, "/*");

        server.setHandler(servletContextHandler);

        return server;
    }
}
