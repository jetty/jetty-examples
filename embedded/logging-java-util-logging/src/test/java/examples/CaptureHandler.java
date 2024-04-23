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

import java.util.LinkedList;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.fail;

public class CaptureHandler extends Handler
{
    public static CaptureHandler attach(String logname)
    {
        CaptureHandler handler = new CaptureHandler();
        Logger.getLogger(logname).addHandler(handler);
        return handler;
    }

    private LinkedList<LogRecord> events = new LinkedList<>();

    @Override
    public void publish(LogRecord record)
    {
        events.add(record);
    }

    @Override
    public void flush()
    {
    }

    @Override
    public void close() throws SecurityException
    {
    }

    public void detach(String logname)
    {
        Logger.getLogger(logname).removeHandler(this);
    }

    public void assertContainsRecord(String logname, String containsString)
    {
        for (LogRecord record : events)
        {
            if (record.getLoggerName().equals(logname))
            {
                if (record.getMessage().contains(containsString))
                {
                    // found it
                    return;
                }
            }
        }
        fail("Unable to find record matching logname [" + logname + "] containing string [" + containsString + "]");
    }
}
