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

import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Path;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.toolchain.test.FS;
import org.eclipse.jetty.toolchain.test.MavenTestingUtils;
import org.eclipse.jetty.util.component.LifeCycle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ServletFileServerMultipleLocationsTest
{
    private final long exampleSize = 2 * StaticFileGen.MB;
    private final long largeSize = 2 * StaticFileGen.GB;
    private Server server;
    private String exampleSha;
    private String largeSha;

    @BeforeEach
    public void startServer() throws Exception
    {
        Path resourcesRoot = MavenTestingUtils.getTargetTestingPath(ServletFileServerMultipleLocations.class.getSimpleName());
        FS.ensureDirExists(resourcesRoot);

        exampleSha = StaticFileGen.generate(resourcesRoot.resolve("example.png"), exampleSize);
        largeSha = StaticFileGen.generate(resourcesRoot.resolve("large.mkv"), largeSize);

        URI defaultBaseResource = ServletFileServerMultipleLocations.findDefaultBaseResource();

        server = ServletFileServerMultipleLocations.newServer(0, defaultBaseResource, resourcesRoot);
        server.start();
    }

    @AfterEach
    public void stopServer()
    {
        LifeCycle.stop(server);
    }

    /**
     * Get small file
     */
    @Test
    public void testGetSmall() throws Exception
    {
        HttpURLConnection http = (HttpURLConnection)server.getURI().resolve("/alt/example.png").toURL().openConnection();
        http.connect();
        dumpRequestResponse(http);
        assertEquals(HttpURLConnection.HTTP_OK, http.getResponseCode());
        String contentLengthResponse = http.getHeaderField("Content-Length");
        assertNotNull(contentLengthResponse);
        long contentLengthLong = Long.parseLong(contentLengthResponse);
        Assertions.assertEquals(2 * StaticFileGen.MB, contentLengthLong);
        assertEquals("image/png", http.getHeaderField("Content-Type"));

        StaticFileGen.verify(http.getInputStream(), exampleSize, exampleSha);
    }

    /**
     * Get large file
     */
    @Test
    public void testGetLarge() throws Exception
    {
        HttpURLConnection http = (HttpURLConnection)server.getURI().resolve("/alt/large.mkv").toURL().openConnection();
        http.connect();
        dumpRequestResponse(http);
        assertEquals(HttpURLConnection.HTTP_OK, http.getResponseCode());
        String contentLengthResponse = http.getHeaderField("Content-Length");
        assertNotNull(contentLengthResponse);
        long contentLengthLong = Long.parseLong(contentLengthResponse);
        Assertions.assertEquals(2 * StaticFileGen.GB, contentLengthLong);
        assertNull(http.getHeaderField("Content-Type"), "Not a recognized mime-type by Jetty");

        StaticFileGen.verify(http.getInputStream(), largeSize, largeSha);
    }

    private static void dumpRequestResponse(HttpURLConnection http)
    {
        System.out.println();
        System.out.println("----");
        System.out.printf("%s %s HTTP/1.1%n", http.getRequestMethod(), http.getURL());
        System.out.println("----");
        System.out.printf("%s%n", http.getHeaderField(null));
        http.getHeaderFields().entrySet().stream()
            .filter(entry -> entry.getKey() != null)
            .forEach((entry) -> System.out.printf("%s: %s%n", entry.getKey(), http.getHeaderField(entry.getKey())));
    }
}
