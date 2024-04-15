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

import org.apache.tomcat.util.scan.StandardJarScanFilter;
import org.apache.tomcat.util.scan.StandardJarScanner;
import org.eclipse.jetty.ee10.apache.jsp.JettyJasperInitializer;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.util.component.AbstractLifeCycle;

/**
 * JspStarter for embedded ServletContextHandlers
 *
 * This is added as a bean that is a jetty LifeCycle on the ServletContextHandler.
 * This bean's doStart method will be called as the ServletContextHandler starts,
 * and will call the ServletContainerInitializer for the jsp engine.
 */
public class EmbeddedJspStarter extends AbstractLifeCycle
{
    private JettyJasperInitializer sci;
    private ServletContextHandler context;

    public EmbeddedJspStarter(ServletContextHandler context)
    {
        this.sci = new JettyJasperInitializer();
        this.context = context;
        StandardJarScanner jarScanner = new StandardJarScanner();
        StandardJarScanFilter jarScanFilter = new StandardJarScanFilter();
        jarScanFilter.setTldScan("taglibs-standard-impl-*");
        jarScanFilter.setTldSkip("apache-*,ecj-*,jetty-*,asm-*,javax.servlet-*,javax.annotation-*,taglibs-standard-spec-*");
        jarScanner.setJarScanFilter(jarScanFilter);
        this.context.setAttribute("org.apache.tomcat.JarScanner", jarScanner);
    }

    @Override
    protected void doStart() throws Exception
    {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(context.getClassLoader());
        try
        {
            sci.onStartup(null, context.getServletContext());
            super.doStart();
        }
        finally
        {
            Thread.currentThread().setContextClassLoader(old);
        }
    }
}
