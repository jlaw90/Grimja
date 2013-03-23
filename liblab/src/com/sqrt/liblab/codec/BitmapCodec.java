package com.sqrt.liblab.codec;

import com.sqrt.liblab.EntryDataProvider;
import com.sqrt.liblab.entry.graphics.GrimBitmap;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.BufferOverflowException;

public class BitmapCodec extends EntryCodec<GrimBitmap> {
    public GrimBitmap _read(EntryDataProvider source) throws IOException {
        int tag = source.readInt();
        switch(tag) {
            case ('B' << 24) | ('M' << 16) | (' ' << 8) | ' ':
                return readBm(source);
            default:
                throw new IOException("Unknown bitmap format");
        }
    }

    private GrimBitmap readBm(EntryDataProvider source) throws IOException {
        int tag = source.readInt();
        if(tag != ('F' << 24))
            throw new IllegalArgumentException("Unknown BM tag");
        GrimBitmap result = new GrimBitmap(source.container, source.getName());
        int codec = source.readIntLE();
        int paletteIncluded = source.readIntLE();
        int numImages = source.readIntLE();
        int x = source.readIntLE();
        int y = source.readIntLE();
        int transparentColor = source.readIntLE();
        int format = source.readIntLE();
        int bpp = source.readIntLE();
        int redBits = source.readIntLE();
        int greenBits = source.readIntLE();
        int blueBits = source.readIntLE();
        int redShift = source.readIntLE();
        int greenShift = source.readIntLE();
        int blueShift = source.readIntLE();

        source.seek(128);
        int width = source.readIntLE();
        int height = source.readIntLE();
        source.seek(128);
        byte[][] data = new byte[numImages][];
        int expectedDataLength = width*height*(bpp/8);
        for(int i = 0; i < numImages; i++) {
            source.skip(8);
            data[i] = new byte[expectedDataLength];
            if(codec == 0)
                source.readFully(data[i]);
            else if(codec == 3) {
                int compressedLen = source.readIntLE();
                byte[] compressed = new byte[compressedLen];
                source.readFully(compressed);
                data[i] = decompress_codec3(compressed, expectedDataLength);
            } else throw new UnsupportedOperationException("Invalid image codec");

            if(data[i].length != expectedDataLength)
                throw new UnsupportedOperationException("Invalid data length, got: " + data[i].length + ", expected " + (width*height*(bpp/8)) + " with " + bpp + "bpp");
            // convert to 32-bit RGBA format
            int[] rgba = new int[width*height];
            if(format == 1) {
                for(int j = 0; j < expectedDataLength; j += 2) {
                    byte t = data[i][j];
                    data[i][j] = data[i][j+1];
                    data[i][j+1] = t;
                }
            }
            for(int j = 0; j < expectedDataLength; j+=2) {
                int pixel = ((data[i][j] & 0xff) << 8) | (data[i][j+1] & 0xff);
                int r = (pixel >>> redShift) & ((1 << redBits) - 1);
                int g = (pixel >>> greenShift) & ((1 << greenBits) - 1);
                int b = (pixel >>> blueShift) & ((1 << blueBits) - 1);
                int rr = (r << (8-redBits));
                int rg = (g << (8-greenBits));
                int rb = (b << (8-blueBits));
                int res = (rr << 16) | (rg << 8) | rb;
                rgba[j/2] = ((res == 0xf800f8? 0: 0xff) << 24) | res;
            }
            BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            bi.setRGB(0, 0, width, height, rgba, 0, width);
            result.images.add(bi);
        }
        return result;
    }

    public EntryDataProvider write(GrimBitmap source) throws IOException {
        throw new UnsupportedOperationException();
    }

    public String[] getFileExtensions() {
        return new String[]{"bm", "zbm"};
    }

    public byte[][] getFileHeaders() {
        return new byte[][]{
                {'B', 'M', ' ', ' '}
        };
    }

    public Class<GrimBitmap> getEntryClass() {
        return GrimBitmap.class;
    }

    private static byte[] decompress_codec3(byte[] compressed, int max) throws EOFException {
        AccessibleByteArrayOutputStream result = new AccessibleByteArrayOutputStream();
        DecompressorStream str = new DecompressorStream(compressed);
        while(true) {
            int copyOne = str.readBit();
            if(copyOne == 1) { // Copy a single byte...
                result.write(str.read());
                if(result.size() > max)
                    throw new BufferOverflowException();
            } else { // Copy multiple bytes...
                int bit = str.readBit();
                int copy_len, copy_offset;
                if(bit == 0) { // Copy multiple bytes...
                    copy_len = 2 * str.readBit() + str.readBit() + 3;
                    copy_offset = str.read() - 256;
                } else {
                    int a = str.read();
                    int b = str.read();
                    copy_offset = (a | ((b & 0xf0) << 4)) - 4096;
                    copy_len = (b & 0xf) + 3;
                    if(copy_len == 3) {
                        copy_len = str.read() + 1;
                        if(copy_len == 1)
                            break; // the end!
                    }
                }

                // Do the copy...
                for(int i = 0; i < copy_len; i++)
                    result.write(result.get(result.size() + copy_offset));
            }
            if(result.size() > max)
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
        if(bitstr_len == 0)
            nextBitString();
        return res;
    }
}

class AccessibleByteArrayOutputStream extends ByteArrayOutputStream {
    public byte get(int index) {
        return buf[index];
    }
}