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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;

import static org.eclipse.jetty.demos.Util.toDebugString;

public class Log4jDump
{
    public static void dump(PrintWriter out) throws IOException
    {
        out.printf("  log4j LogManager.LoggerContextFactory: %s%n", toDebugString(LogManager.getFactory()));
        out.printf("  log4j LogManager.LoggerContext: %s%n", toDebugString(LogManager.getContext()));

        LoggerContext coreLoggerContext = LoggerContext.getContext(true);
        out.printf("  log4j core.LoggerContext: %s%n", toDebugString(coreLoggerContext));
        if (coreLoggerContext != null)
        {
            out.printf("  log4j core.LoggerContext.configLocation: %s%n", coreLoggerContext.getConfigLocation());
            Configuration config = coreLoggerContext.getConfiguration();
            out.printf("  log4j core.LoggerContext.configuration: %s%n", toDebugString(config));
            Map<String, Appender> appenderMap = config.getAppenders();

            List<String> names = new ArrayList<>(appenderMap.keySet());
            Collections.sort(names);
            for (String name : names)
            {
                Appender appender = appenderMap.get(name);
                out.printf("  log4j.append[%s] - %s%n", name, toDebugString(appender));
            }
        }
        Logger rootLogger = LogManager.getRootLogger();
        out.printf("  log4j root Logger: %s%n", toDebugString(rootLogger));
        if (rootLogger != null)
        {
            out.printf("  log4j root MessageFactory: %s%n", toDebugString(rootLogger.getMessageFactory()));
        }
    }
}
