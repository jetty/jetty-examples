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
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceFactory;

public class ResourceFactoryDemo
{
    public static void main(String[] args) throws InterruptedException
    {
        CountDownLatch thread1In = new CountDownLatch(1);
        CountDownLatch thread1Out = new CountDownLatch(1);
        CountDownLatch thread2In = new CountDownLatch(1);
        CountDownLatch thread2Out = new CountDownLatch(1);

        Thread thread1 = new Thread(() ->
        {
            try (ResourceFactory.Closeable resourceFactory = ResourceFactory.closeable())
            {
                Path path = Path.of("src/test/jars/log4j.jar");
                Resource jarFile = resourceFactory.newJarFileResource(path.toUri());
                List<Resource> resources = jarFile.list();
                System.out.printf("[Thread 1] Found %d list() entries from %s%n", resources.size(), jarFile);
                thread1In.countDown();
                System.out.println("[Thread 1] Waiting for Thread 2 to start");
                thread2In.await();
                Resource notice = jarFile.resolve("META-INF/NOTICE");
                System.out.printf("[Thread 1] Reading %s%n", notice);
                try (InputStream in = notice.newInputStream())
                {
                    String noticeStr = IO.toString(in);
                    System.out.printf("[Thread 1] Read %d characters from %s%n", noticeStr.length(), notice);
                }
            }
            catch (Throwable t)
            {
                t.printStackTrace(System.err);
            }
            System.out.println("[Thread 1] ResourceFactory closed");
            thread1Out.countDown();
        });

        Thread thread2 = new Thread(() ->
        {
            try (ResourceFactory.Closeable resourceFactory = ResourceFactory.closeable())
            {
                Path path = Path.of("src/test/jars/log4j.jar");
                Resource jarFile = resourceFactory.newJarFileResource(path.toUri());
                List<Resource> resources = jarFile.list();
                System.out.printf("[Thread 2] Found %d list() entries from %s%n", resources.size(), jarFile);
                thread2In.countDown();
                System.out.println("[Thread 1] Waiting for Thread 1 to exit");
                thread1Out.await();
                Resource deps = jarFile.resolve("META-INF/DEPENDENCIES");
                System.out.printf("[Thread 2] Reading %s%n", deps);
                try (InputStream in = deps.newInputStream())
                {
                    String noticeStr = IO.toString(in);
                    System.out.printf("[Thread 2] Read %d characters from %s%n", noticeStr.length(), deps);
                }
            }
            catch (Throwable t)
            {
                t.printStackTrace(System.err);
            }
            System.out.println("[Thread 2] ResourceFactory closed");
            thread2Out.countDown();
        });

        thread1.start();
        thread1In.await();
        thread2.start();
        thread2In.await();

        thread1Out.await();
        thread2Out.await();
    }
}
