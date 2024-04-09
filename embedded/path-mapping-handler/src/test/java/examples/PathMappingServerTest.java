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
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.LifeCycle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PathMappingServerTest
{
    private Server server;

    @BeforeEach
    public void startServer() throws Exception
    {
        server = PathMappingServer.newServer(0);
        server.start();
    }

    @AfterEach
    public void stopServer()
    {
        LifeCycle.stop(server);
    }

    @Test
    public void testGetRootResource() throws IOException, InterruptedException
    {
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(server.getURI().resolve("/"))
            .GET()
            .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertThat(response.statusCode(), is(200));
        assertThat(response.body(), containsString("<h4>This is the index.html from /static-root/</h4>"));
    }

    @Test
    public void testGetRootHelloResource() throws IOException, InterruptedException
    {
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(server.getURI().resolve("/hello.html"))
            .GET()
            .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertThat(response.statusCode(), is(200));
        assertThat(response.body(), containsString("<h4>Hello from <code>/static-root/</code></h4>"));
    }

    @Test
    public void testGetExtrasResource() throws IOException, InterruptedException
    {
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(server.getURI().resolve("/extras/extra.css"))
            .GET()
            .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertThat(response.statusCode(), is(200));
        assertThat(response.body(), containsString("table { font-family: sans-serif; }"));
    }

    @Test
    public void testGetMetaInfResource() throws IOException, InterruptedException
    {
        Properties props = loadClassPathProperties("/META-INF/maven/org.webjars/bootstrap/pom.properties");
        String version = props.getProperty("version");

        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(server.getURI().resolve("/jars/webjars/bootstrap/@VER@/js/bootstrap.bundle.js".replace("@VER@", version)))
            .GET()
            .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertThat(response.statusCode(), is(200));
        assertThat(response.body(), containsString("* Bootstrap v@VER@ (https://getbootstrap.com/)".replace("@VER@", version)));
    }

    private Properties loadClassPathProperties(String resourceName) throws IOException
    {
        URL url = PathMappingServerTest.class.getResource(resourceName);
        assertNotNull(url, "Unable to find: " + resourceName);
        try (InputStream input = url.openStream())
        {
            Properties props = new Properties();
            props.load(input);
            return props;
        }
    }

    /**
     * All of these request paths will serve the same logo.png
     */
    @ParameterizedTest
    @ValueSource(strings = {
        "/logo.png",
        "/deep/into/a/path/logo.png",
        "/images/logo.png",
        "/alternate/logo.png"
    })
    public void testGetPngResource(String path) throws IOException, InterruptedException
    {
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(server.getURI().resolve(path))
            .GET()
            .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertThat(response.statusCode(), is(200));
        assertThat(response.headers().firstValueAsLong("Content-Length").orElseThrow(), is(3142L));
        assertThat(response.headers().firstValue("Content-Type").orElseThrow(), is("image/png"));
    }
}
