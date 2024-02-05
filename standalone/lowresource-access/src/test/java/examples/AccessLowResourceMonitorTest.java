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
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jetty.deploy.DeploymentManager;
import org.eclipse.jetty.deploy.PropertiesConfigurationManager;
import org.eclipse.jetty.deploy.providers.WebAppProvider;
import org.eclipse.jetty.server.LowResourceMonitor;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.webapp.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AccessLowResourceMonitorTest
{
    private Server server;

    @BeforeEach
    public void startServer() throws Exception
    {
        server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(0);
        server.addConnector(connector);

        // Handler tree
        HandlerList handlers = new HandlerList();
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        handlers.addHandler(contexts);
        handlers.addHandler(new DefaultHandler());
        server.setHandler(handlers);

        // create War
        Path jettyBase = Paths.get(System.getProperty("user.dir"));
        Path webappsDir = jettyBase.resolve("webapps");
        try (WarBuilder war = new WarBuilder(webappsDir.resolve("demo.war")))
        {
            war.addClasses(Paths.get("target/classes"));
            war.addDir(Paths.get("src/main/webapp"));
        }

        // enable hot deployment
        DeploymentManager deployer = new DeploymentManager();
        deployer.setContexts(contexts);
        WebAppProvider webAppProvider = new WebAppProvider();
        webAppProvider.setMonitoredDirName(jettyBase + "/webapps");
        webAppProvider.setScanInterval(0);
        webAppProvider.setExtractWars(true);
        webAppProvider.setConfigurationManager(new PropertiesConfigurationManager());
        deployer.addAppProvider(webAppProvider);
        server.addBean(deployer);

        // add LowResourceMonitor
        LowResourceMonitor lowResourcesMonitor = new LowResourceMonitor(server);
        lowResourcesMonitor.setPeriod(1000);
        lowResourcesMonitor.setLowResourcesIdleTimeout(200);
        lowResourcesMonitor.setMonitorThreads(true);
        lowResourcesMonitor.setMaxMemory(0);
        lowResourcesMonitor.setMaxLowResourcesTime(5000);
        server.addBean(lowResourcesMonitor);

        // start server
        // DEBUG state - server.setDumpAfterStart(true);
        server.start();
    }

    @AfterEach
    public void teardown()
    {
        LifeCycle.stop(server);
    }

    @Test
    public void testFromServletContext() throws IOException
    {
        URI dest = server.getURI().resolve("/demo/servletcontext");
        System.err.println("Request to " + dest);
        HttpURLConnection http = (HttpURLConnection)dest.toURL().openConnection();
        try (InputStream in = http.getInputStream())
        {
            System.err.println(IO.toString(in, UTF_8));
        }
        assertEquals(http.getResponseCode(), HttpURLConnection.HTTP_OK, "HTTP Response Status Code");
    }

    @Test
    public void testFromRequestAttributeChannel() throws IOException
    {
        URI dest = server.getURI().resolve("/demo/requestattribute/channel");
        System.err.println("Request to " + dest);
        HttpURLConnection http = (HttpURLConnection)dest.toURL().openConnection();
        try (InputStream in = http.getInputStream())
        {
            System.err.println(IO.toString(in, UTF_8));
        }
        assertEquals(http.getResponseCode(), HttpURLConnection.HTTP_OK, "HTTP Response Status Code");
    }

    @Test
    public void testFromRequestAttributeConnection() throws IOException
    {
        URI dest = server.getURI().resolve("/demo/requestattribute/connection");
        System.err.println("Request to " + dest);
        HttpURLConnection http = (HttpURLConnection)dest.toURL().openConnection();
        try (InputStream in = http.getInputStream())
        {
            System.err.println(IO.toString(in, UTF_8));
        }
        assertEquals(http.getResponseCode(), HttpURLConnection.HTTP_OK, "HTTP Response Status Code");
    }

    @Test
    public void testFromBaseRequest() throws IOException
    {
        URI dest = server.getURI().resolve("/demo/baserequest");
        System.err.println("Request to " + dest);
        HttpURLConnection http = (HttpURLConnection)dest.toURL().openConnection();
        try (InputStream in = http.getInputStream())
        {
            System.err.println(IO.toString(in, UTF_8));
        }
        assertEquals(http.getResponseCode(), HttpURLConnection.HTTP_OK, "HTTP Response Status Code");
    }
}
