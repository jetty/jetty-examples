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
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.LowResourceMonitor;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;

@WebServlet("/baserequest")
public class FromBaseRequestServlet extends AbstractLowResourceDumpServlet
{
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        Server server = Request.getBaseRequest(request).getHttpChannel().getServer();

        LowResourceMonitor monitor = server.getBean(LowResourceMonitor.class);

        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/plain");
        dump(response.getOutputStream(), monitor);
    }
}
