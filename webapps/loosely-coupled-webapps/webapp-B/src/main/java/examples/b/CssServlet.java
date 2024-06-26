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

package examples.b;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CssServlet extends HttpServlet
{
    private static final Logger LOG = LoggerFactory.getLogger(CssServlet.class);

    private byte[] rawCss;

    @Override
    public void init() throws ServletException
    {
        LOG.debug("Init (in WebApp B)");
        String cssURLRoot = getInitParameter("cssURLRoot");
        if (cssURLRoot == null)
            throw new IllegalStateException("Unable to find 'cssURLRoot' init-param");
        LOG.debug("init-param[cssURLRoot] = {}", cssURLRoot);
        URI uriCss = URI.create(cssURLRoot).resolve("main.css");
        LOG.debug("uriCss is {}", uriCss);

        // Fetch CSS details from WebApp A
        try
        {
            HttpURLConnection http = (HttpURLConnection)uriCss.toURL().openConnection();
            http.setConnectTimeout(2000);
            if (http.getResponseCode() != 200)
                throw new ServletException("Error: Response code [" + http.getResponseCode() + "] on GET of " + uriCss);
            try (InputStream stream = http.getInputStream();
                 ByteArrayOutputStream out = new ByteArrayOutputStream())
            {
                byte[] buf = new byte[8096];
                int len;
                while ((len = stream.read(buf)) != -1)
                {
                    out.write(buf, 0, len);
                }
                rawCss = out.toByteArray();
            }
            LOG.info("GET of rawCss from {} complete: size {}", uriCss, rawCss.length);
        }
        catch (IOException e)
        {
            throw new ServletException("Unable to get css: " + uriCss, e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        resp.setContentType("text/css");
        resp.setCharacterEncoding("utf-8");
        OutputStream out = resp.getOutputStream();
        out.write(rawCss);
    }
}
