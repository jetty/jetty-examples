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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DemoServlet extends HttpServlet
{
    private static final Logger LOG = LoggerFactory.getLogger(DemoServlet.class);

    @Override
    public void init() throws ServletException
    {
        super.init();
        LOG.info("Initializing the WebApp");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        LOG.info("This is from the DemoServlet.doGet()");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/plain");

        PrintWriter out = resp.getWriter();
        out.println("in doGet()");
        out.printf("The logger is a %s%n", LOG.getClass().getName());
    }
}
