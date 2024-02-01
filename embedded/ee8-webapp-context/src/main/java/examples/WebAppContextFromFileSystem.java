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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jetty.ee8.webapp.WebAppContext;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.InetAccessHandler;

public class WebAppContextFromFileSystem
{
    public static void main(String[] args) throws Exception
    {
        Server server = new Server(8080);

        Path warPath = Paths.get("target/webapps/hello-servlet-3.war").toAbsolutePath().normalize();
        if (!Files.isRegularFile(warPath))
        {
            System.err.println("Unable to find " + warPath + ".  Please build the entire project once first (`mvn clean install` from top of repo)");
            System.exit(-1);
        }

        System.out.println("WAR File is " + warPath);

        InetAccessHandler inetAccessHandler = new InetAccessHandler();
        // allow only http clients from localhost IPv4 or IPv6
        inetAccessHandler.include("127.0.0.1", "::1");
        server.setHandler(inetAccessHandler);

        Handler.Sequence handlers = new Handler.Sequence();
        inetAccessHandler.setHandler(handlers);

        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/");
        webapp.setWar(warPath.toUri().toASCIIString());

        handlers.addHandler(webapp);

        // we should now have a handler tree like ...
        // server.handler = InetAccessHandler
        //                    wrapping -> Handler.Sequence
        //                      contains -> WebAppContext

        server.start();
        server.join();
    }
}
