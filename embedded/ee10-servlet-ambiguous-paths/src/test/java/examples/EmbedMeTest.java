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
import java.net.URLDecoder;

import org.eclipse.jetty.http.HttpTester;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.LifeCycle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

public class EmbedMeTest
{
    private Server server;

    @BeforeEach
    public void startServer() throws Exception
    {
        server = EmbedMe.newServer(0);
        server.start();
    }

    @AfterEach
    public void stopServer()
    {
        LifeCycle.stop(server);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "/git/branch/fix%2Frefactor"
    })
    public void testAmbiguousPathSeparator(String path) throws IOException
    {
        URI uri = server.getURI();

        String rawRequest = """
            GET @PATH@ HTTP/1.1
            Host: @AUTHORITY@
            Connection: close
            
            """.replace("@PATH@", path).replace("@AUTHORITY@", uri.getAuthority());

        // Using raw sockets to ensure that we send exactly what we want.
        // Using a proper HttpClient can often lead to the path being encoded/decoded in ways
        // that this test case isn't wanting.
        try (Socket socket = new Socket(uri.getHost(), uri.getPort());
             OutputStream out = socket.getOutputStream();
             InputStream in = socket.getInputStream())
        {
            out.write(rawRequest.getBytes(UTF_8));
            out.flush();

            HttpTester.Response response = HttpTester.parseResponse(in);
            assertThat(response.getStatus(), is(200));
            String responseContent = response.getContent();
            assertThat(responseContent, containsString("requestURI=%s%n".formatted(path)));
            assertThat(responseContent, containsString("servletPath=%n".formatted()));
            String decodedPath = URLDecoder.decode(path, UTF_8);
            assertThat(responseContent, containsString("pathInfo=%s%n".formatted(decodedPath)));
        }
    }
}
