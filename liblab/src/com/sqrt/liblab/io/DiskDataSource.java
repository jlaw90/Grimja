package com.sqrt.liblab.io;

import com.sqrt.liblab.LabFile;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * This DataSource maps the area of the file we're interested in into memory when required and then performs reads
 * from there.  Closing this datasource unmaps the file from memory if mapped.
 */
public class DiskDataSource extends DataSource {
    private MappedByteBuffer map;
    private final RandomAccessFile source;
    private int mark;
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

    private void ensureMapped() throws IOException {
        if (map == null) {
            map = source.getChannel().map(FileChannel.MapMode.READ_ONLY, start, len);
            map.position(0);
        }
    }

    /**
     * Returns a DiskDataSource that only reads the specified subsection of the data
     * @param off the offset into this datasource that the subsections data will start at
     * @param len the length of the data the subsection will read
     * @return the new data source
     */
    public synchronized DiskDataSource subsection(int off, int len) {
        return new DiskDataSource(container, getName(), source, start + off, len);
    }

    /**
     * Reads an unsigned byte from the stream and returns it
     * @return an unsigned byte
     * @throws IOException
     */
    public synchronized int read() throws IOException {
        ensureMapped();
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

    public synchronized int available() throws IOException {
        ensureMapped();
        return map.remaining();
    }

    public synchronized void seek(long pos) throws IOException {
        ensureMapped();
        map.position((int) pos);
    }

    public synchronized long getPosition() throws IOException {
        ensureMapped();
        return map.position();
    }

    public long getLength() {
        return len;
    }

    public synchronized long skip(long n) throws IOException {
        ensureMapped();
        map.position(map.position() + (int) n);
        return n;
    }

    public synchronized int read(byte[] b, int _off, int _len) throws IOException {
        ensureMapped();
        _len = Math.min(map.remaining(), _len);
        if(_len == 0)
            return -1;
        map.get(b, _off, _len);
        return _len;
    }

    public synchronized void mark(int readlimit) {
        try {
            ensureMapped();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mark = map.position();
    }

    public synchronized void reset() throws IOException {
        ensureMapped();
        map.position(mark);
    }

    public boolean markSupported() {
        return true;
    }

    public short readShort() throws IOException {
        ensureMapped();
        map.order(ByteOrder.BIG_ENDIAN);
        return map.getShort();
    }

    public short readShortLE() throws IOException {
        ensureMapped();
        map.order(ByteOrder.LITTLE_ENDIAN);
        return map.getShort();
    }

    public int readInt() throws IOException {
        ensureMapped();
        map.order(ByteOrder.BIG_ENDIAN);
        return map.getInt();
    }

    public int readIntLE() throws IOException {
        ensureMapped();
        map.order(ByteOrder.LITTLE_ENDIAN);
        return map.getInt();
    }

    public byte readByte() throws IOException {
        ensureMapped();
        return map.get();
    }
}