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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.stream.Stream;

import org.eclipse.jetty.util.IO;

public class WarBuilder implements AutoCloseable
{
    private OutputStream out;
    private JarOutputStream jarOut;

    public WarBuilder(Path warPath) throws IOException
    {
        out = Files.newOutputStream(warPath, StandardOpenOption.CREATE);
        jarOut = new JarOutputStream(out);
    }

    public void addClasses(Path path) throws IOException
    {
        packDir(path, "WEB-INF/classes", path);
    }

    public void addDir(Path path) throws IOException
    {
        packDir(path, "/", path);
    }

    private void packDir(Path baseDir, String destPath, Path path) throws IOException
    {
        Stream<Path> pathStream = Files.list(path);
        for (Iterator<Path> it = pathStream.iterator(); it.hasNext(); )
        {
            Path entry = it.next();
            if (Files.isDirectory(entry))
            {
                packDir(baseDir, destPath, entry);
            }
            else
            {
                String name = destPath;
                if (!name.startsWith("/"))
                    name = "/" + name;
                if (!name.endsWith("/"))
                    name = name + "/";
                name = name + baseDir.relativize(entry).toString();
                JarEntry jarEntry = new JarEntry(name);
                jarEntry.setSize(Files.size(entry));
                try (InputStream in = Files.newInputStream(entry))
                {
                    jarOut.putNextEntry(jarEntry);
                    IO.copy(in, jarOut);
                }
                finally
                {
                    jarOut.closeEntry();
                }
            }
        }
    }

    @Override
    public void close()
    {
        IO.close(jarOut);
        IO.close(out);
    }
}
