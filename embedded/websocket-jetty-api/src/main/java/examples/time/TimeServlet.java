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

package examples.time;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class TimeServlet extends HttpServlet
{
    private static final TimeZone TZ = TimeZone.getDefault();

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        Locale locale = req.getLocale();
        Calendar cal = Calendar.getInstance(TZ, locale);
        String dateStr = DateFormat.getDateInstance(DateFormat.DEFAULT, locale).format(cal.getTime());
        String timeStr = DateFormat.getTimeInstance(DateFormat.DEFAULT, locale).format(cal.getTime());
        String tzStr = TZ.getDisplayName(false, TimeZone.SHORT, locale);
        resp.getWriter().println(String.format("%s %s %s", dateStr, timeStr, tzStr));
    }
}
