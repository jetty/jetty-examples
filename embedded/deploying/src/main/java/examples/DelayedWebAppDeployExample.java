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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Demonstrate an Embedded Jetty server where the server
 * is available to answer requests immediately, but only return HTTP status code 503
 * until the webapps themselves are available to respond to requests.
 */
public class DelayedWebAppDeployExample
{
    public static void main(String[] args) throws Exception
    {
        Server server = new Server(8080);

        // where all webapps will be deployed to.
        ContextHandlerCollection contexts = new ContextHandlerCollection();

        // The server handler list
        HandlerList handlers = new HandlerList();
        handlers.addHandler(contexts);
        handlers.addHandler(new UnavailableHandler());

        server.setHandler(handlers);
        server.start();

        Path warPath = new File("webapps/hello.war").toPath().toRealPath();
        System.err.println("WAR File is " + warPath);

        server.getThreadPool().execute(new DelayedDeploymentTask(contexts, warPath, "/"));

        server.join();
    }

    public static class UnavailableHandler extends AbstractHandler
    {
        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
        {
            // Indicate a 503 status
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            // tell jetty that this request was handled (making others handlers not run for this request)
            baseRequest.setHandled(true);
        }
    }

    public static class DelayedDeploymentTask implements Runnable
    {
        private final ContextHandlerCollection contexts;
        private final Path warPath;
        private final String contextPath;

        public DelayedDeploymentTask(ContextHandlerCollection contexts, Path warPath, String contextPath)
        {
            this.contexts = contexts;
            this.warPath = warPath;
            this.contextPath = contextPath;
        }

        @Override
        public void run()
        {
            try
            {
                WebAppContext webapp = new WebAppContext();
                webapp.setContextPath(contextPath);
                webapp.setWar(warPath.toUri().toASCIIString());

                // Simulate a slow to initialize listener - this is for demo purposes only
                webapp.addEventListener(new SlowInitListener());

                contexts.addHandler(webapp);
                webapp.start();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public static class SlowInitListener implements ServletContextListener
    {
        @Override
        public void contextInitialized(ServletContextEvent sce)
        {
            try
            {
                // simulate a listener that takes a while to initialize
                // this should give the server some time to reply with 503
                // until such time that this listener completes and the
                // rest of the webapp can finish its context init successfully.
                TimeUnit.SECONDS.sleep(10);
                System.out.printf("%s is done, try loading the %s webapp now%n", this.getClass().getName(), sce.getServletContext().getContextPath());
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        @Override
        public void contextDestroyed(ServletContextEvent sce)
        {

        }
    }
}
