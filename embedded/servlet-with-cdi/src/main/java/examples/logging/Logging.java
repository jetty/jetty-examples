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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.LogManager;

public class Logging
{
    private static boolean CONFIGURED = false;

    public static void config()
    {
        if (CONFIGURED)
            return;

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL url = cl.getResource("logging.properties");
        if (url != null)
        {
            try (InputStream in = url.openStream())
            {
                LogManager.getLogManager().readConfiguration(in);
            }
            catch (IOException e)
            {
                e.printStackTrace(System.err);
            }
        }

        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.Jdk14Logger");
        CONFIGURED = true;
    }
}
