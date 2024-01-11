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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.HttpTester;
import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.eclipse.jetty.rewrite.handler.Rule;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.resource.ResourceFactory;

public class MovedPermanentlyRuleExample
{
    public static void main(String[] args) throws Exception
    {
        MovedPermanentlyRuleExample example = new MovedPermanentlyRuleExample();
        try
        {
            int httpPort = 8080;
            example.startServer(httpPort);
            URI serverURI = example.getServerURI();

            /* Issue an HTTP http://www.example.org/dump/.
             * Expecting to see a 301 Moved Permanently redirect with a `Location`
             * response header containing the new location of
             * https://api.example.org/dump/
             */
            String rawRequest = "GET /dump/ HTTP/1.1\r\n" +
                "Host: www.example.org\r\n" +
                "Connection: close\r\n" +
                "\r\n";

            example.testRequest(serverURI.getHost(), serverURI.getPort(), rawRequest);
        }
        finally
        {
            example.stopServer();
        }
    }

    private Server server;

    public void startServer(int port) throws Exception
    {
        server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        server.addConnector(connector);

        Handler.Sequence handlers = new Handler.Sequence();
        server.setHandler(handlers);

        // Add Rewrite / Redirect handlers + Rules
        RewriteHandler rewriteHandler = new RewriteHandler();
        MovedPermanentlyRule movedRule = new MovedPermanentlyRule();
        movedRule.setRegex("http://www.example.org/dump/.*");
        movedRule.setReplacement("https://api.example.org/dump/");
        rewriteHandler.addRule(movedRule);
        handlers.addHandler(rewriteHandler);

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

        server.start();
    }

    public void stopServer()
    {
        LifeCycle.stop(server);
    }

    public URI getServerURI()
    {
        return server.getURI();
    }

    private void testRequest(String host, int port, String rawRequest) throws IOException
    {
        try (Socket client = new Socket(host, port))
        {
            OutputStream out = client.getOutputStream();
            out.write(rawRequest.getBytes(StandardCharsets.UTF_8));
            out.flush();

            InputStream in = client.getInputStream();
            HttpTester.Response response = HttpTester.parseResponse(in);

            System.out.println(response);
            System.out.println(response.getContent());
        }
    }

    public static class MovedPermanentlyRule extends Rule
    {
        private Pattern regex;
        private String replacement;

        public MovedPermanentlyRule()
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

        public String getReplacement()
        {
            return replacement;
        }

        public void setReplacement(String replacement)
        {
            this.replacement = replacement;
        }

        @Override
        public Handler matchAndApply(Handler input)
        {
            Matcher matcher = regex.matcher(input.getHttpURI().toString());
            boolean matches = matcher.matches();
            if (matches)
            {
                return new Handler(input)
                {
                    @Override
                    protected boolean handle(Response response, Callback callback)
                    {
                        String location = Response.toRedirectURI(input, replacement);
                        response.setStatus(HttpStatus.MOVED_TEMPORARILY_302);
                        response.getHeaders().put(HttpHeader.LOCATION, location);
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
