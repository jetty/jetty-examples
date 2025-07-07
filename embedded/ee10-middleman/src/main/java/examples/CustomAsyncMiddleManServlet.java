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

import jakarta.servlet.http.HttpServletRequest;
import org.eclipse.jetty.ee10.proxy.AsyncMiddleManServlet;

/**
 * Put custom AsyncMiddleManeServlet behaviors here.
 */
public class CustomAsyncMiddleManServlet extends AsyncMiddleManServlet.Transparent
{
    /**
     * Rewrite of the incoming {@link HttpServletRequest#getRequestURI()} to the destination (proxyTo) URL String.
     *
     * <p>
     *     Default implementation can be found at
     *     {@link org.eclipse.jetty.ee10.proxy.AbstractProxyServlet.TransparentDelegate}.
     *     The default implementation uses {@code URI.create(input).normalize()}
     * </p>
     */
    @Override
    protected String rewriteTarget(HttpServletRequest request)
    {
        return super.rewriteTarget(request);
    }
}
