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
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.eclipse.jetty.toolchain.test.Hex;
import org.eclipse.jetty.toolchain.test.Sha1Sum;
import org.eclipse.jetty.util.IO;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StaticFileGen
{
    public static final long KB = 1024;
    public static final long MB = 1024 * KB;
    public static final long GB = 1024 * MB;

    /**
     * Generate a static file.
     *
     * @param staticFile the path of the file to create
     * @param size the size of the file to create
     * @return the SHA1 hash of the static file
     * @throws IOException if unable to create the file
     * @throws NoSuchAlgorithmException if unable to find the SHA1 algorithm
     */
    public static String generate(Path staticFile, long size) throws IOException, NoSuchAlgorithmException
    {
        byte[] buf = new byte[(int)MB];
        Arrays.fill(buf, (byte)'x');
        ByteBuffer src = ByteBuffer.wrap(buf);

        if (Files.exists(staticFile) && Files.size(staticFile) == size)
        {
            // all done, nothing left to do.
            System.err.printf("File Exists Already: %s (%,d bytes)%n", staticFile, Files.size(staticFile));
            return Sha1Sum.calculate(staticFile);
        }

        System.err.printf("Creating %,d byte file: %s ...%n", size, staticFile);
        try (SeekableByteChannel channel = Files.newByteChannel(staticFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING))
        {
            long remaining = size;
            while (remaining > 0)
            {
                ByteBuffer slice = src.slice();
                int len = buf.length;
                if (remaining < Integer.MAX_VALUE)
                {
                    len = Math.min(buf.length, (int)remaining);
                    slice.limit(len);
                }

                channel.write(slice);
                remaining -= len;
            }
        }
        System.err.println(" Done");
        return Sha1Sum.calculate(staticFile);
    }

    public static void verify(InputStream inputStream, long expectedSize, String expectedSha1) throws NoSuchAlgorithmException, IOException
    {
        MessageDigest digest = MessageDigest.getInstance("SHA1");
        try(ByteCountingOutputStream byteCountingOutputStream = new ByteCountingOutputStream();
            DigestOutputStream digestOut = new DigestOutputStream(byteCountingOutputStream, digest))
        {
            IO.copy(inputStream, digestOut);
            String actualSha1 = Hex.asHex(digestOut.getMessageDigest().digest());
            assertEquals(expectedSha1, actualSha1);

            long actualSize = byteCountingOutputStream.getCount();
            assertEquals(expectedSize, actualSize);
        }
    }
}
