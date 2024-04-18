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

package jetty.bootstrap;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;

public class JettyBootstrap
{
    public static void main(String[] args)
    {
        try
        {
            URL warLocation = JettyBootstrap.class.getProtectionDomain().getCodeSource().getLocation();
            if (warLocation == null)
            {
                throw new IOException("JettyBootstrap not discoverable");
            }

            LiveWarClassLoader clWar = new LiveWarClassLoader(warLocation);
            System.err.println("Using ClassLoader: " + clWar);
            Thread.currentThread().setContextClassLoader(clWar);

            File warFile = new File(warLocation.toURI());
            System.setProperty("org.eclipse.jetty.livewar.LOCATION",warFile.toPath().toRealPath().toString());

            Class<?> mainClass = Class.forName("jetty.livewar.ServerMain",false,clWar);
            Method mainMethod = mainClass.getMethod("main",args.getClass());
            mainMethod.invoke(mainClass,new Object[] { args });
        }
        catch (Throwable t)
        {
            t.printStackTrace(System.err);
            System.exit(-1);
        }
    }
}
