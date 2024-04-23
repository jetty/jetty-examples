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
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
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
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        server.setHandler(contexts);

        contexts.addHandler(new ContextHandler(new HelloCommonsLoggingHandler("Hello commons-logging"),"/clogging"));
        contexts.addHandler(new ContextHandler(new HelloJULHandler("Hello JUL"),"/jul"));
        contexts.addHandler(new ContextHandler(new HelloLog4jHandler("Hello Log4j"),"/log4j"));
        contexts.addHandler(new ContextHandler(new HelloSlf4jHandler("Hello Slf4j"),"/slf4j"));

        // Start server
        server.start();

        // Establish the Server URI
        serverUri = server.getURI().resolve("/");
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
