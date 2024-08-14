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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.eclipse.jetty.rewrite.handler.Rule;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.Callback;

public class MovedPermanentlyRuleExample
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
        MovedPermanentlyRule movedRule = new MovedPermanentlyRule();
        movedRule.setRegex("http://www.example.org/dump/.*");
        movedRule.setReplacement("https://api.example.org/dump/");
        rewriteHandler.addRule(movedRule);

        // Setup handler
        DumpHandler dumpHandler = new DumpHandler();
        handlers.addHandler(dumpHandler);

        return server;
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
