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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.util.component.LifeCycle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class AppTest
{
    private static Server server;
    private static URI serverUri;

    @BeforeEach
    public void startServer() throws Exception
    {
        LoggingUtil.config();

        server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(0); // let it use whatever port thats free
        server.addConnector(connector);

        // add handlers
        ServletContextHandler contexts = new ServletContextHandler();
        contexts.setContextPath("/");
        server.setHandler(contexts);

        contexts.addServlet(new ServletHolder(new HelloCommonsLoggingServlet("Hello commons-logging")),"/clogging/*");
        contexts.addServlet(new ServletHolder(new HelloJettyServlet("Hello Jetty")),"/jetty/*");
        contexts.addServlet(new ServletHolder(new HelloJULServlet("Hello JUL")),"/jul/*");
        contexts.addServlet(new ServletHolder(new HelloLog4jServlet("Hello Log4j")),"/log4j/*");
        contexts.addServlet(new ServletHolder(new HelloSlf4jServlet("Hello Slf4j")),"/slf4j/*");

        // Start server
        server.start();

        // Establish the Server URI
        String host = connector.getHost();
        if (host == null)
        {
            host = "localhost";
        }
        int port = connector.getLocalPort();
        serverUri = new URI(String.format("http://%s:%d/",host,port));
    }

    @AfterEach
    public void stopServer()
    {
        LifeCycle.stop(server);
    }

    @Test
    public void testGetAll() throws IOException
    {
        String loggername = "examples";
        CaptureHandler capture = CaptureHandler.attach(loggername);

        List<String> paths = new ArrayList<>();
        paths.add("/clogging/");
        paths.add("/jetty/");
        paths.add("/jul/");
        paths.add("/log4j/");
        paths.add("/slf4j/");

        try
        {
            // Request each path
            for (String path : paths)
            {
                URI getURI = serverUri.resolve(path);

                HttpURLConnection connection = (HttpURLConnection)getURI.toURL().openConnection();
                assertThat("Connection.statusCode",connection.getResponseCode(),is(HttpURLConnection.HTTP_OK));
                try (InputStream in = connection.getInputStream())
                {
                    String response = IO.toString(in);
                    assertThat("response",response,containsString("Hello"));
                }
            }

            // Validate log entries
            for (String path : paths)
            {
                capture.assertContainsRecord("examples.",path);
            }
        }
        finally
        {
            capture.detach(loggername);
        }
    }
}
