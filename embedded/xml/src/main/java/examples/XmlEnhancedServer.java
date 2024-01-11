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

import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.ee10.servlet.DefaultServlet;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceFactory;
import org.eclipse.jetty.xml.XmlConfiguration;

/**
 * This is a Server setup in a default way, but is can be enhanced by providing
 * (0 to n) Jetty XML file arguments on the command line.
 * <p>
 * Run without a command line argument and pay attention to the contextPath for the
 * default Context. (It should be {@code "/"})
 * <br>
 * Now run with {@code src/test/resources/xml-enhanced/adjust-default-contextpath.xml} and see
 * that the context path is now {@code "/foobar"}
 * </p>
 * <p>
 * Run with {@code src/test/resources/xml-enhanced/configure-http.xml} and you will be
 * adjusting the {@link HttpConfiguration} parameters in use.
 * </p>
 * <p>
 * Run with {@code src/test/resources/xml-enhanced/add-rewrites.xml} and you will be
 * adjusting adding rewrite rules to the existing handler tree.
 * <br>
 * Request {@code http://localhost:8080/bar/blah} and you will receive a 302 redirect
 * to {@code http://localhost:8080/foo}
 * </p>
 * <p>
 * Run with {@code src/test/resources/xml-enhanced/add-https.xml} and you will be
 * adding an HTTPS connector with configuration.
 * </p>
 */
public class XmlEnhancedServer
{
    public static void main(String[] args) throws Exception
    {
        Server server = new Server();

        ResourceFactory resourceFactory = ResourceFactory.of(server);

        HttpConfiguration httpConfig = new HttpConfiguration();
        ServerConnector httpConnector = new ServerConnector(server, new HttpConnectionFactory(httpConfig));
        httpConnector.setPort(8080);
        server.addConnector(httpConnector);

        // Figure out what path to serve content from
        ClassLoader cl = XmlEnhancedServer.class.getClassLoader();
        // We look for a file, as ClassLoader.getResource() is not
        // designed to look for directories (we resolve the directory later)
        URL f = cl.getResource("static-root/hello.html");
        if (f == null)
        {
            throw new RuntimeException("Unable to find resource directory");
        }

        // Resolve file to directory
        URI webRootUri = f.toURI().resolve("./").normalize();
        System.err.println("WebRoot is " + webRootUri);

        Handler.Sequence handlers = new Handler.Sequence();
        ContextHandlerCollection contexts = new ContextHandlerCollection();

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.setBaseResource(resourceFactory.newResource(webRootUri));
        contexts.addHandler(context);

        ServletHolder holderPwd = new ServletHolder("default", DefaultServlet.class);
        holderPwd.setInitParameter("dirAllowed", "true");
        context.addServlet(holderPwd, "/");

        handlers.addHandler(contexts);
        DefaultHandler defaultHandler = new DefaultHandler();
        handlers.addHandler(defaultHandler); // always last in handler list

        server.setHandler(handlers);

        // apply extra XML (provided on command line)
        if (args.length > 0)
        {
            // Map some well known objects via an id that can be referenced in the XMLs
            Map<String, Object> idMap = new HashMap<>();
            idMap.put("Server", server);
            idMap.put("httpConfig", httpConfig);
            idMap.put("httpConnector", httpConnector);
            idMap.put("Handlers", handlers);
            idMap.put("Contexts", contexts);
            idMap.put("Context", context);
            idMap.put("DefaultHandler", defaultHandler);

            // Map some well known properties
            Map<String, String> globalProps = new HashMap<>();
            URI resourcesUriBase = webRootUri.resolve("..");
            System.err.println("ResourcesUriBase is " + resourcesUriBase);
            globalProps.put("resources.location", resourcesUriBase.toASCIIString());

            XmlConfiguration lastConfig = null;
            for (String xml : args)
            {
                Path xmlPath = Paths.get(xml);
                System.err.println("Applying XML: " + xmlPath);
                Resource xmlResource = resourceFactory.newResource(xmlPath);
                XmlConfiguration configuration = new XmlConfiguration(xmlResource);
                if (lastConfig != null)
                    configuration.getIdMap().putAll(lastConfig.getIdMap());
                configuration.getProperties().putAll(globalProps);
                configuration.getIdMap().putAll(idMap);
                idMap.putAll(configuration.getIdMap());
                lastConfig = configuration;
            }

            // Dump the resulting idMap
            idMap.forEach((id, obj) -> System.err.printf("IdMap[%s]: (%s)%n", id, obj.getClass().getName()));
        }

        server.setDumpAfterStart(true);
        server.start();
        server.join();
    }
}
