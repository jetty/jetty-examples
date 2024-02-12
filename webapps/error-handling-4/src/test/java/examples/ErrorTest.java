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
import java.nio.charset.StandardCharsets;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.ee8.webapp.WebAppContext;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceFactory;
import org.eclipse.jetty.util.resource.Resources;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ErrorTest
{
    private Server server;

    @BeforeEach
    public void startServer() throws Exception
    {
        server = new Server(0);

        ContextHandlerCollection contexts = new ContextHandlerCollection();
        server.setHandler(contexts);

        WebAppContext webAppContext = new WebAppContext();
        webAppContext.setContextPath("/app");
        // As a test case, the WAR file isn't built yet, so lets piece it together.
        // First, we'll use the src/main/webapp as the resource base, but that isn't
        // 100% useful as that directory is missing the compiled classes
        ResourceFactory resourceFactory = ResourceFactory.of(webAppContext);
        Resource srcWebApp = resourceFactory.newResource("src/main/webapp");
        assertTrue(Resources.isReadableDirectory(srcWebApp));
        webAppContext.setWarResource(srcWebApp);
        // now let's add the compiled classes
        webAppContext.setExtraClasspath("target/classes/");

        // add the webapp to the handler tree
        contexts.addHandler(webAppContext);

        server.start();
    }

    @AfterEach
    public void stopServer()
    {
        LifeCycle.stop(server);
    }

    @Test
    public void testNormalRequest() throws IOException, InterruptedException
    {
        HttpClient httpClient = HttpClient.newBuilder().build();
        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(server.getURI().resolve("/app/hello"))
            .GET().build();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertThat(httpResponse.statusCode(), is(200));
        assertThat(httpResponse.body(), containsString("Hello "));
    }

    @Test
    public void testTriggers404InContext() throws IOException, InterruptedException
    {
        HttpClient httpClient = HttpClient.newBuilder().build();
        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(server.getURI().resolve("/app/triggers/404"))
            .GET().build();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertThat(httpResponse.statusCode(), is(404));
        String body = httpResponse.body();
        assertThat(body, containsString("DISPATCH: ERROR"));
        assertThat(body, containsString("contextPath: /app"));
        assertThat(body, containsString("servletPath: /errors"));
        assertThat(body, containsString("requestURI: /app/errors/400"));
        assertThat(body, containsString("ERROR_MESSAGE: Not Found"));
        assertThat(body, containsString("ERROR_CODE: 404"));
        assertThat(body, containsString("ERROR_SERVLET: triggers")); // origin was triggers servlet
        assertThat(body, containsString("ERROR_REQUEST_URI: /app/triggers/404"));
        assertThat(body, containsString("ERROR_EXCEPTION_TYPE: null"));
        assertThat(body, containsString("ERROR_EXCEPTION: null"));
    }

    /**
     * Demo of an error-page setup to respond to 403 (Forbidden) sendError with a static HTML page.
     */
    @Test
    public void testTriggers403InContext() throws IOException, InterruptedException
    {
        HttpClient httpClient = HttpClient.newBuilder().build();
        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(server.getURI().resolve("/app/triggers/" + HttpServletResponse.SC_FORBIDDEN))
            .GET().build();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertThat(httpResponse.statusCode(), is(403));
        String body = httpResponse.body();
        assertThat(body, containsString("<title>Error Handling WebApp - FORBIDDEN</title>"));
        assertThat(body, containsString("<h1>Error Handling WebApp - FORBIDDEN</h1>"));
    }

    @Test
    public void testTriggers500InContext() throws IOException, InterruptedException
    {
        HttpClient httpClient = HttpClient.newBuilder().build();
        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(server.getURI().resolve("/app/triggers/500"))
            .GET().build();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertThat(httpResponse.statusCode(), is(500));
        String body = httpResponse.body();
        assertThat(body, containsString("DISPATCH: ERROR"));
        assertThat(body, containsString("contextPath: /app"));
        assertThat(body, containsString("servletPath: /errors"));
        assertThat(body, containsString("requestURI: /app/errors/500"));
        assertThat(body, containsString("ERROR_MESSAGE: Server Error"));
        assertThat(body, containsString("ERROR_CODE: 500"));
        assertThat(body, containsString("ERROR_SERVLET: triggers")); // origin was triggers servlet
        assertThat(body, containsString("ERROR_REQUEST_URI: /app/triggers/500"));
        assertThat(body, containsString("ERROR_EXCEPTION_TYPE: null"));
        assertThat(body, containsString("ERROR_EXCEPTION: null"));
    }

    /**
     * Demo of how the {@link javax.servlet.http.HttpServletResponse#sendError(int, String)} with message parameter works.
     * <p>
     *     WARNING: This example has security issues (XSS) as it takes the provided message from the URL and uses it in
     *     the call to {@link javax.servlet.http.HttpServletResponse#sendError(int, String)}.
     *     DO NOT DO THIS IN A PRODUCTION WEBAPP.
     * </p>
     */
    @Test
    public void testTriggers500WithMessageInContext() throws IOException, InterruptedException
    {
        HttpClient httpClient = HttpClient.newBuilder().build();
        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(server.getURI().resolve("/app/triggers/Example%20Message%20from%20sendError"))
            .GET().build();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertThat(httpResponse.statusCode(), is(500));
        String body = httpResponse.body();
        assertThat(body, containsString("DISPATCH: ERROR"));
        assertThat(body, containsString("contextPath: /app"));
        assertThat(body, containsString("servletPath: /errors"));
        assertThat(body, containsString("requestURI: /app/errors/500"));
        assertThat(body, containsString("ERROR_MESSAGE: Example Message from sendError"));
        assertThat(body, containsString("ERROR_CODE: 500"));
        assertThat(body, containsString("ERROR_SERVLET: triggers")); // origin was triggers servlet
        assertThat(body, containsString("ERROR_REQUEST_URI: /app/triggers/Example%20Message%20from%20sendError"));
        assertThat(body, containsString("ERROR_EXCEPTION_TYPE: null"));
        assertThat(body, containsString("ERROR_EXCEPTION: null"));
    }

    @Test
    public void testTriggersIOExceptionInContext() throws IOException, InterruptedException
    {
        HttpClient httpClient = HttpClient.newBuilder().build();
        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(server.getURI().resolve("/app/triggers/IOException"))
            .GET().build();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertThat(httpResponse.statusCode(), is(500));
        String body = httpResponse.body();
        assertThat(body, containsString("DISPATCH: ERROR"));
        assertThat(body, containsString("contextPath: /app"));
        assertThat(body, containsString("servletPath: /errors"));
        assertThat(body, containsString("requestURI: /app/errors/500"));
        assertThat(body, containsString("ERROR_MESSAGE: java.io.IOException: Error from examples.TriggersServlet"));
        assertThat(body, containsString("ERROR_CODE: 500"));
        assertThat(body, containsString("ERROR_SERVLET: triggers")); // origin was triggers servlet
        assertThat(body, containsString("ERROR_REQUEST_URI: /app/triggers/IOException"));
        assertThat(body, containsString("ERROR_EXCEPTION_TYPE: class java.io.IOException"));
        assertThat(body, containsString("ERROR_EXCEPTION: Error from examples.TriggersServlet"));
        assertThat(body, containsString("at examples.TriggersServlet.doGet("));
    }

    @Test
    public void testTriggers404InFilterUsingHeader() throws IOException, InterruptedException
    {
        HttpClient httpClient = HttpClient.newBuilder().build();
        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(server.getURI().resolve("/app/hello"))
            .header("X-Filter-Trigger", "404")
            .GET().build();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertThat(httpResponse.statusCode(), is(404));
        String body = httpResponse.body();
        assertThat(body, containsString("DISPATCH: ERROR"));
        assertThat(body, containsString("contextPath: /app"));
        assertThat(body, containsString("servletPath: /errors"));
        assertThat(body, containsString("requestURI: /app/errors/400"));
        assertThat(body, containsString("ERROR_MESSAGE: Not Found"));
        assertThat(body, containsString("ERROR_CODE: 404"));
        assertThat(body, containsString("ERROR_SERVLET: hello"));
        assertThat(body, containsString("ERROR_REQUEST_URI: /app/hello"));
        assertThat(body, containsString("ERROR_EXCEPTION_TYPE: null"));
        assertThat(body, containsString("ERROR_EXCEPTION: null"));
    }

    @Test
    public void testTriggersExceptionInFilterUsingHeader() throws IOException, InterruptedException
    {
        HttpClient httpClient = HttpClient.newBuilder().build();
        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(server.getURI().resolve("/app/hello"))
            .header("X-Filter-Trigger", "RuntimeException")
            .GET().build();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertThat(httpResponse.statusCode(), is(500));
        String body = httpResponse.body();
        assertThat(body, containsString("DISPATCH: ERROR"));
        assertThat(body, containsString("contextPath: /app"));
        assertThat(body, containsString("servletPath: /errors"));
        assertThat(body, containsString("requestURI: /app/errors/500"));
        assertThat(body, containsString("ERROR_MESSAGE: java.lang.RuntimeException: Error from examples.TriggersFilter"));
        assertThat(body, containsString("ERROR_CODE: 500"));
        assertThat(body, containsString("ERROR_SERVLET: hello"));
        assertThat(body, containsString("ERROR_REQUEST_URI: /app/hello"));
        assertThat(body, containsString("ERROR_EXCEPTION_TYPE: class java.lang.RuntimeException"));
        assertThat(body, containsString("ERROR_EXCEPTION: Error from examples.TriggersFilter"));
        assertThat(body, containsString("at examples.TriggersFilter.doFilter("));
    }

    @Test
    public void testTriggers403InFilterUsingQuery() throws IOException, InterruptedException
    {
        HttpClient httpClient = HttpClient.newBuilder().build();
        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(server.getURI().resolve("/app/hello?trigger=403"))
            .GET().build();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertThat(httpResponse.statusCode(), is(403));
        String body = httpResponse.body();
        assertThat(body, containsString("<title>Error Handling WebApp - FORBIDDEN</title>"));
        assertThat(body, containsString("<h1>Error Handling WebApp - FORBIDDEN</h1>"));
    }

    @Test
    public void testTriggers404InFilterUsingQuery() throws IOException, InterruptedException
    {
        HttpClient httpClient = HttpClient.newBuilder().build();
        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(server.getURI().resolve("/app/hello?trigger=404"))
            .GET().build();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertThat(httpResponse.statusCode(), is(404));
        String body = httpResponse.body();
        assertThat(body, containsString("DISPATCH: ERROR"));
        assertThat(body, containsString("contextPath: /app"));
        assertThat(body, containsString("servletPath: /errors"));
        assertThat(body, containsString("requestURI: /app/errors/400"));
        assertThat(body, containsString("ERROR_MESSAGE: Not Found"));
        assertThat(body, containsString("ERROR_CODE: 404"));
        assertThat(body, containsString("ERROR_SERVLET: hello"));
        assertThat(body, containsString("ERROR_REQUEST_URI: /app/hello"));
        assertThat(body, containsString("ERROR_EXCEPTION_TYPE: null"));
        assertThat(body, containsString("ERROR_EXCEPTION: null"));
    }

    @Test
    public void testTriggersExceptionInFilterUsingQuery() throws IOException, InterruptedException
    {
        HttpClient httpClient = HttpClient.newBuilder().build();
        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(server.getURI().resolve("/app/hello?trigger=RuntimeException"))
            .GET().build();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertThat(httpResponse.statusCode(), is(500));
        String body = httpResponse.body();
        assertThat(body, containsString("DISPATCH: ERROR"));
        assertThat(body, containsString("contextPath: /app"));
        assertThat(body, containsString("servletPath: /errors"));
        assertThat(body, containsString("requestURI: /app/errors/500"));
        assertThat(body, containsString("ERROR_MESSAGE: java.lang.RuntimeException: Error from examples.TriggersFilter"));
        assertThat(body, containsString("ERROR_CODE: 500"));
        assertThat(body, containsString("ERROR_SERVLET: hello"));
        assertThat(body, containsString("ERROR_REQUEST_URI: /app/hello"));
        assertThat(body, containsString("ERROR_EXCEPTION_TYPE: class java.lang.RuntimeException"));
        assertThat(body, containsString("ERROR_EXCEPTION: Error from examples.TriggersFilter"));
        assertThat(body, containsString("at examples.TriggersFilter.doFilter("));
    }
}
