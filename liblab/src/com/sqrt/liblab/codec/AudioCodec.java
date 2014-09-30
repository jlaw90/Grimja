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

package com.sqrt.liblab.codec;

import com.sqrt.liblab.entry.audio.*;
import com.sqrt.liblab.io.DataSource;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AudioCodec extends EntryCodec<Audio> {
    protected Audio _read(final DataSource source) throws IOException {
        String ext = source.getName().toLowerCase().substring(source.getName().length() - 3);
        Audio audio = new Audio(source.container, source.getName());
        if (ext.equals("wav") || ext.equals("imc")) {
            audio.stream = new McmpStream(source); // VIMA decompression stream...
            parseSoundHeader(audio, source);
        } else if (ext.equals("imu")) {
            final int headerSize = parseSoundHeader(audio, source);
            audio.stream = new AudioInputStream() {
                public void seek(int pos) throws IOException {
                    source.position(pos + headerSize);
                }

                public int read() throws IOException {
                    return source.getUByte();
                }

                public int read(byte[] dst, int off, int len) throws IOException {
                    source.get(dst, off, len);
                    return len;
                }
            };
        } else {
            System.err.println("Currently unsupported audio ext: " + ext);
            return null;
        }
        return audio;
    }

    public DataSource write(Audio source) throws IOException {
        throw new UnsupportedOperationException(); // Todo
    }

    public String[] getFileExtensions() {
        return new String[]{"wav", "imu", "imc"};
    }

    public Class<Audio> getEntryClass() {
        return Audio.class;
    }

    private int parseSoundHeader(Audio audio, final DataSource source) throws IOException {
        List<UnresolvedJump> unresolvedJumps = new LinkedList<UnresolvedJump>();
        int tag = source.getInt();
        if (tag == (('R' << 24) | ('I' << 16) | ('F' << 8) | 'F')) {
            Region main = new Region();
            audio.regions.add(main);
            main.offset = 0;
            source.skip(18);
            audio.channels = source.get();
            source.skip(1);
            audio.sampleRate = source.getIntLE();
            source.skip(6);
            audio.bits = source.get();
            source.skip(5);
            main.length = source.getIntLE();
        } else if (tag == (('i' << 24) | ('M' << 16) | ('U' << 8) | 'S')) {
            int size;
            final int headerStart = (int) source.position();
            source.skip(12);

            Map<Integer, String> unmappedComments = new HashMap<Integer, String>();
            do {
                tag = source.getInt();
                switch (tag) {
                    case (('F' << 24) | ('R' << 16) | ('M' << 8) | 'T'):
                        source.skip(12);
                        audio.bits = source.getInt();
                        audio.sampleRate = source.getInt();
                        audio.channels = source.getInt();
                        break;
                    case (('T' << 24) | ('E' << 16) | ('X' << 8) | 'T'):
                        size = source.getInt();
                        int offset = source.getInt();
                        String s = source.getString(size - 4);
                        if(!unmappedComments.containsKey(offset))
                            unmappedComments.put(offset, s);
                        else
                            unmappedComments.put(offset, unmappedComments.get(offset) + "\n" + s);
                        break;
                    case (('S' << 24) | ('T' << 16) | ('O' << 8) | 'P'):
                        size = source.getInt();
                        int off = source.getInt();
                        source.skip(size-4);
                        break;
                    case (('R' << 24) | ('E' << 16) | ('G' << 8) | 'N'):
                        source.skip(4);
                        Region r = new Region();
                        r.offset = source.getInt();
                        r.length = source.getInt();
                        audio.regions.add(r);
                        break;
                    case (('J' << 24) | ('U' << 16) | ('M' << 8) | 'P'):
                        source.skip(4);
                        Jump j = new Jump();
                        UnresolvedJump uj = new UnresolvedJump();
                        unresolvedJumps.add(uj);
                        uj.wrap = j;
                        uj.offset = source.getInt();
                        uj.dest = source.getInt();
                        j.hookId = source.getInt();
                        j.fadeDelay = source.getInt();
                        break;
                    case (('D' << 24) | ('A' << 16) | ('T' << 8) | 'A'):
                        source.skip(4);
                        break;
                    default:
                        System.err.println("Unknown MAP tag: " + tag);
                }
            } while (tag != (('D' << 24) | ('A' << 16) | ('T' << 8) | 'A'));
            final int headerSize = (int) (source.position() - headerStart);
            for (Region r : audio.regions) {
                r.comments = unmappedComments.remove(r.offset);
                r.offset -= headerSize;
            }
            // resolve jumps...
            for (UnresolvedJump uj : unresolvedJumps) {
                uj.offset -= headerSize;
                uj.dest -= headerSize;
                boolean foundSource = false;
                boolean foundTarget = false;
                for(Region r: audio.regions) {
                    if(r.offset == uj.offset) {
                        r.jumps.add(uj.wrap);
                        foundSource = true;
                    }
                    if(r.offset == uj.dest) {
                        uj.wrap.target = r;
                        foundTarget = true;
                    }
                    if(foundSource && foundTarget)
                        break;
                }
                if(!foundSource || !foundTarget)
                    System.err.println("Couldn't locate target or source for jump!");
            }
            if(!unmappedComments.isEmpty()) {
                System.err.println("Unmappable comments: ");
                for(Map.Entry<Integer,String> set: unmappedComments.entrySet())
                    System.err.println("\t" + set.getKey() + ": " + set.getValue());
            }
            return headerSize;
        } else {
            System.err.println("Invalid sound header: " + tagToString(tag));
        }
        return 44;
    }

    private class UnresolvedJump {
        Jump wrap;
        int offset, dest;
    }
}

class McmpStream extends AudioInputStream {
    private DataSource source;
    private CompressionEntry[] compEntries;
    private int entryIdx = 0, bufOff;
    private byte[] buf;
    private int headerSize;

    public McmpStream(DataSource src) throws IOException {
        this.source = new NullPaddedDataSource(src);
        if (source.getInt() != (('M' << 24) | ('C' << 16) | ('M' << 8) | 'P'))
            throw new IOException("Invalid MCMP format :S");
        int numCompItems = source.getUShort();
        int offset = (int) (source.position() + (numCompItems * 9) + 2);
        numCompItems--;
        source.skip(5);
        compEntries = new CompressionEntry[numCompItems];
        for (int i = 0; i < compEntries.length; i++)
            compEntries[i] = new CompressionEntry();
        headerSize = source.getInt();
        offset += headerSize;
        int i;
        for (i = 0; i < numCompItems; i++) {
            compEntries[i].codec = source.get();
            compEntries[i].decompressedSize = source.getInt();
            compEntries[i].compressedSize = source.getInt();
            compEntries[i].offset = offset;
            offset += compEntries[i].compressedSize;
        }
        int sizeCodecs = source.getUShort();
        for (i = 0; i < numCompItems; i++) {
            compEntries[i].offset += sizeCodecs;
        }
        source.skip(sizeCodecs);
        headerSize += (int) source.position();
    }

    public synchronized void seek(int pos) throws IOException {
        int calc = 0;
        for (int i = 0; i < compEntries.length; i++) {
            if(pos >= calc && pos < calc + compEntries[i].decompressedSize) {
                if(entryIdx != i)
                    buf = null; // Need a new buffer
                entryIdx = i;
                bufOff = pos - calc;
                break;
            }
            calc += compEntries[i].decompressedSize;
        }
    }

    public synchronized void reset() {
        entryIdx = 0;
        bufOff = 0;
        buf = null;
    }

    public synchronized int read() throws IOException {
        fillBuffer();
        if (buf == null)
            return -1; // EOF
        return buf[bufOff++] & 0xff;
    }

    public synchronized int read(byte[] dest, int off, int len) throws IOException {
        int read = 0;
        while (read < len) {
            fillBuffer();
            if (buf == null)
                return read == 0 ? -1 : read;
            int toRead = Math.min(len - read, buf.length - bufOff);
            System.arraycopy(buf, bufOff, dest, off + read, toRead);
            bufOff += toRead;
            read += toRead;
        }
        return read;
    }

    public void close() {
        reset();
    }

    private void fillBuffer() throws IOException {
        if (buf == null || bufOff >= buf.length) {
            buf = nextEntry();
            bufOff = 0;
        }
    }

    private synchronized byte[] nextEntry() throws IOException {
        if (entryIdx >= compEntries.length)
            return null;
        CompressionEntry entry = compEntries[entryIdx++];
        source.position(entry.offset);
        return Vima.decompress(source, entry.decompressedSize).toByteArray();
    }
}

class CompressionEntry {
    public byte codec;
    public int decompressedSize;
    public int compressedSize;
    public int offset;

    public String toString() {
        return offset + " - " + offset + decompressedSize;
    }
}

class NullPaddedDataSource extends DataSource {
    private DataSource source;

    public NullPaddedDataSource(DataSource source) {
        super(source.container, source.getName());
        this.source = source;
    }

    public void position(long pos) throws IOException {
        source.position(pos);
    }

    public long position() throws IOException {
        return source.position();
    }

    public long length() {
        return source.length();
    }

    @Override
    public void get(byte[] b, int off, int len) throws IOException {
        source.get(b, off, len);
    }

    @Override
    public void put(byte[] b, int off, int len) throws IOException {
        source.put(b, off, len);
    }

    public byte get() throws IOException {
        return position() < length() ? source.get() : 0;
    }

    @Override
    public void put(byte b) throws IOException {
        source.put(b);
    }

    public int hashCode() {
        return source.hashCode() + 1;
    }
}