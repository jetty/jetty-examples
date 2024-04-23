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

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;

public class Util
{
    public static String toDebugString(Object obj)
    {
        if (obj == null)
            return "<null>";
        return String.format("%s@%X - %s", obj.getClass().getName(), obj.hashCode(), obj);
    }

    public static URI getCodeSourceLocation(Class<?> clazz)
    {
        try
        {
            ProtectionDomain domain = AccessController.doPrivileged((PrivilegedAction<ProtectionDomain>)clazz::getProtectionDomain);
            if (domain != null)
            {
                CodeSource source = domain.getCodeSource();
                if (source != null)
                {
                    URL location = source.getLocation();

                    if (location != null)
                    {
                        return location.toURI();
                    }
                }
            }
        }
        catch (URISyntaxException ignored)
        {
        }
        return null;
    }

    public static URI getClassLoaderLocation(Class<?> clazz, ClassLoader loader)
    {
        if (loader == null)
        {
            return null;
        }

        try
        {
            String resourceName = toClassReference(clazz.getName());
            URL url = loader.getResource(resourceName);
            if (url != null)
            {
                URI uri = url.toURI();
                String uriStr = uri.toASCIIString();
                if (uriStr.startsWith("jar:file:"))
                {
                    uriStr = uriStr.substring(4);
                    int idx = uriStr.indexOf("!/");
                    if (idx > 0)
                    {
                        return URI.create(uriStr.substring(0, idx));
                    }
                }
                return uri;
            }
        }
        catch (URISyntaxException ignored)
        {
        }
        return null;
    }

    public static URI getSystemClassLoaderLocation(Class<?> clazz)
    {
        return getClassLoaderLocation(clazz, ClassLoader.getSystemClassLoader());
    }

    public static String toClassReference(String className)
    {
        return className.replace('.', '/').concat(".class");
    }
}
