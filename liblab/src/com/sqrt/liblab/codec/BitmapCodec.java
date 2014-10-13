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

package com.sqrt.liblab.codec;

import com.sqrt.liblab.entry.graphics.GrimBitmap;
import com.sqrt.liblab.io.DataSource;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.BufferOverflowException;

public class BitmapCodec extends EntryCodec<GrimBitmap> {
    public GrimBitmap _read(DataSource source) throws IOException {
        int tag = source.getInt();
        switch (tag) {
            case ('B' << 24) | ('M' << 16) | (' ' << 8) | ' ':
                return readBm(source);
            default:
                throw new IOException("Unknown bitmap format");
        }
    }

    private GrimBitmap readBm(DataSource source) throws IOException {
        int tag = source.getInt();
        if (tag != ('F' << 24))
            throw new IllegalArgumentException("Unknown BM tag");
        GrimBitmap result = new GrimBitmap(source.container, source.getName());
        int codec = source.getIntLE();
        int paletteIncluded = source.getIntLE();
        int numImages = source.getIntLE();
        int x = source.getIntLE();
        int y = source.getIntLE();
        int transparentColor = source.getIntLE();
        int format = source.getIntLE();
        int bpp = source.getIntLE();
        int redBits = source.getIntLE();
        int greenBits = source.getIntLE();
        int blueBits = source.getIntLE();
        int redShift = source.getIntLE();
        int greenShift = source.getIntLE();
        int blueShift = source.getIntLE();

        source.position(128);
        int width = source.getIntLE();
        int height = source.getIntLE();
        source.position(128);
        byte[][] data = new byte[numImages][];
        int expectedDataLength = width * height * (bpp / 8);
        for (int i = 0; i < numImages; i++) {
            source.skip(8);
            data[i] = new byte[expectedDataLength];
            if (codec == 0)
                source.get(data[i]);
            else if (codec == 3) {
                int compressedLen = source.getIntLE();
                byte[] compressed = new byte[compressedLen];
                source.get(compressed);
                data[i] = decompress_codec3(compressed, expectedDataLength);
            } else throw new UnsupportedOperationException("Invalid image codec: " + codec);

            if (data[i].length != expectedDataLength)
                throw new UnsupportedOperationException("Invalid data length, got: " + data[i].length + ", expected " + (width * height * (bpp / 8)) + " with " + bpp + "bpp");
            if (format == 1) {
                for (int j = 0; j < expectedDataLength; j += 2) {
                    byte t = data[i][j];
                    data[i][j] = data[i][j + 1];
                    data[i][j + 1] = t;
                }
            }
            short[] rgb565 = new short[width * height];
            for (int k = 0; k < expectedDataLength; k += 2)
                rgb565[k / 2] = (short) (((data[i][k] & 0xff) << 8) | (data[i][k + 1] & 0xff));
            BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_USHORT_565_RGB);
            WritableRaster raster = bi.getRaster();
            raster.setDataElements(0, 0, width, height, rgb565);
            result.images.add(bi);
        }
        return result;
    }

    public DataSource write(GrimBitmap source) throws IOException {
        throw new UnsupportedOperationException(); // Todo
    }

    public String[] getFileExtensions() {
        return new String[]{"bm", "zbm"};
    }

    public Class<GrimBitmap> getEntryClass() {
        return GrimBitmap.class;
    }

    private static byte[] decompress_codec3(byte[] compressed, int max) throws EOFException {
        AccessibleByteArrayOutputStream result = new AccessibleByteArrayOutputStream();
        DecompressorStream str = new DecompressorStream(compressed);
        while (true) {
            int copyOne = str.readBit();
            if (copyOne == 1) { // Copy a single byte...
                result.write(str.read());
                if (result.size() > max)
                    throw new BufferOverflowException();
            } else { // Copy multiple bytes...
                int bit = str.readBit();
                int copy_len, copy_offset;
                if (bit == 0) { // Copy multiple bytes...
                    copy_len = 2 * str.readBit() + str.readBit() + 3;
                    copy_offset = str.read() - 256;
                } else {
                    int a = str.read();
                    int b = str.read();
                    copy_offset = (a | ((b & 0xf0) << 4)) - 4096;
                    copy_len = (b & 0xf) + 3;
                    if (copy_len == 3) {
                        copy_len = str.read() + 1;
                        if (copy_len == 1)
                            break; // the end!
                    }
                }

                // Do the copy...
                for (int i = 0; i < copy_len; i++)
                    result.write(result.get(result.size() + copy_offset));
            }
            if (result.size() > max)
                throw new BufferOverflowException();
        }
        return result.toByteArray();
    }
}

class DecompressorStream extends ByteArrayInputStream {
    private int bitstr_len, bitstr_value;

    public DecompressorStream(byte[] buf) throws EOFException {
        super(buf);
        nextBitString();
    }

    private void nextBitString() throws EOFException {
        int c1 = read(), c2 = read();
        if ((c1 | c2) < 0)
            throw new EOFException();
        bitstr_value = ((c2 << 8) | c1);
        bitstr_len = 16;
    }

    public int readBit() throws EOFException {
        int res = bitstr_value & 1;
        bitstr_len--;
        bitstr_value >>>= 1;
        if (bitstr_len == 0)
            nextBitString();
        return res;
    }
}

class AccessibleByteArrayOutputStream extends ByteArrayOutputStream {
    public byte get(int index) {
        return buf[index];
    }
}