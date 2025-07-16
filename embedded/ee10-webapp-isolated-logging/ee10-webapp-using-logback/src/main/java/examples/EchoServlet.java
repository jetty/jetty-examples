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
import java.util.Collections;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet(urlPatterns = "/echo/*")
public class EchoServlet extends HttpServlet
{
    private static final Logger LOG = LoggerFactory.getLogger(EchoServlet.class);

    @Override
    public void init() throws ServletException
    {
        LOG.info("init()");
        super.init();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        LOG.info("doGet() - {}", req.getRequestURL());

        resp.setStatus(200);
        PrintWriter out = resp.getWriter();
        out.printf("GET of %s%n", req.getRequestURL());
        for (String headerName : Collections.list(req.getHeaderNames()))
        {
            List<String> values = Collections.list(req.getHeaders(headerName));
            out.printf("Request Header [%s] = %s%n", headerName,
                String.join(", ", values));
        }
    }
}
