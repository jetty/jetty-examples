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

package org.eclipse.jetty.demos;

import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class ContextInit implements ServletContextListener
{
    @Override
    public void contextInitialized(ServletContextEvent sce)
    {
        // Clear out java.util.logging configuration
        Logger rootLogger = LogManager.getLogManager().getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        for (Handler handler : handlers)
        {
            try
            {
                handler.close();
            }
            catch (Throwable ignored)
            {
            }
            rootLogger.removeHandler(handler);
        }
        // Install JUL to SLF4J Bridge
        org.slf4j.bridge.SLF4JBridgeHandler.install();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce)
    {
    }
}
