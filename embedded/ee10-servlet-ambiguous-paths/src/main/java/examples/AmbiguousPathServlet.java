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
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AmbiguousPathServlet extends HttpServlet
{
    private final AtomicInteger count = new AtomicInteger();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        String requestURI = request.getRequestURI();
        String servletPath;
        String pathInfo;
        try
        {
            servletPath = request.getServletPath();
        }
        catch (IllegalArgumentException iae)
        {
            servletPath = iae.toString();
        }
        try
        {
            pathInfo = request.getPathInfo();
        }
        catch (IllegalArgumentException iae)
        {
            pathInfo = iae.toString();
        }

        PrintWriter out = response.getWriter();
        out.printf("request.count=%d%n", count.incrementAndGet());
        out.printf("requestURI=%s%n", requestURI);
        out.printf("servletPath=%s%n", servletPath);
        out.printf("pathInfo=%s%n", pathInfo);
    }
}
