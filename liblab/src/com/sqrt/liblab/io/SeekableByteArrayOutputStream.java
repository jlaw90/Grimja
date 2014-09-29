/*
 * Copyright (C) 2014  James Lawrence.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.sqrt.liblab.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class SeekableByteArrayOutputStream extends ByteArrayOutputStream {
    private int pos;

    public SeekableByteArrayOutputStream() {
        super();
    }

    public SeekableByteArrayOutputStream(int size) {
        super(size);
    }

    private void ensureCapacity(int minCapacity) {
        if (minCapacity - buf.length > 0)
            grow(minCapacity);
    }

    private void grow(int minCapacity) {
        int oldCapacity = buf.length;
        int newCapacity = oldCapacity << 1;
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        if (newCapacity < 0) {
            if (minCapacity < 0) // overflow
                throw new OutOfMemoryError();
            newCapacity = Integer.MAX_VALUE;
        }
        buf = Arrays.copyOf(buf, newCapacity);
    }

    public synchronized void write(int b) {
        ensureCapacity(pos + 1);
        buf[pos] = (byte) b;
        pos += 1;
        if (pos > count)
            count = pos;
    }

    public synchronized void seek(int off) {
        this.pos = off;
    }

    public synchronized int getPosition() {
        return pos;
    }

    public synchronized int skip(int i) {
        pos += i;
        return i;
    }

    public synchronized void write(byte b[], int off, int len) {
        if ((off < 0) || (off > b.length) || (len < 0) ||
                ((off + len) - b.length > 0)) {
            throw new IndexOutOfBoundsException();
        }
        ensureCapacity(this.pos + len);
        System.arraycopy(b, off, buf, this.pos, len);
        this.pos += len;
        if (this.pos > count)
            count = this.pos;
    }

    public synchronized void reset() {
        super.reset();
        pos = 0;
    }

    public void close() throws IOException {
    }
}