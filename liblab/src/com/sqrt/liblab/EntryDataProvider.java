package com.sqrt.liblab;

import com.sqrt.liblab.codec.CodecMapper;
import com.sqrt.liblab.threed.Angle;
import com.sqrt.liblab.threed.Vector2;
import com.sqrt.liblab.threed.Vector3;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

// Todo: prettier name...
/***
 * Provides an abstract way to handle data caching (e.g., read from disk originally, store in memory if modified, etc.)
 */
public abstract class EntryDataProvider extends InputStream {
    public final LabFile container;
    private String name;

    protected EntryDataProvider(LabFile container, String name) {
        this.container = container;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public abstract void seek(long pos) throws IOException;

    public abstract long getPosition();

    public String readString(int maxLen) throws IOException {
        byte[] data = new byte[maxLen];
        readFully(data);
        String s = new String(data);
        int idx = s.indexOf(0);
        if(idx != -1)
            s = s.substring(0, idx);
        return s;
    }

    public void readFully(byte[] b, int off, int len) throws IOException {
        while (len > 0) {
            int read = read(b, off, len);
            if (read < 0)
                throw new EOFException();
            off += read;
            len -= read;
        }
    }

    public void readFully(byte[] dest) throws IOException {
        readFully(dest, 0, dest.length);
    }

    public byte readByte() throws IOException {
        int i = read();
        if (i < 0)
            throw new EOFException();
        return (byte) i;
    }

    public int readUByte() throws IOException {
        return readByte() & 0xff;
    }

    public int readInt() throws IOException {
        int c1 = read(), c2 = read(), c3 = read(), c4 = read();
        if ((c1 | c2 | c3 | c4) < 0)
            throw new EOFException();
        return (c1 << 24) | (c2 << 16) | (c3 << 8) | c4;
    }

    public int readIntLE() throws IOException {
        return Integer.reverseBytes(readInt());
    }

    public long readUInt() throws IOException {
        return ((long) readInt()) & 0xffffffffL;
    }

    public long readUIntLE() throws IOException {
        return (long) Integer.reverseBytes(readInt()) & 0xffffffffL;
    }

    public short readShort() throws IOException {
        int c1 = read(), c2 = read();
        if ((c1 | c2) < 0)
            throw new EOFException();
        return (short) ((c1 << 8) | c2);
    }

    public short readShortLE() throws IOException {
        return Short.reverseBytes(readShort());
    }

    public int readUShortLE() throws IOException {
        return Short.reverseBytes(readShort()) & 0xffff;
    }

    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readIntLE());
    }

    public Angle readAngle() throws IOException {
        return new Angle(readFloat());
    }

    public Vector2 readVector2() throws IOException {
        return new Vector2(readFloat(), readFloat());
    }

    public Vector3 readVector3() throws IOException {
        return new Vector3(readFloat(), readFloat(), readFloat());
    }

    public String toString() {
        return name;
    }
}