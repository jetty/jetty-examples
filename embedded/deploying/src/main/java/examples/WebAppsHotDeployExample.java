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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.deploy.App;
import org.eclipse.jetty.deploy.AppLifeCycle;
import org.eclipse.jetty.deploy.DeploymentManager;
import org.eclipse.jetty.deploy.graph.Node;
import org.eclipse.jetty.deploy.providers.ContextProvider;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.DefaultHandler;

public class WebAppsHotDeployExample
{
    public static void main(String[] args) throws Exception
    {
        Server server = new Server(8080);

        ContextHandlerCollection contexts = new ContextHandlerCollection();
        Handler.Sequence handlers = new Handler.Sequence();
        handlers.addHandler(contexts);
        handlers.addHandler(new DefaultHandler());

        server.setHandler(handlers);

        Path confFile = Paths.get(System.getProperty("user.dir"), "example.conf");

        ContextAttributeCustomizer contextAttributeCustomizer = new ContextAttributeCustomizer();
        contextAttributeCustomizer.setAttribute("common.conf", confFile);

        DeploymentManager deploymentManager = new DeploymentManager();
        deploymentManager.setContexts(contexts);
        deploymentManager.addLifeCycleBinding(contextAttributeCustomizer);

        String jettyBaseProp = System.getProperty("jetty.base");
        if (jettyBaseProp == null)
        {
            throw new FileNotFoundException("Missing System Property 'jetty.base'");
        }
        Path jettyBase = Path.of(jettyBaseProp).toAbsolutePath();

        ContextProvider webAppProvider = new ContextProvider();
        webAppProvider.setEnvironmentName("ee10");
        webAppProvider.setMonitoredDirName(jettyBase.resolve("webapps").toString());

        deploymentManager.addAppProvider(webAppProvider);
        server.addBean(deploymentManager);

        // Lets dump the server after start.
        // We can look for the deployed contexts, along with an example of the
        // result of ContextAttributesCustomizer in the dump section for "Handler attributes"
        server.setDumpAfterStart(true);
        server.start();
        server.join();
    }

    public static class ContextAttributeCustomizer implements AppLifeCycle.Binding
    {
        public final Map<String, Object> attributes = new HashMap<>();

        public void setAttribute(String name, Object value)
        {
            this.attributes.put(name, value);
        }

        @Override
        public String[] getBindingTargets()
        {
            return new String[]{AppLifeCycle.DEPLOYING};
        }

        @Override
        public void processBinding(Node node, App app) throws Exception
        {
            ContextHandler handler = app.getContextHandler();
            if (handler == null)
            {
                throw new NullPointerException("No Handler created for App: " + app);
            }
            attributes.forEach((name, value) -> handler.setAttribute(name, value));
        }
    }
}
