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
import java.io.InputStream;
import java.io.OutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SlowImageServlet extends HttpServlet
{
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        response.addHeader("Cache-control", "no-store, no-cache, must-revalidate");
        response.addDateHeader("Last-Modified", 0);
        response.addDateHeader("Expires", 0);

        String path = request.getRequestURI();
        try (InputStream input = getServletContext().getResourceAsStream(path))
        {
            OutputStream output = response.getOutputStream();
            byte[] buffer = new byte[4 * 1024];
            int read;
            while ((read = input.read(buffer)) >= 0)
            {
                output.write(buffer, 0, read);
            }
        }
    }
}
