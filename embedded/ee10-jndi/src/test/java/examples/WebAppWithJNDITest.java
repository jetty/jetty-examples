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
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.ee10.webapp.WebAppContext;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.util.component.Environment;
import org.eclipse.jetty.util.component.LifeCycle;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class WebAppWithJNDITest
{
    private static Server server;
    private static WebAppContext context;

    public static Map<String, Object> collectTreeMap(Context ctx) throws NamingException
    {
        HashMap<String, Object> map = new HashMap<>();
        String namespace = ctx instanceof InitialContext ? ctx.getNameInNamespace() : "";
        for (NameClassPair pair : Collections.list(ctx.list(namespace)))
        {
            String name = pair.getName();
            String jndiPath = namespace + name;
            map.put(name, lookupJndiValue(ctx, jndiPath));
        }

        return map;
    }

    private static void dumpContext(PrintStream out, InitialContext initialContext) throws NamingException
    {
        out.printf("-- InitialContext namespace:\"%s\" --%n", initialContext.getNameInNamespace());
        out.println(toJson(initialContext));
        out.println();
    }

    public static void dumpJndi(PrintStream out) throws NamingException
    {
        InitialContext initialContext = new InitialContext();

        List<String> paths = List.of("val/foo", "entry/foo");
        List<String> prefixes = List.of(
            "",
            "ee10/",
            "java:comp/env/",
            "java:comp/env/ee10/");

        for (String prefix : prefixes)
        {
            for (String path : paths)
            {
                try
                {
                    Integer val = (Integer)initialContext.lookup(prefix + path);
                    out.printf("lookup(\"%s%s\") = %s%n", prefix, path, val);
                }
                catch (NameNotFoundException e)
                {
                    out.printf("lookup(\"%s%s\") = NameNotFound: %s%n", prefix, path, e.getMessage());
                }
            }
        }
    }

    private static Object lookupJndiValue(Context ctx, String path)
    {
        try
        {
            Object tmp = ctx.lookup(path);
            if (tmp instanceof Context tmpCtx)
                return collectTreeMap(tmpCtx);
            else
                return tmp.toString();
        }
        catch (Throwable t)
        {
            return t.getMessage();
        }
    }

    @BeforeAll
    public static void startServer() throws Exception
    {
        server = new Server(0); // let os/jvm pick a port

        context = new WebAppContext();
        context.setContextPath("/");
        // This directory only has WEB-INF/web.xml
        context.setBaseResourceAsPath(Paths.get("src/main/webroots/jndi-root"));
        context.addServlet(JndiDumpServlet.class, "/jndi-dump");

        Environment environment = Environment.ensure("ee10");

        new org.eclipse.jetty.plus.jndi.Resource(environment, "val/foo", 606);
        new org.eclipse.jetty.plus.jndi.Resource(null, "val/foo", 707);
        new org.eclipse.jetty.plus.jndi.Resource(server, "val/foo", 808);
        new org.eclipse.jetty.plus.jndi.Resource(context, "val/foo", 909);

        new org.eclipse.jetty.plus.jndi.EnvEntry(environment, "entry/foo", 330, false);
        new org.eclipse.jetty.plus.jndi.EnvEntry(null, "entry/foo", 440, false);
        new org.eclipse.jetty.plus.jndi.EnvEntry(server, "entry/foo", 550, false);
        new org.eclipse.jetty.plus.jndi.EnvEntry(context, "entry/foo", 660, false);

        server.setHandler(context);
        server.start();
    }

    @AfterAll
    public static void stopServer()
    {
        LifeCycle.stop(server);
    }

    public static String toJson(Context ctx) throws NamingException
    {
        Map<String, Object> map = collectTreeMap(ctx);
        Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();
        return gson.toJson(map);
    }

    @Test
    public void testLookup() throws NamingException, IOException
    {
        InitialContext initialContext = new InitialContext();
        dumpContext(System.out, initialContext);

        System.out.println("-- Dump from WebApp Scope");
        HttpURLConnection http = (HttpURLConnection)server.getURI().resolve("/jndi-dump").toURL().openConnection();
        try (InputStream in = http.getInputStream())
        {
            String body = IO.toString(in, StandardCharsets.UTF_8);
            System.out.println(body);
        }

        System.out.println("-- Dump from Test scope");
        dumpJndi(System.out);
    }

    public static class JndiDumpServlet extends HttpServlet
    {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
        {
            resp.setCharacterEncoding("utf-8");
            resp.setContentType("text/plain");
            PrintStream out = new PrintStream(resp.getOutputStream(), false, StandardCharsets.UTF_8);
            try
            {
                dumpJndi(out);
            }
            catch (NamingException e)
            {
                throw new ServletException(e);
            }
        }
    }
}
