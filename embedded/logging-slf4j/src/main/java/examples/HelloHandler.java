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

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloHandler extends AbstractHandler
{
    private static final Logger LOG = LoggerFactory.getLogger(HelloHandler.class);
    private final String msg;

    public HelloHandler(String msg)
    {
        this.msg  = msg;
    }

    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
        LOG.info("Got request from {} for {}",request.getRemoteAddr(), request.getRequestURL());
        response.setContentType("text/plain");
        response.getWriter().printf("%s%n",msg);
        baseRequest.setHandled(true);
    }
}
