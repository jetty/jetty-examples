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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.http.pathmap.PathSpec;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.PathMappingsHandler;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceFactory;
import org.eclipse.jetty.util.resource.Resources;

public class PathMappingServer
{
    public static void main(String[] args) throws Exception
    {
        Server server = PathMappingServer.newServer(8080);
        server.start();
        server.join();
    }

    public static Server newServer(int port) throws IOException
    {
        Server server = new Server(port);

        ResourceFactory resourceFactory = ResourceFactory.of(server);

        PathMappingsHandler pathMappingsHandler = new PathMappingsHandler();

        Resource rootResourceDir = resourceFactory.newClassLoaderResource("/static-root/");
        if (!Resources.isReadableDirectory(rootResourceDir))
            throw new FileNotFoundException("Unable to find /static-root/ classloader directory");

        ResourceHandler rootResourceHandler = new ResourceHandler();
        rootResourceHandler.setBaseResource(rootResourceDir);
        rootResourceHandler.setDirAllowed(false);
        rootResourceHandler.setWelcomeFiles("index.html");

        Path extrasDir = Path.of("extras").toAbsolutePath();
        if (!Files.isDirectory(extrasDir))
            throw new FileNotFoundException("Unable to find /extras/ directory on disk: " + extrasDir);
        if (!Files.isReadable(extrasDir))
            throw new FileNotFoundException("Unable to read directory (permissions?): " + extrasDir);

        Resource extraResourceDir = resourceFactory.newResource(extrasDir);

        ResourceHandler extraResourceHandler = new ResourceHandler();
        extraResourceHandler.setBaseResource(extraResourceDir);
        extraResourceHandler.setDirAllowed(true);

        Resource metaInfResource = findMetaInfResources(resourceFactory, PathMappingServer.class.getClassLoader());

        ResourceHandler metaInfResourceHandler = new ResourceHandler();
        metaInfResourceHandler.setBaseResource(metaInfResource);
        metaInfResourceHandler.setDirAllowed(false);

        // The context-path is the portion of the path that doesn't belong to the filename.
        // Example:
        //   You have this setup.
        //   * You have files you want to serve in "/home/user/webroot/"
        //   * You have that directory mapped against the path-spec of "/content/*"
        //
        //   Then a request arrives against the path "/content/foo.txt"
        //   The server will send that request to the mapped location of "/content/*"
        //   which is your ResourceHandler serving content from "/home/user/webroot/"
        //   The looked for file will be <base-dir> + "/" + <request-path>
        //   which turns out to be "/home/user/webroot/content/foo.txt"
        //
        //   The PathNameWrapper below allows you to map the incoming request path
        //   to a simple path name that the ResourceHandler of your choice can use
        //   against it's base resource.

        pathMappingsHandler.addMapping(PathSpec.from("/"), rootResourceHandler);
        pathMappingsHandler.addMapping(PathSpec.from("/extras/*"),
            new StripContextPath("/extras", extraResourceHandler));
        pathMappingsHandler.addMapping(PathSpec.from("/jars/*"),
            new StripContextPath("/jars", metaInfResourceHandler));
        // Example of a mapping to an extension.
        pathMappingsHandler.addMapping(PathSpec.from("*.png"),
            new PathNameWrapper((path) ->
            {
                int idx = path.lastIndexOf("/");
                if (idx >= 0)
                    return "/images" + path.substring(idx);
                return "/images/" + path;
            }, extraResourceHandler));
        pathMappingsHandler.addMapping(PathSpec.from("/hello/*"), new HelloHandler("Mappings"));

        server.setHandler(pathMappingsHandler);
        return server;
    }

    private static Resource findMetaInfResources(ResourceFactory resourceFactory, ClassLoader classLoader) throws IOException
    {
        List<URL> hits = Collections.list(classLoader.getResources("META-INF/resources"));
        List<Resource> resources = new ArrayList<>();
        for (URL hit : hits)
        {
            Resource resource = resourceFactory.newResource(hit);
            if (Resources.isReadableDirectory(resource))
                resources.add(resource);
        }

        if (resources.isEmpty())
            throw new FileNotFoundException("Classloader Configuration error: No META-INF/resources entries found");

        return ResourceFactory.combine(resources);
    }

    private static class StripContextPath extends PathNameWrapper
    {
        public StripContextPath(String contextPath, Handler handler)
        {
            super((path) ->
            {
                if (path.startsWith(contextPath))
                    return path.substring(contextPath.length());
                return path;
            }, handler);
        }
    }

    private static class PathNameWrapper extends Handler.Wrapper
    {
        private final Function<String, String> nameFunction;

        public PathNameWrapper(Function<String, String> nameFunction, Handler handler)
        {
            super(handler);
            this.nameFunction = nameFunction;
        }

        @Override
        public boolean handle(Request request, Response response, Callback callback) throws Exception
        {
            String originalPath = request.getHttpURI().getPath();

            String newPath = nameFunction.apply(originalPath);

            HttpURI newURI = HttpURI.build(request.getHttpURI()).path(newPath);

            Request wrappedRequest = new Request.Wrapper(request)
            {
                @Override
                public HttpURI getHttpURI()
                {
                    return newURI;
                }
            };

            return super.handle(wrappedRequest, response, callback);
        }
    }
}
