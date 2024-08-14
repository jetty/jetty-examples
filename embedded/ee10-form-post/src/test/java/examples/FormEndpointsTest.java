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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.eclipse.jetty.http.HttpTester;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.LifeCycle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

    public static Stream<Arguments> formTechniques()
    {
        List<Arguments> args = new ArrayList<>();

        List<String> paths = List.of(
            "/form/post-only",
            "/form/conjoined",
            "/form/service");

        // all paths support POST
        for (String path : paths)
        {
            args.add(Arguments.of("POST", path));
        }

        // Some paths support other methods
        args.add(Arguments.of("GET", "/form/conjoined"));
        args.add(Arguments.of("GET", "/form/service"));
        args.add(Arguments.of("PUT", "/form/service"));

        return args.stream();
    }

    @ParameterizedTest
    @MethodSource("formTechniques")
    public void testAsQuery(String httpMethod, String path)
    {
        HttpClient client = HttpClient.newBuilder().build();
        URI destURI = server.getURI().resolve(path);

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
                .method(httpMethod, HttpRequest.BodyPublishers.noBody())
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

    @ParameterizedTest
    @MethodSource("formTechniques")
    public void testAsUrlEncoded(String httpMethod, String path)
    {
        Assumptions.assumeTrue("POST".equals(httpMethod), "form-urlencoded is a POST only feature");

        HttpClient client = HttpClient.newBuilder().build();
        URI destURI = server.getURI().resolve(path);

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
                .method(httpMethod, HttpRequest.BodyPublishers.ofString(urlEncoded, UTF_8))
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

    @ParameterizedTest
    @MethodSource("formTechniques")
    public void testAsMultiPart(String httpMethod, String path)
    {
        Assumptions.assumeTrue("POST".equals(httpMethod), "multipart/form-data is a POST only feature");

        HttpClient client = HttpClient.newBuilder().build();
        URI destURI = server.getURI().resolve(path);

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
                .method(httpMethod, HttpRequest.BodyPublishers.ofByteArray(multipartFormBytes))
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

    @Test
    public void testFormUrlEncodedPostTooBig() throws IOException
    {
        URI uri = server.getURI();

        // Create a big urlencoded form
        byte[] formcontents = new byte[4_000_000];
        Arrays.fill(formcontents, (byte)'x');
        byte[] key = "Member=".getBytes(UTF_8);
        System.arraycopy(key, 0, formcontents, 0, key.length);

        String rawRequest = """
            POST /form/post-only HTTP/1.1
            Host: %s
            Content-Type: application/x-www-form-urlencoded
            Content-Length: %d
            Connection: close
            
            """.formatted(uri.getRawAuthority(), formcontents.length);
        try (Socket socket = new Socket(uri.getHost(), uri.getPort());
             OutputStream out = socket.getOutputStream();
             InputStream in = socket.getInputStream())
        {
            out.write(rawRequest.getBytes(UTF_8));
            // send form
            out.write(formcontents);
            out.flush();

            HttpTester.Response response = HttpTester.parseResponse(in);
            System.out.println(response.get());
            System.out.println(response.getContent());
            assertEquals(400, response.getStatus());
            assertThat(response.getContent(), containsString("Unable to parse form content"));
        }
    }

    @Test
    public void testMultipartFormPostTooBig()
    {
        HttpClient client = HttpClient.newBuilder().build();
        URI destURI = server.getURI().resolve("/form/post-only");

        Map<String, String> form = new HashMap<>();
        // create a large value, something to be bigger than the configured form size, this one should be about 180,000 characters.
        String membervalue = "Narváez expedition".repeat(10_000);
        assertThat("Value should be bigger than limits on form size", membervalue.length(), greaterThan(128_000));
        form.put("Member", membervalue);

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
                .method("POST", HttpRequest.BodyPublishers.ofByteArray(multipartFormBytes))
                .build();

            HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString(UTF_8));
            assertThat(response.statusCode(), is(400));
            assertThat(response.body(), containsString("bad multipart"));
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
