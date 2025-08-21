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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.zip.GZIPInputStream;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.util.component.LifeCycle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GzipRequestResponseExampleTest
{
    private Server server;

    @AfterEach
    public void stopServer()
    {
        LifeCycle.stop(server);
    }

    @Test
    public void testGetWithGzip() throws Exception
    {
        server = GzipRequestResponseExample.newServer(0);
        server.start();

        HttpClient httpClient = HttpClient.newBuilder().build();
        HttpRequest httpRequest = HttpRequest.newBuilder()
            .GET()
            .uri(server.getURI().resolve("/hello.html"))
            .header("Accept-Encoding", "gzip")
            .build();
        HttpResponse<InputStream> httpResponse = httpClient.send(httpRequest,
            HttpResponse.BodyHandlers.ofInputStream());
        assertEquals(200, httpResponse.statusCode());
        try (InputStream in = httpResponse.body();
             GZIPInputStream gzipIn = new GZIPInputStream(in);
             InputStreamReader reader = new InputStreamReader(gzipIn, UTF_8))
        {
            String body = IO.toString(reader);
            assertThat(body, containsString("<title>Hello from src/main/resources/static-root/</title>"));
        }
    }
}
