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

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public abstract class AbstractMainTest
{
    private Main main;

    @BeforeEach
    public void startServer() throws Exception
    {
        main = new Main(8080);
        main.start();
    }

    @AfterEach
    public void stopServer() throws Exception
    {
        main.stop();
    }

    public String resourceWithUrl(String uri) throws Exception
    {
        URL url = new URL(uri);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        InputStream inputStream = connection.getInputStream();
        byte[] response = new byte[inputStream.available()];
        inputStream.read(response);

        return new String(response);
    }
}
