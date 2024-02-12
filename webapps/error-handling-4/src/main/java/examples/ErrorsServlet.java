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
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ErrorsServlet extends HttpServlet
{
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        if (req.getDispatcherType() != DispatcherType.ERROR)
        {
            // don't allow users to directly access this servlet.
            // it only exists for handling ERROR dispatch requests.
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        resp.setCharacterEncoding("utf-8");
        resp.setContentType("text/plain");

        PrintWriter out = resp.getWriter();


        out.println("DISPATCH: " + req.getDispatcherType().name());
        out.println("contextPath: "+ req.getContextPath());
        out.println("servletPath: "+ req.getServletPath());
        out.println("pathInfo: "+ req.getPathInfo());
        out.println("requestURI: "+ req.getRequestURI());

        out.println("ERROR_MESSAGE: " + req.getAttribute(RequestDispatcher.ERROR_MESSAGE));
        out.println("ERROR_CODE: " + req.getAttribute(RequestDispatcher.ERROR_STATUS_CODE));
        out.println("ERROR_SERVLET: " + req.getAttribute(RequestDispatcher.ERROR_SERVLET_NAME));
        out.println("ERROR_REQUEST_URI: " + req.getAttribute(RequestDispatcher.ERROR_REQUEST_URI));
        out.println("ERROR_EXCEPTION_TYPE: " + req.getAttribute(RequestDispatcher.ERROR_EXCEPTION_TYPE));
        Object errorException = req.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        if (errorException instanceof Throwable cause)
        {
            out.println("ERROR_EXCEPTION: " + cause.getMessage());
            cause.printStackTrace(out);
        }
        else
        {
            out.println("ERROR_EXCEPTION: null");
        }
    }
}
