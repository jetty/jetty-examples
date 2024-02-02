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
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.toolchain.test.FS;
import org.eclipse.jetty.toolchain.test.MavenPaths;
import org.eclipse.jetty.util.component.LifeCycle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

public class RequestLogToFileDemoTest
{
    private static final Logger LOG = LoggerFactory.getLogger(RequestLogToFileDemoTest.class);
    private Server server;
    private Path logsDir;

    @BeforeEach
    public void startServer() throws Exception
    {
        logsDir = MavenPaths.targetTestDir(RequestLogToFileDemoTest.class.getSimpleName());
        FS.ensureDirExists(logsDir);

        server = RequestLogToFileDemo.newServer(0, logsDir);
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
        long fileSizeInitial = getActiveLogFileSize();
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

        long fileSizeNow = getActiveLogFileSize();
        assertThat("Request Log should have grown in size", fileSizeNow, greaterThan(fileSizeInitial));
    }

    private long getActiveLogFileSize()
    {
        try
        {
            Path requestlog = logsDir.resolve("request.log");
            if (Files.isRegularFile(requestlog))
                return Files.size(requestlog);
            return -1;
        }
        catch (IOException e)
        {
            LOG.warn("Unable to calculate log file size", e);
            return -1;
        }
    }
}
