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

import com.sqrt.liblab.LabFile;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.BufferOverflowException;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * This DataSource maps the area of the file we're interested in into memory when required and then performs reads
 * from there.  Closing this datasource unmaps the file from memory if mapped.
 */
public class DiskDataSource extends DataSource {
    private static final int BUFFER_SIZE = 1 * 1024 * 1024; // Page 1MB of data
    private MappedByteBuffer map;
    private final RandomAccessFile source;
    private long _pos;
    /**
     * The offset into the source file that the data we're interested in starts at
     */
    public final long start;
    /**
     * The length of the data we're interested in
     */
    public final long len;

    /**
     * Constructs a DiskDataSource that gets its data from the specified RandomAccessFile
     * @param container the container that contains this data
     * @param name the name of the entry we have the data for
     * @param source the source of the data
     * @param start the offset into the source that the data we're interested in starts at
     * @param len the length of the data we're interested in
     */
    public DiskDataSource(LabFile container, String name, RandomAccessFile source, long start, long len) {
        super(container, name);
        this.start = start;
        this.len = len;
        this.source = source;
    }

    /**
     * Constructs a DiskDataSource that gets its data from the specified RandomAccessFile
     * @param container the container that contains this data
     * @param name the name of the entry we have the data for
     * @param source the source of the data
     * @throws IOException
     */
    public DiskDataSource(LabFile container, String name, RandomAccessFile source) throws IOException {
        this(container, name, source, 0, source.length());
    }

    /**
     * Returns a DiskDataSource that only reads the specified subsection of the data
     * @param off the offset into this datasource that the subsections data will start at
     * @param len the length of the data the subsection will read
     * @return the new data source
     */
    public synchronized DiskDataSource slice(int off, int len) {
        return new DiskDataSource(container, getName(), source, start + off, len);
    }

    private void ensureMapped(long pos, int len) throws IOException {
        if (map == null || pos < _pos || pos + len > _pos + map.capacity()) {
            _pos = pos;
            long nlen = Math.max(Math.min(BUFFER_SIZE, this.len - _pos), len);
            if(_pos + nlen > this.len)
                throw new BufferOverflowException(); // Todo: this is required when reading from a LAB, but will restrict
                                                     // when writing to a new file... need canExpand or something
            map = source.getChannel().map(FileChannel.MapMode.READ_ONLY, start + _pos, nlen);
            map.position(0);
        }
    }

    private void ensureMapped(int len) throws IOException {
        ensureMapped(position(), len);
    }

    public synchronized long position() throws IOException {
        return map == null? 0: (_pos + map.position());
    }

    public synchronized void position(long pos) throws IOException {
        ensureMapped(pos, 0);
        map.position((int) (pos - _pos));
    }

    public long length() {
        return len;
    }

    public synchronized void get(byte[] b, int off, int len) throws IOException {
        ensureMapped(len);
        map.get(b, off, len);
    }

    public synchronized void put(byte[] b, int off, int len) throws IOException {
        ensureMapped(len);
        map.put(b, off, len);
    }

    public byte get() throws IOException {
        ensureMapped(1);
        return map.get();
    }

    public void put(byte b) throws IOException {
        map.put(b);
    }

    public synchronized int getUByte() throws IOException {
        ensureMapped(1);
        return map.get() & 0xff;
    }

    /**
     * Unmaps the data from memory (if previously mapped)
     * @throws IOException
     */
    public synchronized void close() throws IOException {
        if (map == null)
            return;
        map.clear();
        map = null;
    }

    public boolean markSupported() {
        return true;
    }

    public short getShort() throws IOException {
        ensureMapped(2);
        be();
        return map.getShort();
    }

    public void putShort(short s) throws IOException {
        ensureMapped(2);
        be();
        map.putShort(s);
    }

    public short getShortLE() throws IOException {
        ensureMapped(2);
        le();
        return map.getShort();
    }

    public void putShortLE(short s) throws IOException {
        ensureMapped(2);
        le();
        map.putShort(s);
    }

    public int getInt() throws IOException {
        ensureMapped(4);
        be();
        return map.getInt();
    }

    public void putInt(int i) throws IOException {
        ensureMapped(4);
        be();
        map.putInt(i);
    }

    public int getIntLE() throws IOException {
        ensureMapped(4);
        le();
        return map.getInt();
    }

    public void putIntLE(int i) throws IOException {
        ensureMapped(4);
        le();
        map.putInt(i);
    }

    private void be() {
        map.order(ByteOrder.BIG_ENDIAN);
    }

    private void le() {
        map.order(ByteOrder.LITTLE_ENDIAN);
    }
}