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

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import static org.eclipse.jetty.demos.Util.toDebugString;
import static org.slf4j.LoggerFactory.getILoggerFactory;
import static org.slf4j.LoggerFactory.getLogger;

public class Slf4jDump
{
    public static void dump(PrintWriter out) throws IOException
    {
        ILoggerFactory loggerFactory = getILoggerFactory();
        out.printf("  slf4j ILoggerFactory: %s%n", toDebugString(loggerFactory));
        Logger rootLogger = getLogger("");
        out.printf("  slf4j root Logger: %s%n", toDebugString(rootLogger));
    }
}
