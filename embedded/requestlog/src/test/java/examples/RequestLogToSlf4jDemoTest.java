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
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.LifeCycle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class RequestLogToSlf4jDemoTest
{
    private static final Logger LOG = LoggerFactory.getLogger(RequestLogToSlf4jDemoTest.class);
    private Server server;

    @BeforeEach
    public void startServer() throws Exception
    {
        LOG.warn("===== SEE Console Logging Output for \":INFO :examples.requests:\" entries =====");

        server = RequestLogToSlf4jDemo.newServer(0);
        server.start();
    }

    @AfterEach
    public void stopServer()
    {
        LifeCycle.stop(server);
    }

    @Test
    public void testRequests() throws IOException, InterruptedException
    {
        HttpClient client = HttpClient.newBuilder().build();

        HttpRequest request = HttpRequest.newBuilder()
            .uri(server.getURI().resolve("/interesting/dir"))
            .version(HttpClient.Version.HTTP_1_1)
            .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(UTF_8));
        assertThat(response.statusCode(), is(200));

        request = HttpRequest.newBuilder()
            .uri(server.getURI().resolve("/bogus/path"))
            .version(HttpClient.Version.HTTP_1_1)
        .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString(UTF_8));
        assertThat(response.statusCode(), is(404));
    }
}
