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
import java.util.Set;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServlet;

import jakarta.servlet.http.HttpServletRequest;
import org.eclipse.jetty.server.LowResourceMonitor;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;

public class AbstractLowResourceDumpServlet extends HttpServlet
{
    protected LowResourceMonitor getLowResourceMonitor()
    {
        return (LowResourceMonitor)getServletContext().getAttribute(LowResourceMonitor.class.getName());
    }

    protected Server getServer(HttpServletRequest request)
    {
        Request coreRequest = (Request)request.getAttribute(Request.class.getName());
        return coreRequest.getConnectionMetaData().getConnector().getServer();
    }

    protected void dump(ServletOutputStream out, LowResourceMonitor monitor) throws IOException
    {
        Set<LowResourceMonitor.LowResourceCheck> checks = monitor.getLowResourceChecks();
        out.println("LowResourceChecks: ");
        for (LowResourceMonitor.LowResourceCheck check : checks)
        {
            out.println(String.format("   (%s) %s%n", check.getClass().getName(), check));
        }
    }
}
