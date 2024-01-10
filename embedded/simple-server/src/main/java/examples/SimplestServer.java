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

import org.eclipse.jetty.server.Server;

public class SimplestServer
{
    public static void main(String[] args) throws Exception
    {
        Server server = new Server(9090);
        // This has a connector listening on port 9090
        // and no handlers, meaning all requests will result
        // in a 404 response
        server.start();
        System.err.println("Hint: Hit Ctrl+C to stop Jetty.");
        server.join();
    }
}
