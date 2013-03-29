package com.sqrt.liblab.codec;

import com.sqrt.liblab.io.DataSource;
import com.sqrt.liblab.io.SeekableByteArrayOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Vima {
    public static ByteArrayOutputStream decompress(DataSource source, int len) throws IOException {
        int numChannels = 1;
        byte[] sBytes = new byte[2];
        short[] sWords = new short[2];

        // Left step index hint
        sBytes[0] = source.readByte();
        if ((sBytes[0] & 0x80) != 0) { // Now in stereo!
            sBytes[0] = (byte) ~sBytes[0];
            numChannels = 2;
        }
        // Left PCM hint
        sWords[0] = source.readShort();
        if (numChannels > 1) {
            sBytes[1] = source.readByte(); // Right step index hint
            sWords[1] = source.readShort(); // Right PCM hint
        }

        int numSamples = len / (numChannels * 2);
        SeekableByteArrayOutputStream ibaos = new SeekableByteArrayOutputStream(8192);
        int bits = source.readShort();
        int bitPtr = 0;

        for (int channel = 0; channel < numChannels; channel++) {
            ibaos.seek(channel * 2);
            int stepIndexHint = sBytes[channel];
            int pcm = sWords[channel];

            for (int sample = 0; sample < numSamples; sample++) {
                int numBits = imcTable2[stepIndexHint];
                bitPtr += numBits;
                int highBit = 1 << (numBits - 1);
                int lowBits = highBit - 1;
                int val = (bits >> (16 - bitPtr)) & (highBit | lowBits);

                if (bitPtr > 7) { // refresh...
                    bits = ((bits & 0xff) << 8) | source.readUnsignedByte();
                    bitPtr -= 8;
                }

                if ((val & highBit) != 0)
                    val ^= highBit;
                else
                    highBit = 0;

                if (val == lowBits) {
                    pcm = (short) ((bits << bitPtr) & 0xffffff00);
                    bits = ((bits & 0xff) << 8) | source.readUnsignedByte();
                    pcm |= ((bits >>> (8 - bitPtr)) & 0xff);
                    bits = ((bits & 0xff) << 8) | source.readUnsignedByte();
                } else {
                    int delta = predict_table[(val << (7 - numBits)) | (stepIndexHint << 6)];

                    if (val != 0)
                        delta += (imcTable1[stepIndexHint] >>> (numBits - 1));
                    if (highBit != 0)
                        delta = -delta;

                    pcm += delta;
                    if (pcm < Short.MIN_VALUE)
                        pcm = Short.MIN_VALUE;
                    else if (pcm > Short.MAX_VALUE)
                        pcm = Short.MAX_VALUE;
                }

                ibaos.write((pcm >>> 8) & 0xff);
                ibaos.write(pcm & 0xff);
                ibaos.skip((numChannels-1)*2); // 1 channel? skip 0 bytes, 2 channels? skip 2 bytes... etc.

                stepIndexHint += offsets[numBits - 2][val];

                if (stepIndexHint < 0)
                    stepIndexHint = 0;
                else if (stepIndexHint > 88)
                    stepIndexHint = 88;
            }
        }
        return ibaos;
    }

    private static final short[] imcTable1 = {
            7, 8, 9, 10, 11, 12, 13, 14, 16, 17, 19, 21, 23, 25, 28, 31, 34,
            37, 41, 45, 50, 55, 60, 66, 73, 80, 88, 97, 107, 118, 130, 143,
            157, 173, 190, 209, 230, 253, 279, 307, 337, 371, 408, 449, 494,
            544, 598, 658, 724, 796, 876, 963, 1060, 1166, 1282, 1411, 1552,
            1707, 1878, 2066, 2272, 2499, 2749, 3024, 3327, 3660, 4026,
            4428, 4871, 5358, 5894, 6484, 7132, 7845, 8630, 9493, 10442,
            11487, 12635, 13899, 15289, 16818, 18500, 20350, 22385, 24623,
            27086, 29794, 32767
    };
    private static final byte[] imcTable2 = {
            4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
            4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
            4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6,
            6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7
    };

    private static final byte[] imcOtherTable1 = {
            -1, 4, -1, 4
    };

    private static final byte[] imcOtherTable2 = {
            -1, -1, 2, 6, -1, -1, 2, 6
    };

    private static final byte[] imcOtherTable3 = {
            -1, -1, -1, -1, 1, 2, 4, 6,
            -1, -1, -1, -1, 1, 2, 4, 6
    };

    private static final byte[] imcOtherTable4 = {
            -1, -1, -1, -1, -1, -1, -1, -1,
            1,  1,  1,  2,  2,  4,  5,  6,
            -1, -1, -1, -1, -1, -1, -1, -1,
            1,  1,  1,  2,  2,  4,  5,  6
    };

    private static final byte[] imcOtherTable5 = {
            -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1,
            1,  1,  1,  1,  1,  2,  2,  2,
            2,  4,  4,  4,  5,  5,  6,  6,
            -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1,
            1,  1,  1,  1,  1,  2,  2,  2,
            2,  4,  4,  4,  5,  5,  6,  6
    };

    private static final byte[] imcOtherTable6 = {
            -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1,
            1,  1,  1,  1,  1,  1,  1,  1,
            1,  1,  2,  2,  2,  2,  2,  2,
            2,  2,  4,  4,  4,  4,  4,  4,
            5,  5,  5,  5,  6,  6,  6,  6,
            -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1,
            1,  1,  1,  1,  1,  1,  1,  1,
            1,  1,  2,  2,  2,  2,  2,  2,
            2,  2,  4,  4,  4,  4,  4,  4,
            5,  5,  5,  5,  6,  6,  6,  6
    };
    private static final byte[][] offsets = {
            imcOtherTable1, imcOtherTable2, imcOtherTable3,
            imcOtherTable4, imcOtherTable5, imcOtherTable6
    };


    private static int[] predict_table = new int[5786];

    static {
        int destTableStartPos, incer;

        for (destTableStartPos = 0, incer = 0; destTableStartPos < 64; destTableStartPos++, incer++) {
            int destTablePos, imcTable1Pos;
            for (imcTable1Pos = 0, destTablePos = destTableStartPos;
                 imcTable1Pos < imcTable1.length; imcTable1Pos++, destTablePos += 64) {
                int put = 0, count, tableValue;
                for (count = 32, tableValue = imcTable1[imcTable1Pos]; count != 0; count >>>= 1, tableValue >>>= 1) {
                    if ((incer & count) != 0) {
                        put += tableValue;
                    }
                }
                predict_table[destTablePos] = put;
            }
        }
    }
}