package com.sqrt.liblab;

import com.sqrt.liblab.codec.CodecMapper;
import com.sqrt.liblab.codec.EntryCodec;
import com.sqrt.liblab.io.DataSource;
import com.sqrt.liblab.io.DiskDataSource;

import java.io.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class LabFile {
    public final LabCollection container;
    private String name;
    public final List<DataSource> entries = new LinkedList<DataSource>();

    LabFile(LabCollection container) {
        this.container = container;
        name = "New";
    }

    LabFile(LabCollection container, File path) throws IOException {
        this(container);
        name = path.getName().toUpperCase();
        RandomAccessFile source;
        try {
            source = new RandomAccessFile(path, "rw");
        } catch (IOException ioe) {
            source = new RandomAccessFile(path, "r");
        }
        int magic;
        if ((magic = source.readInt()) != (('L' << 24) | ('A' << 16) | ('B' << 8) | ('N')))
            throw new IOException(String.format("Invalid LAB file, magic: %08x", magic));
        source.skipBytes(4); // version
        int entries = Integer.reverseBytes(source.readInt()); // should be uint but the chances of there being over 2147483647 entries is, I assume: slim
        source.skipBytes(4); // string table size
        Integer.reverseBytes(source.readInt());
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
            this.entries.add(new DiskDataSource(this, sb.toString(), source, start, size));
        }

        // Sort them for convenience...
        Collections.sort(this.entries, new Comparator<DataSource>() {
            public int compare(DataSource o1, DataSource o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
    }

    public <T extends LabEntry> List<DataSource> findByType(Class<T> type) {
        List<DataSource> res = new LinkedList<DataSource>();
        for (DataSource edp : entries) {
            EntryCodec<?> codec = CodecMapper.codecForProvider(edp);
            if (codec == null || codec.getEntryClass() != type)
                continue;
            res.add(edp);
        }
        if (res.isEmpty())
            return null;
        return res;
    }

    public LabEntry findByName(String name) throws IOException {
        for (DataSource edp : entries) {
            if (edp.getName().equalsIgnoreCase(name)) {
                EntryCodec c = CodecMapper.codecForProvider(edp);
                if(c == null)
                    continue;
                edp.seek(0);
                return c.read(edp);
            }
        }
        return null;
    }

    public String toString() {
        return name;
    }
}