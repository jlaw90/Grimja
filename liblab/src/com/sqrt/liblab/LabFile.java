package com.sqrt.liblab;

import com.sqrt.liblab.codec.CodecMapper;

import java.io.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class LabFile {
    private RandomAccessFile source;
    private List<EntryDataProvider> _entries = new LinkedList<EntryDataProvider>();
    public final List<EntryDataProvider> entries = Collections.unmodifiableList(_entries);

    public LabFile() throws IOException {
    }

    private LabFile(File path) throws IOException {
        // Todo: endianness would be better handled with NIO, but reading and writing would not be so pretty
        long fLen = path.length();
        try {
            source = new RandomAccessFile(path, "rw");
        } catch (IOException ioe) {
            source = new RandomAccessFile(path, "r");
        }
        int magic;
        if ((magic = source.readInt()) != (('L' << 24) | ('A' << 16) | ('B' << 8) | ('N')))
            throw new IOException(String.format("Invalid LAB file, magic: %08x", magic));
        int ver = Integer.reverseBytes(source.readInt());
        int entries = Integer.reverseBytes(source.readInt()); // should be uint but the chances of there being over 2147483647 entries is, I assume: slim
        int stringTableSize = Integer.reverseBytes(source.readInt());
        int stringTableOffset = 16 * (entries + 1);

        for (int i = 0; i < entries; i++) {
            source.seek(16 + i * 16); // seek...
            int nameOff = Integer.reverseBytes(source.readInt());
            int start = Integer.reverseBytes(source.readInt());
            int size = Integer.reverseBytes(source.readInt());
            //int what = EndianHelper.reverse4(in.getInt());

            // nul terminated string
            StringBuilder sb = new StringBuilder();
            source.seek(stringTableOffset + nameOff);
            int ch;
            while ((ch = source.read()) != -1) {
                if (ch == 0)
                    break;
                sb.append((char) ch);
            }
            String name = sb.toString(); // huzzah
            this._entries.add(new EntryDiskDataProvider(this, name, start, size));
        }
    }

    public static LabFile open(File path) throws IOException {
        return new LabFile(path);
    }

    public LabEntry getEntry(String name) throws IOException {
        for(EntryDataProvider edp: entries) {
            if(edp.getName().equalsIgnoreCase(name)) {
                return CodecMapper.codecForProvider(edp).read(edp);
            }
        }
        return null;
    }

    // Only works for labfiles loaded from disk
    private class EntryDiskDataProvider extends EntryDataProvider {
        public final long off, len;
        private long _off, _mark;

        protected EntryDiskDataProvider(LabFile container, String name, long off, long len) {
            super(container, name);
            this.off = off;
            this.len = len;
            _off = off;
            _mark = _off;
        }

        public int read() throws IOException {
            if (_off >= off + len)
                return -1;
            container.source.seek(_off++);
            return container.source.read();
        }

        public void close() throws IOException {
            _off = off;
            _mark = _off;
        }

        public int available() throws IOException {
            return (int) ((off + len) - _off);
        }

        public void seek(long pos) throws IOException {
            if (pos >= len)
                throw new IOException();
            _off = off + pos;
        }

        public long skip(long n) throws IOException {
            long skip = Math.min((off + len) - 1, _off + n) - _off;
            _off += skip;
            return skip;
        }

        public int read(byte[] b, int _off, int _len) throws IOException {
            if (this._off + _len > off + len)
                _len = (int) (Math.max(0, (off + len) - this._off));
            container.source.seek(this._off);
            int read = container.source.read(b, _off, _len);
            if (read != -1)
                this._off += read;
            return read;
        }

        public synchronized void mark(int readlimit) {
            _mark = _off;
        }

        public synchronized void reset() throws IOException {
            _off = _mark;
        }

        public boolean markSupported() {
            return true;
        }
    }
}