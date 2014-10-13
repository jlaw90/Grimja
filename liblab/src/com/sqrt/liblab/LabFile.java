/*
 * Copyright (C) 2014  James Lawrence.
 *
 *     This file is part of LibLab.
 *
 *     LibLab is free software: you can redistribute it and/or modify
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

package com.sqrt.liblab;

import com.sqrt.liblab.codec.CodecMapper;
import com.sqrt.liblab.codec.EntryCodec;
import com.sqrt.liblab.entry.LabEntry;
import com.sqrt.liblab.io.DataSource;
import com.sqrt.liblab.io.DiskDataSource;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * A LabFile is a container for LabEntry's
 */
public class LabFile {
    /**
     * The collection that this LabFile belongs to
     */
    public final LabCollection container;
    /**
     * The names and data for the entries of this LabFile
     */
    public final List<DataSource> entries = new LinkedList<DataSource>();
    private int version = 256;
    private String name;

    private File path;
    private RandomAccessFile source;

    LabFile(LabCollection container) {
        this.container = container;
        name = "New";
    }

    LabFile(LabCollection container, File path) throws IOException {
        this(container);
        this.path = path;
        name = path.getName().toUpperCase();
        try {
            source = new RandomAccessFile(path, "rw");
        } catch (IOException ioe) {
            source = new RandomAccessFile(path, "r");
        }
        int magic;
        if ((magic = source.readInt()) != (('L' << 24) | ('A' << 16) | ('B' << 8) | ('N')))
            throw new IOException(String.format("Invalid LAB file, magic: %08x", magic));
        version = source.readInt(); // Always 256?
        int entries = Integer.reverseBytes(source.readInt()); // should be uint but the chances of there being over 2147483647 entries is, I assume: slim
        int stringTableOffset = 16 * (entries + 1);

        for (int i = 0; i < entries; i++) {
            source.seek((i+1) * 16); // position...
            int nameOff = Integer.reverseBytes(source.readInt());
            int start = Integer.reverseBytes(source.readInt());
            int size = Integer.reverseBytes(source.readInt());
            // 4 bytes of 0

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
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns all the LabEntry's contained in this LabFile that have a model of the specified type
     * @param type the type of the model
     * @return a list containing the results
     */
    public List<DataSource> findByType(Class<? extends LabEntry> type) {
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

    /**
     * Fins the first LabEntry with the specified name
     * @param name the name to search for (case insensitive)
     * @return the entry or null if one was not found
     * @throws IOException
     */
    public LabEntry findByName(String name) throws IOException {
        for (DataSource edp : entries) {
            if (edp.getName().equalsIgnoreCase(name)) {
                EntryCodec c = CodecMapper.codecForProvider(edp);
                if(c == null)
                    continue;
                edp.position(0);
                return c.read(edp);
            }
        }
        return null;
    }

    public String toString() {
        return name;
    }

    public void save(File file) throws IOException {
        // Todo: save!
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        raf.writeInt((('L' << 24) | ('A' << 16) | ('B' << 8) | ('N')));
        raf.writeInt(version);
        raf.writeInt(Integer.reverseBytes(entries.size()));

        // Build string table first
        // This means we have to iterate twice, but we don't have to buffer all file data in memory
        ByteArrayOutputStream nameBuffer = new ByteArrayOutputStream();
        int[] nameIndices = new int[entries.size()];
        int stringTableSize = 0;
        for(int i = 0; i < entries.size(); i++) {
            nameIndices[i] = stringTableSize;
            byte[] nameData = entries.get(i).getName().getBytes();
            nameBuffer.write(nameData, 0, nameData.length);
            nameBuffer.write(0);
            stringTableSize += nameData.length + 1;
            nameData = null;
        }
        raf.writeInt(stringTableSize);

        int stringTableOff = ((entries.size()+1) * 16);

        int dataOff = stringTableOff + stringTableSize;

        byte[] buf = new byte[5000];
        for (int i = 0; i < entries.size(); i++) {
            raf.seek((i+1) * 16);
            DataSource d = entries.get(i);
            raf.writeInt(Integer.reverseBytes(nameIndices[i]));
            raf.writeInt(Integer.reverseBytes(dataOff));
            raf.writeInt(Integer.reverseBytes((int) d.length()));
            raf.writeInt(0);

            // Write data
            raf.seek(dataOff);
            d.position(0);
            while(d.remaining() > 0) {
                int read = (int) Math.min(d.remaining(), buf.length);
                d.get(buf, 0, read);
                raf.write(buf, 0, read);
                dataOff += read;
            }
        }

        // Write the string table
        raf.seek(stringTableOff);
        raf.write(nameBuffer.toByteArray());
        raf.close();
    }
}