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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.LifeCycle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class FormEndpointsTest
{
    private Server server;

    @BeforeEach
    public void startServer() throws Exception
    {
        server = FormEndpoints.newServer(8080);
        server.start();
    }

    @AfterEach
    public void stopServer()
    {
        LifeCycle.stop(server);
    }

    @Test
    public void testAsQuery()
    {
        HttpClient client = HttpClient.newBuilder().build();
        URI destURI = server.getURI().resolve("/form/query");

        Map<String, String> form = new HashMap<>();
        form.put("Member", "Esteban de Dorantes");

        try
        {
            String urlEncoded = form.entrySet()
                .stream()
                .map(e -> URLEncoder.encode(e.getKey(), UTF_8) + "=" + URLEncoder.encode(e.getValue(), UTF_8))
                .collect(Collectors.joining("&"));

            URI uriWithQuery = new URI(
                destURI.getScheme(),
                destURI.getRawAuthority(),
                destURI.getRawPath(),
                urlEncoded,
                null);

            HttpRequest request = HttpRequest
                .newBuilder(uriWithQuery)
                .GET()
                .build();

            HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString(UTF_8));
            assertThat(response.statusCode(), is(200));
        }
        catch (URISyntaxException e)
        {
            Assertions.fail("Unable to make URI with query", e);
        }
        catch (IOException | InterruptedException e)
        {
            Assertions.fail("Unable to submitFormAsQuery(" + destURI + ")", e);
        }
    }

    @Test
    public void testAsUrlEncoded()
    {
        HttpClient client = HttpClient.newBuilder().build();
        URI destURI = server.getURI().resolve("/form/urlencoded");

        Map<String, String> form = new HashMap<>();
        form.put("Member", "Álvar Núñez Cabeza de Vaca");

        try
        {
            String urlEncoded = form.entrySet()
                .stream()
                .map(e -> URLEncoder.encode(e.getKey(), UTF_8) + "=" + URLEncoder.encode(e.getValue(), UTF_8))
                .collect(Collectors.joining("&"));

            HttpRequest request = HttpRequest
                .newBuilder(destURI)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(urlEncoded, UTF_8))
                .build();

            HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString(UTF_8));
            assertThat(response.statusCode(), is(200));
        }
        catch (IOException | InterruptedException e)
        {
            Assertions.fail("Unable to submitFormUrlEncoded(" + destURI + ")", e);
        }
    }

    @Test
    public void testAsMultiPart()
    {
        HttpClient client = HttpClient.newBuilder().build();
        URI destURI = server.getURI().resolve("/form/multipart");

        Map<String, String> form = new HashMap<>();
        form.put("Member", "Andrés Dorantes de Carranza");

        try
        {
            // Build multipart form
            MultipartEntityBuilder multipartBuilder = MultipartEntityBuilder.create();

            form.forEach((key, value) -> multipartBuilder.addPart(key,
                new StringBody(
                    value,
                    ContentType.create("application/x-www-form-urlencoded", StandardCharsets.UTF_8)
                )
            ));

            HttpEntity multipartForm = multipartBuilder.build();

            byte[] multipartFormBytes = toByteArray(multipartForm);

            // Send Request
            HttpRequest request = HttpRequest
                .newBuilder(destURI)
                .header("Content-Type", multipartForm.getContentType().getValue())
                .POST(HttpRequest.BodyPublishers.ofByteArray(multipartFormBytes))
                .build();

            HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString(UTF_8));
            assertThat(response.statusCode(), is(200));
        }
        catch (IOException | InterruptedException e)
        {
            Assertions.fail("Unable to submitFormMultipart(" + destURI + ")", e);
        }
    }

    private byte[] toByteArray(HttpEntity multipartForm) throws IOException
    {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream())
        {
            multipartForm.writeTo(out);
            return out.toByteArray();
        }
    }
}
