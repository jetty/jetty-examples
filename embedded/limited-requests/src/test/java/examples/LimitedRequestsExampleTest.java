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

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpTester;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.LifeCycle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class LimitedRequestsExampleTest
{
    private Server server;

    @BeforeEach
    public void startServer() throws Exception
    {
        server = LimitedRequestsExample.newServer(0);
        server.start();
    }

    @AfterEach
    public void stopServer()
    {
        LifeCycle.stop(server);
    }

    @Test
    public void testLimitedRequests() throws IOException
    {
        URI serverURI = server.getURI();

        String rawRequest = "GET / HTTP/1.1\r\n" +
            "Host: " + serverURI.getRawAuthority() + "\r\n" +
            "\r\n";

        try (Socket client = new Socket(serverURI.getHost(), serverURI.getPort());
             OutputStream out = client.getOutputStream();
             InputStream in = client.getInputStream();)
        {
            // Per configuration of setMaxRequests in LimitedRequestsExample this will allow
            // only 5 requests before sending a Connection: close
            for (int i = 1; i < 5; i++)
            {
                out.write(rawRequest.getBytes(StandardCharsets.UTF_8));
                out.flush();

                HttpTester.Response response = HttpTester.parseResponse(in);
                assertThat(response.getStatus(), is(200));
                assertThat(response.get(HttpHeader.CONNECTION), is(nullValue()));
            }

            // and one final one should trigger the closure
            out.write(rawRequest.getBytes(StandardCharsets.UTF_8));
            out.flush();

            HttpTester.Response response = HttpTester.parseResponse(in);
            assertThat(response.getStatus(), is(200));
            assertThat(response.get(HttpHeader.CONNECTION), is("close"));
        }
    }
}
