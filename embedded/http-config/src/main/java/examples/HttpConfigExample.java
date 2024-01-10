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
import java.net.URI;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.component.LifeCycle;

public class HttpConfigExample
{
    public static void main(String[] args) throws Exception
    {
        Server server = new Server();

        HttpConfiguration httpConfigOff = new HttpConfiguration();
        httpConfigOff.setSendDateHeader(false);
        httpConfigOff.setSendServerVersion(false);
        httpConfigOff.setSendXPoweredBy(false);

        ServerConnector connectorOff = new ServerConnector(server, new HttpConnectionFactory(httpConfigOff));
        connectorOff.setPort(9090);
        server.addConnector(connectorOff);

        HttpConfiguration httpConfigDefault = new HttpConfiguration();

        ServerConnector connectorDefault = new ServerConnector(server, new HttpConnectionFactory(httpConfigDefault));
        connectorDefault.setPort(9191);
        server.addConnector(connectorDefault);

        server.setHandler(new AbstractHandler()
        {
            @Override
            public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
            {
                response.setCharacterEncoding("utf-8");
                response.setContentType("text/plain");
                response.getWriter().println("Greetings.");
                baseRequest.setHandled(true);
            }
        });

        HttpClient client = new HttpClient();

        try
        {
            client.start();
            server.start();

            // Let's show what the default Response headers look like.
            dumpResponse("Default Response", client.GET(URI.create("http://localhost:9191/")));

            // Let's show what the default Response headers look like.
            dumpResponse("Configured Response", client.GET(URI.create("http://localhost:9090/")));
        }
        finally
        {
            LifeCycle.stop(server);
            LifeCycle.stop(client);
        }
    }

    private static void dumpResponse(String heading, ContentResponse response)
    {
        System.out.printf("--- %s ---%n", heading);
        System.out.printf("Request to %s%n", response.getRequest().getURI());
        System.out.printf("Response: %s %d %s%n", response.getVersion(), response.getStatus(), response.getReason());
        System.out.println(response.getHeaders());
        System.out.println(response.getContentAsString());
    }
}
