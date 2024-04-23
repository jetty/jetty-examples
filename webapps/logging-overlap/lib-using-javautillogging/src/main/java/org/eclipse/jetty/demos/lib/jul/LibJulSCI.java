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

package org.eclipse.jetty.demos.lib.jul;

import java.util.EnumSet;
import java.util.Set;
import javax.servlet.DispatcherType;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;

public class LibJulSCI implements ServletContainerInitializer
{
    @Override
    public void onStartup(Set<Class<?>> c, ServletContext ctx)
    {
        ctx.addFilter("JulFilter", JulFilter.class)
            .addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST),
                false, "/*");
    }
}
