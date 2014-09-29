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
import com.sqrt.liblab.threed.Angle;
import com.sqrt.liblab.threed.Vector2f;
import com.sqrt.liblab.threed.Vector3f;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Provides an abstract way to read data from an unspecified source
 */
public abstract class DataSource {
    /**
     * The LabFile that we are providing data from/for
     */
    public final LabFile container;
    private String name;
    private long mark;
    private DataSourceInputStream in;
    private DataSourceOutputStream out;

    /**
     * The only constructor.  Enforces implementations to provide a container and a name
     * @param container the container of the data
     * @param name the name of the entry
     */
    protected DataSource(LabFile container, String name) {
        this.container = container;
        this.name = name;
    }

    /**
     * Returns the name of the entry we provide data for
     * @return the name of the entry we provider data for
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the entry we provide data for
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the position we are currently at in the data stream
     * @return the position
     * @throws IOException
     */
    public abstract long position() throws IOException;

    /**
     * Seeks to the specified position in the data stream
     * @param pos the position from the beginning of this source
     * @throws IOException
     */
    public abstract void position(long pos) throws IOException;

    /**
     * Skips the specified number of bytes in the DataSource
     * @param count the number of bytes to advance over
     * @throws IOException
     */
    public void skip(long count) throws IOException {
        position(position() + count);
    }

    /**
     * Returns the total length of the data stream
     * @return the total length of the data stream
     */
    public abstract long limit();

    /**
     * Returns the number of bytes remaining in the data stream to be consumed
     * @return the number of bytes remaining in the data stream to be consumed
     * @throws IOException
     */
    public long remaining() throws IOException {
        return limit() - position();
    }

    /**
     * Reads a null-padded string from the stream of the specified length
     * @param maxLen the number of bytes to read from the stream
     * @return the string with all nulls removed
     * @throws IOException
     */
    public String getString(int maxLen) throws IOException {
        byte[] data = new byte[maxLen];
        get(data);
        String s = new String(data);
        int idx = s.indexOf(0);
        if (idx != -1)
            s = s.substring(0, idx);
        return s;
    }

    /**
     * Writes a null-padded string to this DataSource
     * @param s the String
     * @throws IOException
     */
    public void putString(String s, int len) throws IOException {
        byte[] data = s.getBytes();
        int e = Math.min(data.length, len);
        put(data, 0, e);
    }

    /**
     * Returns a string containing all encountered bytes up to the next \n character
     * @return the string
     * @throws IOException
     */
    public String getLine() throws IOException {
        StringBuilder sb = new StringBuilder();
        while(remaining() > 0) {
            char c = (char) get();
            if(c == '\n')
                break;
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * Reads len bytes from the stream, or dies trying
     * @param b the destination for the data we read
     * @param off the offset into the destination to start placing the data
     * @param len the number of bytes to read into the destination
     * @throws IOException
     */
    public abstract void get(byte[] b, int off, int len) throws IOException;

    /**
     * Writes len bytes to the stream
     * @param b the data to write
     * @param off the offset into the supplied data that we start copying from
     * @param len the number of bytes to write
     * @throws IOException
     */
    public abstract void put(byte[] b, int off, int len) throws IOException;

    /**
     * Reads dest.length bytes into dest from index 0
     * @param dest the destination of the read data
     * @throws IOException
     */
    public void get(byte[] dest) throws IOException {
        get(dest, 0, dest.length);
    }

    /**
     * Writes data.length bytes to this stream
     * @param data the bytes to write
     * @throws IOException
     */
    public void put(byte[] data) throws IOException {
        put(data, 0, data.length);
    }

    /**
     * Reads a byte from the stream
     * @return a byte
     * @throws IOException
     */
    public abstract byte get() throws IOException;

    /**
     * Writes a byte to the stream
     * @param b the byte to write
     * @throws IOException
     */
    public abstract void put(byte b) throws IOException;

    /**
     * Reads an unsigned byte from the stream
     * @return and unsigned byte
     * @throws IOException
     */
    public int getUByte() throws IOException {
        return get() & 0xff;
    }

    /**
     * Reads an integer from the stream
     * @return an integer
     * @throws IOException
     */
    public int getInt() throws IOException {
        int c1 = getUByte(), c2 = getUByte(), c3 = getUByte(), c4 = getUByte();
        return (c1 << 24) | (c2 << 16) | (c3 << 8) | c4;
    }

    /**
     * Writes an integer to the stream
     * @param i the integer to write
     * @throws IOException
     */
    public void putInt(int i) throws IOException {
        put((byte) ((i >> 24) & 0xff));
        put((byte) ((i >> 16) & 0xff));
        put((byte) ((i >> 8) & 0xff));
        put((byte) (i & 0xff));
    }

    /**
     * Reads a boolean from the stream (getIntLE() != 0).  Not efficient, but how grimE formats seem to work.
     * @return the boolean value
     * @throws IOException
     */
    public boolean getBoolean() throws IOException {
        return getIntLE() != 0; // As grimE does it...
    }

    /**
     * Writes a boolean to the data stream in grimE format
     * @param b the boolean to write
     */
    public void putBoolean(boolean b) throws IOException {
        putIntLE(b? 1: 0);
    }

    /**
     * Reads an integer from the stream with little endian ordering
     * @return an integer
     * @throws IOException
     */
    public int getIntLE() throws IOException {
        return Integer.reverseBytes(getInt());
    }

    /**
     * Writes an integer to the stream with little endian ordering
     * @param i the integer to write
     * @throws IOException
     */
    public void putIntLE(int i) throws IOException{
        putInt(Integer.reverseBytes(i));
    }

    /**
     * Reads an unsigned integer from the stream with little endian byte ordering
     * @return an integer
     * @throws IOException
     */
    public long getUIntLE() throws IOException {
        return (long) getIntLE() & 0xffffffffL;
    }

    /**
     * Reads a short from the stream
     * @return a short
     * @throws IOException
     */
    public short getShort() throws IOException {
        int c1 = getUByte(), c2 = getUByte();
        if ((c1 | c2) < 0)
            throw new EOFException();
        return (short) ((c1 << 8) | c2);
    }

    /**
     * Writes a short to the stream
     * @param s the short to write
     * @throws IOException
     */
    public void putShort(short s) throws IOException {
        put((byte) ((s >> 8) & 0xff));
        put((byte) (s & 0xff));
    }

    /**
     * Reads an unsigned short from the stream
     * @return an unsigned short
     * @throws IOException
     */
    public int getUShort() throws IOException {
        return getShort() & 0xffff;
    }

    /**
     * Reads a short from the stream in little endian byte order
     * @return a short
     * @throws IOException
     */
    public short getShortLE() throws IOException {
        return Short.reverseBytes(getShort());
    }

    /**
     * Writes a short to the stream in little endian byte order
     * @param s the short to write
     * @throws IOException
     */
    public void putShortLE(short s) throws IOException {
        putShort(Short.reverseBytes(s));
    }

    /**
     * Returns an unsigned short from the stream in little endian byte ordering
     * @return an unsigned short
     * @throws IOException
     */
    public int getUShortLE() throws IOException {
        return getShortLE() & 0xffff;
    }

    /**
     * Reads a float from the stream in little endian format
     * @return a float
     * @throws IOException
     */
    public float getFloatLE() throws IOException {
        return Float.intBitsToFloat(getIntLE());
    }

    /**
     * Writes a float to the stream in little endian byte order
     * @param f
     */
    public void putFloatLE(float f) throws IOException {
        putIntLE(Float.floatToIntBits(f));
    }

    /**
     * Reads an angle from the stream (a float encapsulated in an Angle object)
     * @return the angle
     * @throws IOException
     */
    public Angle getAngle() throws IOException {
        return new Angle(getFloatLE());
    }

    /**
     * Writes an angle to the data stream
     * @param a the angle to write
     */
    public void putAngle(Angle a) throws IOException {
        putFloatLE(a.degrees);
    }

    /**
     * Reads a vector2 from the stream (2 floats encapsulated in a Vector2f)
     * @return the vector
     * @throws IOException
     */
    public Vector2f getVector2f() throws IOException {
        return new Vector2f(getFloatLE(), getFloatLE());
    }

    /**
     * Writes a vector2f to the stream
     * @param v the vector to write
     * @throws IOException
     */
    public void putVector2f(Vector2f v) throws IOException {
        putFloatLE(v.x);
        putFloatLE(v.y);
    }

    /**
     * Reads a 3D vector from the stream (3 floats)
     * @return the vector
     * @throws IOException
     */
    public Vector3f getVector3f() throws IOException {
        return new Vector3f(getFloatLE(), getFloatLE(), getFloatLE());
    }

    /**
     * Writes a 3d vector to the stream
     * @param v the vector to write
     */
    public void putVector3f(Vector3f v) throws IOException {
        putFloatLE(v.x);
        putFloatLE(v.y);
        putFloatLE(v.z);
    }

    public synchronized void mark(int readlimit) throws IOException {
        mark = position();
    }

    public synchronized void reset() throws IOException {
        position(mark);
    }

    public boolean markSupported() {
        return true;
    }

    public InputStream asInputStream() {
        if(in == null)
            in = new DataSourceInputStream();
        return in;
    }

    public OutputStream asOutputStream() {
        if(out == null)
            out = new DataSourceOutputStream();
        return out;
    }

    public String toString() {
        return name;
    }

    private class DataSourceInputStream extends InputStream {
        public int read() throws IOException {
            return remaining() < 0? -1: getUByte();
        }

        public int read(byte[] b, int off, int len) throws IOException {
            int l = (int) Math.min(remaining(), len);
            if(l == 0)
                return -1;
            get(b, off, l);
            return l;
        }

        public long skip(long n) throws IOException {
            DataSource.this.skip(n);
            return n;
        }

        public int available() throws IOException {
            return (int) DataSource.this.remaining();
        }

        public synchronized void mark(int readlimit) {
            try {
                DataSource.this.mark(readlimit);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public synchronized void reset() throws IOException {
            DataSource.this.reset();
        }

        public boolean markSupported() {
            return DataSource.this.markSupported();
        }
    }

    private class DataSourceOutputStream extends OutputStream {
        public void write(int b) throws IOException {
            put((byte) b);
        }

        public void write(byte[] b, int off, int len) throws IOException {
            put(b, off, len);
        }
    }
}