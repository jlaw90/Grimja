package com.sqrt.liblab.io;

import com.sqrt.liblab.LabFile;
import com.sqrt.liblab.threed.Angle;
import com.sqrt.liblab.threed.Vector2f;
import com.sqrt.liblab.threed.Vector3f;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Provides an abstract way to read data from an unspecified source
 */
public abstract class DataSource extends InputStream {
    /**
     * The LabFile that we are providing data from/for
     */
    public final LabFile container;
    private String name;

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
     * Seeks to the specified position in the data stream
     * @param pos the position from the beginning of this source
     * @throws IOException
     */
    public abstract void seek(long pos) throws IOException;

    /**
     * Returns the position we are currently at in the data stream
     * @return the position
     * @throws IOException
     */
    public abstract long getPosition() throws IOException;

    /**
     * Returns the total length of the data stream
     * @return the total length of the data stream
     */
    public abstract long getLength();

    /**
     * Returns the number of bytes remaining in the data stream to be consumed
     * @return the number of bytes remaining in the data stream to be consumed
     * @throws IOException
     */
    public final long getRemaining() throws IOException {
        return getLength() - getPosition();
    }

    /**
     * Reads a null-padded string from the stream of the specified length
     * @param maxLen the number of bytes to read from the stream
     * @return the string with all nulls removed
     * @throws IOException
     */
    public String readString(int maxLen) throws IOException {
        byte[] data = new byte[maxLen];
        readFully(data);
        String s = new String(data);
        int idx = s.indexOf(0);
        if (idx != -1)
            s = s.substring(0, idx);
        return s;
    }

    /**
     * Returns a string containing all encountered bytes up to the next \n character
     * @return the string
     * @throws IOException
     */
    public String readLine() throws IOException {
        StringBuilder sb = new StringBuilder();
        while(getRemaining() > 0) {
            char c = (char) readByte();
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
    public void readFully(byte[] b, int off, int len) throws IOException {
        while (len > 0) {
            int read = read(b, off, len);
            if (read < 0)
                throw new EOFException();
            off += read;
            len -= read;
        }
    }

    /**
     * Reads dest.length bytes into dest from index 0
     * @param dest the destination of the read data
     * @throws IOException
     */
    public void readFully(byte[] dest) throws IOException {
        readFully(dest, 0, dest.length);
    }

    /**
     * Reads a byte from the stream
     * @return a byte
     * @throws IOException
     */
    public byte readByte() throws IOException {
        int i = read();
        if (i < 0)
            throw new EOFException();
        return (byte) i;
    }

    /**
     * Reads an unsigned byte from the stream
     * @return and unsigned byte
     * @throws IOException
     */
    public int readUnsignedByte() throws IOException {
        return readByte() & 0xff;
    }

    /**
     * Reads an integer from the stream
     * @return an integer
     * @throws IOException
     */
    public int readInt() throws IOException {
        int c1 = read(), c2 = read(), c3 = read(), c4 = read();
        if ((c1 | c2 | c3 | c4) < 0)
            throw new EOFException();
        return (c1 << 24) | (c2 << 16) | (c3 << 8) | c4;
    }

    /**
     * Reads a boolean from the stream (readIntLE() != 0).  Not efficient, but how grimE formats seem to work.
     * @return the boolean value
     * @throws IOException
     */
    public boolean readBoolean() throws IOException {
        return readIntLE() != 0; // As grimE does it...
    }

    /**
     * Reads an integer from the stream with little endian ordering
     * @return an integer
     * @throws IOException
     */
    public int readIntLE() throws IOException {
        return Integer.reverseBytes(readInt());
    }

    /**
     * Reads an unsigned integer from the stream with little endian byte ordering
     * @return an integer
     * @throws IOException
     */
    public long readUnsignedIntLE() throws IOException {
        return (long) readIntLE() & 0xffffffffL;
    }

    /**
     * Reads a short from the stream
     * @return a short
     * @throws IOException
     */
    public short readShort() throws IOException {
        int c1 = read(), c2 = read();
        if ((c1 | c2) < 0)
            throw new EOFException();
        return (short) ((c1 << 8) | c2);
    }

    /**
     * Reads an unsigned short from the stream
     * @return an unsigned short
     * @throws IOException
     */
    public int readUnsignedShort() throws IOException {
        return readShort() & 0xffff;
    }

    /**
     * Reads a short from the stream in little endian byte order
     * @return a short
     * @throws IOException
     */
    public short readShortLE() throws IOException {
        return Short.reverseBytes(readShort());
    }

    /**
     * Returns an unsigned short from the stream in little endian byte ordering
     * @return an unsigned short
     * @throws IOException
     */
    public int readUnsignedShortLE() throws IOException {
        return readShortLE() & 0xffff;
    }

    /**
     * Reads a float from the stream in little endian format
     * @return a float
     * @throws IOException
     */
    public float readFloatLE() throws IOException {
        return Float.intBitsToFloat(readIntLE());
    }

    /**
     * Reads an angle from the stream (a float encapsulated in an Angle object)
     * @return the angle
     * @throws IOException
     */
    public Angle readAngle() throws IOException {
        return new Angle(readFloatLE());
    }

    /**
     * Reads a vector2 from the stream (2 floats encapsulated in a Vector2f)
     * @return the vector
     * @throws IOException
     */
    public Vector2f readVector2f() throws IOException {
        return new Vector2f(readFloatLE(), readFloatLE());
    }

    /**
     * Reads a 3D vector from the stream (3 floats)
     * @return the vector
     * @throws IOException
     */
    public Vector3f readVector3f() throws IOException {
        return new Vector3f(readFloatLE(), readFloatLE(), readFloatLE());
    }

    public String toString() {
        return name;
    }
}