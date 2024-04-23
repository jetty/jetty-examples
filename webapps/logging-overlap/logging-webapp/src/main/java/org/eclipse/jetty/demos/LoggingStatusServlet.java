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

package org.eclipse.jetty.demos;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.demos.dumps.JavaUtilLoggingDump;
import org.eclipse.jetty.demos.dumps.Log4jDump;
import org.eclipse.jetty.demos.dumps.Slf4jDump;

import static org.eclipse.jetty.demos.Util.toDebugString;

public class LoggingStatusServlet extends HttpServlet
{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/plain");

        PrintWriter out = resp.getWriter();

        out.println("## Key Classes");
        out.println("----------------------------------------------");
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        dumpKeyClassDetail(cl, out, "java.util.logging.Logger");
        dumpKeyClassDetail(cl, out, "org.slf4j.Logger");
        dumpKeyClassDetail(cl, out, "org.slf4j.LoggerFactory");
        dumpKeyClassDetail(cl, out, "org.apache.commons.logging.Log");
        dumpKeyClassDetail(cl, out, "org.apache.commons.logging.LogFactory");
        dumpKeyClassDetail(cl, out, "org.apache.logging.log4j.Logger");
        dumpKeyClassDetail(cl, out, "org.apache.logging.log4j.LogManager");

        out.println();
        out.println("## Active Slf4j Configuration");
        out.println("----------------------------------------------");
        Slf4jDump.dump(out);

        out.println();
        out.println("## Active Log4j Configuration");
        out.println("----------------------------------------------");
        Log4jDump.dump(out);

        out.println();
        out.println("## Active java.util.logging Configuration");
        out.println("----------------------------------------------");
        JavaUtilLoggingDump.dump(out);
    }

    private void dumpKeyClassDetail(ClassLoader cl, PrintWriter out, String className)
    {
        try
        {
            Class<?> clazz = Class.forName(className, false, cl);
            out.printf("Found Class [%s]%n", className);
            ClassLoader loadedFrom = clazz.getClassLoader();
            out.printf("  ClassLoader: %s%n", toDebugString(loadedFrom));
            out.printf("  Location (CodeSource): %s%n", Util.getCodeSourceLocation(clazz));
            out.printf("  Location (ClassLoader/Self) : %s%n", Util.getClassLoaderLocation(clazz, loadedFrom));
            out.printf("  Location (ClassLoader/System) : %s%n", Util.getSystemClassLoaderLocation(clazz));
        }
        catch (ClassNotFoundException e)
        {
            out.printf("Unable to find class [%s]%n", className);
        }
    }
}
