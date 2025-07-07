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

package org.eclipse.jetty.demos.dumps;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static org.eclipse.jetty.demos.Util.toDebugString;

public class JavaUtilLoggingDump
{
    public static void dump(PrintWriter out) throws IOException
    {
        LogManager logManager = LogManager.getLogManager();
        out.printf("  java.util.logging - LogManager: %s%n", toDebugString(logManager));

        Logger rootLogger = logManager.getLogger("");
        out.printf("  java.util.logging - root Logger: %s%n", toDebugString(rootLogger));

        for (Handler handler : rootLogger.getHandlers())
        {
            out.printf("  rootLogger.handler - %s%n", toDebugString(handler));
        }
    }
}
