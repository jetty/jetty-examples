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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.eclipse.jetty.rewrite.handler.Rule;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.resource.ResourceFactory;

public class RejectUserAgentRuleExample
{
    public static void main(String[] args) throws Exception
    {
        Server server = newServer(8080);
        server.start();
        server.join();
    }

    public static Server newServer(int port) throws Exception
    {
        Server server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        server.addConnector(connector);

        Handler.Sequence handlers = new Handler.Sequence();
        server.setHandler(handlers);

        // Add Rewrite Handler
        RewriteHandler rewriteHandler = new RewriteHandler();
        handlers.addHandler(rewriteHandler);

        // Add rules for Rewrite Handler
        RejectUserAgentRule rule = new RejectUserAgentRule();
        rule.setRegex(".*Robot.*");
        rule.setStatusCode(HttpStatus.UNAUTHORIZED_401);
        rewriteHandler.addRule(rule);

        // Setup context
        Path webRootPath = Paths.get("webapps/alt-root/").toAbsolutePath().normalize();
        if (!Files.isDirectory(webRootPath))
        {
            System.err.println("ERROR: Unable to find " + webRootPath + ".");
            System.exit(-1);
        }

        ServletContextHandler context = new ServletContextHandler();
        handlers.addHandler(context);
        context.setContextPath("/");
        context.setBaseResource(ResourceFactory.of(context).newResource(webRootPath));
        context.setWelcomeFiles(new String[]{"index.html"});
        context.addServlet(DumpServlet.class, "/dump/*");

        return server;
    }

    public static class RejectUserAgentRule extends Rule
    {
        private Pattern regex;
        private int statusCode;

        public RejectUserAgentRule()
        {
            setTerminating(true);
        }

        public String getRegex()
        {
            return regex == null ? null : regex.pattern();
        }

        public void setRegex(String regex)
        {
            this.regex = Pattern.compile(regex);
        }

        public int getStatusCode()
        {
            return statusCode;
        }

        public void setStatusCode(int statusCode)
        {
            this.statusCode = statusCode;
        }

        @Override
        public Handler matchAndApply(Handler input)
        {
            String userAgent = input.getHeaders().get(HttpHeader.USER_AGENT);
            Matcher matcher = regex.matcher(userAgent);
            boolean matches = matcher.matches();
            if (matches)
            {
                return new Handler(input)
                {
                    @Override
                    protected boolean handle(Response response, Callback callback)
                    {
                        response.setStatus(getStatusCode());
                        callback.succeeded();
                        return true;
                    }
                };
            }
            return null;
        }

        @Override
        public String toString()
        {
            return super.toString() + "[" + regex + "]";
        }
    }
}
