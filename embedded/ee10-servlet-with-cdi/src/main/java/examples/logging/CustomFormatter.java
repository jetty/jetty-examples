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

package examples.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class CustomFormatter extends Formatter
{
    // Level [shortname] message throwable
    private static final String format = "[%5$s.%3$s] %6$s%8$s%n";
    private final Date dat = new Date();

    /**
     * Format the LogRecord.
     *
     * @param record the log record
     * @return the formatted record
     */
    public String format(LogRecord record)
    {
        dat.setTime(record.getMillis());
        String source;
        if (record.getSourceClassName() != null)
        {
            source = record.getSourceClassName();
            if (record.getSourceMethodName() != null)
            {
                source += " " + record.getSourceMethodName();
            }
        }
        else
        {
            source = record.getLoggerName();
        }
        String message = formatMessage(record);
        String threadId = String.valueOf(record.getThreadID());
        String shortName = record.getLoggerName();
        int lastDot = shortName.lastIndexOf('.');
        if (lastDot > 0)
            shortName = shortName.substring(lastDot + 1);
        String throwable = "";
        if (record.getThrown() != null)
        {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.println();
            record.getThrown().printStackTrace(pw);
            pw.close();
            throwable = sw.toString();
        }

        return String.format(format, dat, // %1
            source, // %2
            record.getLoggerName(), // %3
            shortName, // %4
            record.getLevel().getName(), // %5
            message, // %6
            threadId, // %7
            throwable // %8
        );
    }
}