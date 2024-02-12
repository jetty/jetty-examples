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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TriggersServlet extends HttpServlet
{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null)
        {
            // strip leading slash
            if (pathInfo.startsWith("/"))
                pathInfo = pathInfo.substring(1);

            if (pathInfo.matches("[0-9]+"))
            {
                // trigger is a number, use it as an HTTP status code
                int statusCode = Integer.parseInt(pathInfo);
                resp.sendError(statusCode);
                return;
            }
            else
            {
                // trigger is complex, try to match some common runtime errors
                if (pathInfo.equalsIgnoreCase(RuntimeException.class.getSimpleName()))
                {
                    throw new RuntimeException("Error from " + TriggersServlet.class.getName());
                }
                else if (pathInfo.equalsIgnoreCase(IOException.class.getSimpleName()))
                {
                    throw new IOException("Error from " + TriggersServlet.class.getName());
                }
                else if (pathInfo.equalsIgnoreCase(ServletException.class.getSimpleName()))
                {
                    throw new ServletException("Error from " + TriggersServlet.class.getName());
                }
                else
                {
                    // throw a 500 error with a message
                    // this message is accessible from RequestDispatcher.ERROR_MESSAGE attribute
                    resp.sendError(500, pathInfo);
                    return;
                }
            }
        }

        throw new IllegalArgumentException("Unrecognized pathInfo trigger");
    }
}
