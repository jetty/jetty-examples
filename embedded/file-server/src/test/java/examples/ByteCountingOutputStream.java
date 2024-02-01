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

import java.io.OutputStream;

public class ByteCountingOutputStream extends OutputStream
{
    private long count = 0;

    public long getCount()
    {
        return count;
    }

    @Override
    public void write(int b)
    {
        count++;
    }

    @Override
    public void write(byte[] b)
    {
        count += b.length;
    }

    @Override
    public void write(byte[] b, int off, int len)
    {
        count += len;
    }
}
