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

package org.example;

import jakarta.servlet.ServletContextAttributeEvent;
import jakarta.servlet.ServletContextAttributeListener;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletRequestAttributeEvent;
import jakarta.servlet.ServletRequestAttributeListener;
import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;
import jakarta.servlet.http.HttpSessionActivationListener;
import jakarta.servlet.http.HttpSessionAttributeListener;
import jakarta.servlet.http.HttpSessionBindingEvent;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

public class TagListener implements HttpSessionListener, HttpSessionAttributeListener, HttpSessionActivationListener, ServletContextListener, ServletContextAttributeListener, ServletRequestListener, ServletRequestAttributeListener
{
    public void attributeAdded(HttpSessionBindingEvent se)
    {
        //System.err.println("tagListener: attributedAdded "+se);
    }

    public void attributeRemoved(HttpSessionBindingEvent se)
    {
        //System.err.println("tagListener: attributeRemoved "+se);
    }

    public void attributeReplaced(HttpSessionBindingEvent se)
    {
        //System.err.println("tagListener: attributeReplaced "+se);
    }

    public void sessionWillPassivate(HttpSessionEvent se)
    {
        //System.err.println("tagListener: sessionWillPassivate "+se);
    }

    public void sessionDidActivate(HttpSessionEvent se)
    {
        //System.err.println("tagListener: sessionDidActivate "+se);
    }

    public void contextInitialized(ServletContextEvent sce)
    {
        //System.err.println("tagListener: contextInitialized "+sce);
    }

    public void contextDestroyed(ServletContextEvent sce)
    {
        //System.err.println("tagListener: contextDestroyed "+sce);
    }

    public void attributeAdded(ServletContextAttributeEvent scab)
    {
        //System.err.println("tagListener: attributeAdded "+scab);
    }

    public void attributeRemoved(ServletContextAttributeEvent scab)
    {
        //System.err.println("tagListener: attributeRemoved "+scab);
    }

    public void attributeReplaced(ServletContextAttributeEvent scab)
    {
        //System.err.println("tagListener: attributeReplaced "+scab);
    }

    public void requestDestroyed(ServletRequestEvent sre)
    {
        //System.err.println("tagListener: requestDestroyed "+sre);
    }

    public void requestInitialized(ServletRequestEvent sre)
    {
        //System.err.println("tagListener: requestInitialized "+sre);
    }

    public void attributeAdded(ServletRequestAttributeEvent srae)
    {
        //System.err.println("tagListener: attributeAdded "+srae);
    }

    public void attributeRemoved(ServletRequestAttributeEvent srae)
    {
        //System.err.println("tagListener: attributeRemoved "+srae);
    }

    public void attributeReplaced(ServletRequestAttributeEvent srae)
    {
        //System.err.println("tagListener: attributeReplaced "+srae);
    }

    public void sessionCreated(HttpSessionEvent se)
    {
        //System.err.println("tagListener: sessionCreated "+se);
    }

    public void sessionDestroyed(HttpSessionEvent se)
    {
        //System.err.println("tagListener: sessionDestroyed "+se);
    }
}
