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
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.resource.Resource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MetaInfResourceDemoTest
{
    public static Server server;

    public static URI serverBaseURI;

    @BeforeAll
    public static void initServer() throws Exception
    {
        server = MetaInfResourceDemo.newServer(0);
        server.start();
        serverBaseURI = server.getURI().resolve("/");
    }

    @AfterAll
    public static void stopServer()
    {
        LifeCycle.stop(server);
    }

    @Test
    public void testGetMyReadmeResource() throws Exception
    {
        HttpURLConnection http = (HttpURLConnection)serverBaseURI.resolve("/MYREADME.txt").toURL().openConnection();
        http.connect();
        dumpRequestResponse(http);
        assertEquals(HttpURLConnection.HTTP_OK, http.getResponseCode());
        assertEquals("text/plain", http.getHeaderField("Content-Type"));
    }

    @Test
    public void testGetBootStrapResource() throws Exception
    {
        String bootstrapFile = findMetaInfResourceFile(MetaInfResourceDemoTest.class.getClassLoader(), "/webjars/bootstrap/", "bootstrap\\.css");

        HttpURLConnection http = (HttpURLConnection)serverBaseURI.resolve(bootstrapFile).toURL().openConnection();
        http.connect();
        dumpRequestResponse(http);
        assertEquals(HttpURLConnection.HTTP_OK, http.getResponseCode());
        assertEquals("text/css", http.getHeaderField("Content-Type"));
    }

    /**
     * Find the actual version in the jar files, so we don't have to hardcode the version in the testcases.
     *
     * @param classLoader the classloader to look in
     * @param prefix the prefix webjar to look for.
     * @param regex the regex to match the first hit against.
     * @return the found resource
     */
    private String findMetaInfResourceFile(ClassLoader classLoader, String prefix, String regex) throws IOException
    {
        List<URL> hits = Collections.list(classLoader.getResources("META-INF/resources" + prefix));
        for (URL hit : hits)
        {
            try (Resource res = Resource.newResource(hit))
            {
                Resource match = findNestedResource(res, regex);
                if (match != null)
                {
                    String rawpath = match.toString();
                    int idx;

                    // use only part after `!/`
                    idx = rawpath.lastIndexOf("!/");
                    if (idx >= 0)
                        rawpath = rawpath.substring(idx + 2);

                    // find substring starting at prefix
                    idx = rawpath.indexOf(prefix);
                    if (idx >= 0)
                        return rawpath.substring(idx);
                    return rawpath;
                }
            }
        }
        throw new RuntimeException("Unable to find resource [" + regex + "] in " + prefix);
    }

    private Resource findNestedResource(Resource res, String regex) throws IOException
    {
        for (String content : res.list())
        {
            Resource subresource = res.addPath(content);
            if (content.matches(regex))
                return subresource;
            if (subresource.isDirectory())
            {
                Resource nested = findNestedResource(subresource, regex);
                if (nested != null)
                    return nested;
            }
        }
        return null;
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
