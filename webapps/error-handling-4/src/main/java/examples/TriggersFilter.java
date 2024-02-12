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
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TriggersFilter implements Filter
{
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException
    {
        HttpServletRequest request = (HttpServletRequest)req;
        HttpServletResponse response = (HttpServletResponse)resp;

        String trigger = getPossibleTrigger(request);
        if (trigger != null)
        {
            if (trigger.matches("[0-9]+"))
            {
                // trigger is a number, use it as an HTTP status code
                int statusCode = Integer.parseInt(trigger);
                response.sendError(statusCode);
                return;
            }
            else
            {
                // trigger is complex, try to match some common runtime errors
                if (trigger.equalsIgnoreCase(RuntimeException.class.getSimpleName()))
                {
                    throw new RuntimeException("Error from " + TriggersFilter.class.getName());
                }
                else if (trigger.equalsIgnoreCase(IOException.class.getSimpleName()))
                {
                    throw new IOException("Error from " + TriggersFilter.class.getName());
                }
                else if (trigger.equalsIgnoreCase(ServletException.class.getSimpleName()))
                {
                    throw new ServletException("Error from " + TriggersFilter.class.getName());
                }
                else
                {
                    // throw a 500 error with a message
                    // this message is accessible from RequestDispatcher.ERROR_MESSAGE attribute
                    response.sendError(500, trigger);
                    return;
                }
            }
        }
        chain.doFilter(req, resp);
    }

    private String getPossibleTrigger(HttpServletRequest request)
    {
        // look for custom request header
        String trigger = request.getHeader("X-Filter-Trigger");
        if (trigger != null)
            return trigger;

        // look for custom query parameter
        String query = request.getQueryString();
        if (query != null)
        {
            int idx = query.indexOf("trigger=");
            if (idx >= 0)
            {
                return query.substring(idx + "trigger=".length());
            }
        }

        return null;
    }
}
