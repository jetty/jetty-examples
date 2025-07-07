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
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.stream.Stream;

import org.eclipse.jetty.http.HttpTester;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.util.component.LifeCycle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

public class MiddleManTest
{
    private static final Logger LOG = LoggerFactory.getLogger(MiddleManTest.class);
    private RemoteTestServer remoteTestServer;
    private Server server;

    @BeforeEach
    public void startRemoteServer() throws Exception
    {
        remoteTestServer = new RemoteTestServer(0);
    }

    @AfterEach
    public void stopServers()
    {
        // Stop "remote" server
        IO.close(remoteTestServer);
        // Stop proxy server
        LifeCycle.stop(server);
    }

    @Test
    public void testSimpleGET() throws Exception
    {
        HashMap<String, String> initParams = new HashMap<>();
        initParams.put("proxyTo", remoteTestServer.getURI().toASCIIString());

        server = MiddleManMain.createServer(0, initParams);
        server.start();

        LOG.info("Remote Server: {}", remoteTestServer.getURI());
        LOG.info("Proxy Server: {}", server.getURI());

        HttpClient httpClient = HttpClient.newBuilder().build();
        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(server.getURI().resolve("/deep/index.html"))
            .GET()
            .build();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(UTF_8));
        assertThat(httpResponse.statusCode(), is(200));
        assertThat(httpResponse.body(),
            allOf(
                containsString("request.method=GET"),
                containsString("request.uri=" + remoteTestServer.getURI().resolve("/deep/index.html"))
            ));
    }

    public static Stream<Arguments> unsafeRequestPaths()
    {
        /* The first argument is the path, as sent to the proxy server.
         * The second argument is the path as seen by the remote (proxyTo) server.
         */
        return Stream.of(
            // canonicalization of path is performed by AsyncMiddleMansServlet
            Arguments.of("/deep/../index.html", "/index.html"),
            // compactPath of path is performed by AsyncMiddleMansServlet
            Arguments.of("/deep//index.html", "/deep/index.html"),
            // Preserve encoded "."
            Arguments.of("/deep/%2e%2e/index.html", "/deep/%2e%2e/index.html"),
            // Don't change the encoded "." case.
            Arguments.of("/deep/%2E%2e/index.html", "/deep/%2E%2e/index.html"),
            // Preserve encoded "/"
            Arguments.of("/deep/a%2Fb/index.html", "/deep/a%2Fb/index.html")
        );
    }

    @ParameterizedTest
    @MethodSource("unsafeRequestPaths")
    public void testUnsafeRequestPaths(String unsafeRequestPath, String expectedRemoteRequestPath) throws Exception
    {
        HashMap<String, String> initParams = new HashMap<>();
        initParams.put("proxyTo", remoteTestServer.getURI().toASCIIString());

        server = MiddleManMain.createServer(0, initParams);
        server.start();

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Remote Server: {}", remoteTestServer.getURI());
            LOG.debug("Proxy Server: {}", server.getURI());
        }

        // Using a raw Socket to avoid cleanup of the Request performed by various
        // HttpClient implementations.
        URI uri = server.getURI();
        try (Socket socket = new Socket(uri.getHost(), uri.getPort());
             OutputStream out = socket.getOutputStream();
             InputStream in = socket.getInputStream())
        {
            String rawRequest = """
                GET %s HTTP/1.1\r
                Host: %s\r
                Connection: close\r
                
                """.formatted(unsafeRequestPath, uri.getRawAuthority());

            out.write(rawRequest.getBytes(UTF_8));
            out.flush();

            HttpTester.Response response = HttpTester.parseResponse(in);
            assertThat(response.getStatus(), is(200));
            assertThat(response.getContent(),
                allOf(
                    containsString("request.method=GET"),
                    containsString("request.uri=" + remoteTestServer.getURI().resolve(expectedRemoteRequestPath))
                ));
        }
    }
}
