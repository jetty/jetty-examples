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

package com.acme;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

public class TestFilter implements Filter
{
    private ServletContext _context;

    public void init(FilterConfig filterConfig) throws ServletException
    {
        _context = filterConfig.getServletContext();
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException
    {
        String user = ((HttpServletRequest)request).getRemoteUser();
        String url = ((HttpServletRequest)request).getRequestURL().toString();
        System.err.println("TestFilter: Request.url = " + url + ", Request.getRemoteUser = " + user);

        chain.doFilter(request, response);
    }

    public void destroy()
    {
    }
}
