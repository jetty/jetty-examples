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

import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.HttpTester;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.LifeCycle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class RejectUserAgentRuleExampleTest
{
    private Server server;

    @BeforeEach
    public void startServer() throws Exception
    {
        server = RejectUserAgentRuleExample.newServer(0);
        server.start();
    }

    @AfterEach
    public void stopServer()
    {
        LifeCycle.stop(server);
    }

    @Test
    public void testReject() throws IOException
    {
        String rawRequest = """
            GET /dump/ HTTP/1.1
            Host: www.example.org
            User-Agent: AI-Robot
            Connection: close
            
            """;

        URI uri = server.getURI();

        try (Socket socket = new Socket(uri.getHost(), uri.getPort());
             OutputStream out = socket.getOutputStream();
             InputStream in = socket.getInputStream())
        {
            out.write(rawRequest.getBytes(UTF_8));
            out.flush();

            HttpTester.Response response = HttpTester.parseResponse(in);
            assertThat(response.getStatus(), is(HttpStatus.UNAUTHORIZED_401));
        }
    }

    @Test
    public void testAllow() throws IOException
    {
        String rawRequest = """
            GET /dump/ HTTP/1.1
            Host: www.example.org
            User-Agent: Fancy-Browser
            Connection: close
            
            """;

        URI uri = server.getURI();

        try (Socket socket = new Socket(uri.getHost(), uri.getPort());
             OutputStream out = socket.getOutputStream();
             InputStream in = socket.getInputStream())
        {
            out.write(rawRequest.getBytes(UTF_8));
            out.flush();

            HttpTester.Response response = HttpTester.parseResponse(in);
            assertThat(response.getStatus(), is(HttpStatus.OK_200));
        }
    }
}
