package com.sqrt.liblab.codec;

import com.sqrt.liblab.entry.video.Video;
import com.sqrt.liblab.entry.video.VideoInputStream;
import com.sqrt.liblab.io.DataSource;
import com.sqrt.liblab.io.DiskDataSource;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class SmushCodec extends EntryCodec<Video> {
    protected Video _read(DataSource compressed) throws IOException {
        SANMVideoContext ctx = new SANMVideoContext();
        ctx.video = new Video(compressed.container, compressed.getName());
        ctx.video.format = BufferedImage.TYPE_USHORT_565_RGB;
        byte[] buf = new byte[5000];
        File temp = File.createTempFile("video_decomp", ".snm.raw");
        FileOutputStream fos = new FileOutputStream(temp);
        GZIPInputStream gzis = new GZIPInputStream(compressed);
        while (true) {
            int len = gzis.read(buf, 0, buf.length);
            if (len < 0)
                break;
            fos.write(buf, 0, len);
        }
        buf = null;
        fos.close();
        RandomAccessFile raf = new RandomAccessFile(temp, "r");
        DataSource source = new DiskDataSource(compressed.container, compressed.getName(), raf);
        source.seek(0);
        ctx.source = source;
        ctx.video.stream = new SANMVideoStream(ctx);
        if (source.readInt() != (('S' << 24) | ('A' << 16) | ('N' << 8) | 'M')) {
            System.err.println("WHA?");
            return null;
        }

        int size = source.readInt();
        while (source.getPosition() < size)
            processBlock(ctx, source);
        temp.delete();
        ctx.video.fps = 14.99992f;
        return ctx.video;
    }

    private void processBlock(SANMVideoContext ctx, DataSource source) throws IOException {
        int tag = source.readInt();
        int size = source.readInt();
        long end = source.getPosition() + size;
        switch (tag) {
            // Header
            case (('S' << 24) | ('H' << 16) | ('D' << 8) | 'R'):
                ctx.ver = source.readUnsignedShort();
                int numFrames = source.readUnsignedShortLE();
                int vidX = source.readUnsignedShortLE();
                int vidY = source.readUnsignedShortLE();
                ctx.width = source.readUnsignedShortLE();
                ctx.height = source.readUnsignedShortLE();
                ctx.video.width = ctx.width;
                ctx.video.height = ctx.height;
                ctx.aligned_width = align(ctx.width, 8);
                ctx.aligned_height = align(ctx.height, 8);
                ctx.npixels = ctx.width * ctx.height;
                int type = source.readUnsignedShortLE();
                int frameDelay = source.readIntLE(); // microseconds...
                int frame_buf_size = source.readIntLE();
                ctx.buf_size = ctx.aligned_width * ctx.aligned_height;
                ctx.frm0 = new short[ctx.buf_size];
                ctx.frm1 = new short[ctx.buf_size];
                ctx.frm2 = new short[ctx.buf_size];
                ctx.pitch = ctx.width;
                for (int i = 0; i < 256; i++)
                    ctx.palette[i] = source.readIntLE();
                break;
            // Frame header...
            case (('F' << 24) | ('L' << 16) | ('H' << 8) | 'D'):
                while (source.getPosition() < end - 5) // I use 5 because sometimes the Wave metadata has an extra 4 bytes
                    readFrameMetadata(ctx, source);
                break;
            // Frame
            case (('F' << 24) | ('R' << 16) | ('M' << 8) | 'E'):
                while (source.getPosition() < end - 1) {
                    readFrameData(ctx, source);
                }
                break;
            case (('A' << 24) | ('N' << 16) | ('N' << 8) | 'O'):
                System.out.println("ANNO: " + source.readString(size));
                break;
            default:
                System.out.println("Unknown chunk tag " + tagToString(tag));
        }
        source.skip(end - source.getPosition()); // skip any padding...
    }

    private void readFrameMetadata(SANMVideoContext ctx, DataSource source) throws IOException {
        int tag = source.readInt();
        int size = source.readInt();
        long end = source.getPosition() + size;
        switch (tag) {
            case (('B' << 24) | ('l' << 16) | ('1' << 8) | '6'):
                source.skip(10);
                //int width = source.readIntLE();
                //int height = source.readIntLE();
                //ctx.blockDimensions.add(new Dimension(width, height));
                break;
            case (('W' << 24) | ('a' << 16) | ('v' << 8) | 'e'):
                Video vid = ctx.video;
                vid.audio.sampleRate = source.readIntLE();
                vid.audio.channels = source.readIntLE();
                vid.audio.bits = 16;
                vid.audio.stream = new VimaStream(source);
                break;
            default:
                System.out.println("Unknown frame metadata tag: " + tagToString(tag));
        }
        source.skip(end - source.getPosition());
    }

    private void readFrameData(SANMVideoContext ctx, DataSource source) throws IOException {
        int tag = source.readInt();
        int size = source.readInt();
        long end = source.getPosition() + size;
        switch (tag) {
            case (('B' << 24) | ('l' << 16) | ('1' << 8) | ('6')):
                source.skip(8);
                FrameHeader fh = new FrameHeader();
                fh.width = source.readIntLE();
                fh.height = source.readIntLE();
                fh.seq_num = source.readUnsignedShortLE();
                fh.codec = source.readUnsignedByte();
                fh.keyframe = fh.codec == 0 || fh.seq_num == 0; // Keyframe?
                fh.rotate_code = source.readUnsignedByte();
                source.skip(4);
                for (int i = 0; i < 4; i++)
                    ctx.small_codebook[i] = source.readShortLE();
                fh.bg_color = source.readShortLE();
                source.skip(2);
                fh.rle_output_size = source.readIntLE();
                for (int i = 0; i < 256; i++)
                    ctx.codebook[i] = source.readShortLE();
                source.skip(8);
                fh.off = source.getPosition();
                fh.size = (int) (end - fh.off);
                ((SANMVideoStream) ctx.video.stream).frames.add(fh);
                break;
            case (('W' << 24) | ('a' << 16) | ('v' << 8) | 'e'):
                int samples = source.readInt();
                if (samples < 0) {
                    source.skip(4);
                    samples = source.readInt();
                }
                Video vid = ctx.video;
                VimaStream stream = (VimaStream) vid.audio.stream;
                stream.offsets.add((int) source.getPosition());
                stream.lengths.add(samples * vid.audio.channels * 2);
                break;
            default:
                System.out.println("Unknown frame data tag: " + tagToString(tag));
        }
        source.skip(end - source.getPosition());
    }

    public DataSource write(Video source) throws IOException {
        throw new UnsupportedOperationException();
    }

    public String[] getFileExtensions() {
        return new String[]{"snm"};
    }

    public Class<Video> getEntryClass() {
        return Video.class;
    }

    private static int align(int x, int a) {
        return (x + a - 1 & ~(a - 1));
    }
}

class SANMVideoContext {
    DataSource source;
    FrameHeader current_frame;
    Video video;
    int ver;
    int[] palette = new int[256];
    short[] frm0, frm1, frm2, old_frame;
    int pitch, width, height, aligned_width, aligned_height;
    int rle_buf_size, rotate_code, npixels, buf_size;
    short[] codebook = new short[256];
    short[] small_codebook = new short[4];
    boolean[][] p4x4glyphs = new boolean[256][16];
    boolean[][] p8x8glyphs = new boolean[256][64];

    public short readCodebook(int index) {
        short val = codebook[index];
        return val;
    }
}

class FrameHeader {
    long off;
    int size, width, height, seq_num, codec, rotate_code, rle_output_size;
    short bg_color;
    boolean keyframe;
}

class VimaStream extends com.sqrt.liblab.entry.audio.AudioInputStream {
    private final DataSource source;
    final List<Integer> offsets = new LinkedList<Integer>();
    final List<Integer> lengths = new LinkedList<Integer>();
    private int offsetIdx = 0, bufOff;
    private byte[] buf;

    public VimaStream(DataSource source) {
        this.source = source;
    }

    public synchronized void reset() {
        offsetIdx = 0;
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
        if (offsetIdx >= offsets.size())
            return null;
        int offset = offsets.get(offsetIdx);
        int length = lengths.get(offsetIdx++);
        source.seek(offset);
        return Vima.decompress(source, length).toByteArray();
    }

    public void seek(int pos) throws IOException {
        int count = 0;
        for (int i = 0; i < lengths.size(); i++) {
            int length = lengths.get(i);
            if (pos >= count && pos < count + length) {
                if (offsetIdx != i)
                    buf = null; // Need a new buffer...
                offsetIdx = i;
                bufOff = pos - count;
                break;
            }
            count += length;
        }
    }
}

class SANMVideoStream extends VideoInputStream {
    //region Large arrays...
    private static final byte[] glyph4_x = {0, 1, 2, 3, 3, 3, 3, 2, 1, 0, 0, 0, 1, 2, 2, 1};
    private static final byte[] glyph4_y = {0, 0, 0, 0, 1, 2, 3, 3, 3, 3, 2, 1, 1, 1, 2, 2};
    private static final byte[] glyph8_x = {0, 2, 5, 7, 7, 7, 7, 7, 7, 5, 2, 0, 0, 0, 0, 0};
    private static final byte[] glyph8_y = {0, 0, 0, 0, 1, 3, 4, 6, 7, 7, 7, 7, 6, 4, 3, 1};
    private static final byte[][] motion_vectors = {
            {0, 0}, {-1, -43}, {6, -43}, {-9, -42}, {13, -41},
            {-16, -40}, {19, -39}, {-23, -36}, {26, -34}, {-2, -33},
            {4, -33}, {-29, -32}, {-9, -32}, {11, -31}, {-16, -29},
            {32, -29}, {18, -28}, {-34, -26}, {-22, -25}, {-1, -25},
            {3, -25}, {-7, -24}, {8, -24}, {24, -23}, {36, -23},
            {-12, -22}, {13, -21}, {-38, -20}, {0, -20}, {-27, -19},
            {-4, -19}, {4, -19}, {-17, -18}, {-8, -17}, {8, -17},
            {18, -17}, {28, -17}, {39, -17}, {-12, -15}, {12, -15},
            {-21, -14}, {-1, -14}, {1, -14}, {-41, -13}, {-5, -13},
            {5, -13}, {21, -13}, {-31, -12}, {-15, -11}, {-8, -11},
            {8, -11}, {15, -11}, {-2, -10}, {1, -10}, {31, -10},
            {-23, -9}, {-11, -9}, {-5, -9}, {4, -9}, {11, -9},
            {42, -9}, {6, -8}, {24, -8}, {-18, -7}, {-7, -7},
            {-3, -7}, {-1, -7}, {2, -7}, {18, -7}, {-43, -6},
            {-13, -6}, {-4, -6}, {4, -6}, {8, -6}, {-33, -5},
            {-9, -5}, {-2, -5}, {0, -5}, {2, -5}, {5, -5},
            {13, -5}, {-25, -4}, {-6, -4}, {-3, -4}, {3, -4},
            {9, -4}, {-19, -3}, {-7, -3}, {-4, -3}, {-2, -3},
            {-1, -3}, {0, -3}, {1, -3}, {2, -3}, {4, -3},
            {6, -3}, {33, -3}, {-14, -2}, {-10, -2}, {-5, -2},
            {-3, -2}, {-2, -2}, {-1, -2}, {0, -2}, {1, -2},
            {2, -2}, {3, -2}, {5, -2}, {7, -2}, {14, -2},
            {19, -2}, {25, -2}, {43, -2}, {-7, -1}, {-3, -1},
            {-2, -1}, {-1, -1}, {0, -1}, {1, -1}, {2, -1},
            {3, -1}, {10, -1}, {-5, 0}, {-3, 0}, {-2, 0},
            {-1, 0}, {1, 0}, {2, 0}, {3, 0}, {5, 0},
            {7, 0}, {-10, 1}, {-7, 1}, {-3, 1}, {-2, 1},
            {-1, 1}, {0, 1}, {1, 1}, {2, 1}, {3, 1},
            {-43, 2}, {-25, 2}, {-19, 2}, {-14, 2}, {-5, 2},
            {-3, 2}, {-2, 2}, {-1, 2}, {0, 2}, {1, 2},
            {2, 2}, {3, 2}, {5, 2}, {7, 2}, {10, 2},
            {14, 2}, {-33, 3}, {-6, 3}, {-4, 3}, {-2, 3},
            {-1, 3}, {0, 3}, {1, 3}, {2, 3}, {4, 3},
            {19, 3}, {-9, 4}, {-3, 4}, {3, 4}, {7, 4},
            {25, 4}, {-13, 5}, {-5, 5}, {-2, 5}, {0, 5},
            {2, 5}, {5, 5}, {9, 5}, {33, 5}, {-8, 6},
            {-4, 6}, {4, 6}, {13, 6}, {43, 6}, {-18, 7},
            {-2, 7}, {0, 7}, {2, 7}, {7, 7}, {18, 7},
            {-24, 8}, {-6, 8}, {-42, 9}, {-11, 9}, {-4, 9},
            {5, 9}, {11, 9}, {23, 9}, {-31, 10}, {-1, 10},
            {2, 10}, {-15, 11}, {-8, 11}, {8, 11}, {15, 11},
            {31, 12}, {-21, 13}, {-5, 13}, {5, 13}, {41, 13},
            {-1, 14}, {1, 14}, {21, 14}, {-12, 15}, {12, 15},
            {-39, 17}, {-28, 17}, {-18, 17}, {-8, 17}, {8, 17},
            {17, 18}, {-4, 19}, {0, 19}, {4, 19}, {27, 19},
            {38, 20}, {-13, 21}, {12, 22}, {-36, 23}, {-24, 23},
            {-8, 24}, {7, 24}, {-3, 25}, {1, 25}, {22, 25},
            {34, 26}, {-18, 28}, {-32, 29}, {16, 29}, {-11, 31},
            {9, 32}, {29, 32}, {-4, 33}, {2, 33}, {-26, 34},
            {23, 36}, {-19, 39}, {16, 40}, {-13, 41}, {9, 42},
            {-6, 43}, {1, 43}, {0, 0}, {0, 0}, {0, 0},
    };
    private static final byte[] c37_mv = {
            0, 0, 1, 0, 2, 0, 3, 0, 5, 0,
            8, 0, 13, 0, 21, 0, -1, 0, -2, 0,
            -3, 0, -5, 0, -8, 0, -13, 0, -17, 0,
            -21, 0, 0, 1, 1, 1, 2, 1, 3, 1,
            5, 1, 8, 1, 13, 1, 21, 1, -1, 1,
            -2, 1, -3, 1, -5, 1, -8, 1, -13, 1,
            -17, 1, -21, 1, 0, 2, 1, 2, 2, 2,
            3, 2, 5, 2, 8, 2, 13, 2, 21, 2,
            -1, 2, -2, 2, -3, 2, -5, 2, -8, 2,
            -13, 2, -17, 2, -21, 2, 0, 3, 1, 3,
            2, 3, 3, 3, 5, 3, 8, 3, 13, 3,
            21, 3, -1, 3, -2, 3, -3, 3, -5, 3,
            -8, 3, -13, 3, -17, 3, -21, 3, 0, 5,
            1, 5, 2, 5, 3, 5, 5, 5, 8, 5,
            13, 5, 21, 5, -1, 5, -2, 5, -3, 5,
            -5, 5, -8, 5, -13, 5, -17, 5, -21, 5,
            0, 8, 1, 8, 2, 8, 3, 8, 5, 8,
            8, 8, 13, 8, 21, 8, -1, 8, -2, 8,
            -3, 8, -5, 8, -8, 8, -13, 8, -17, 8,
            -21, 8, 0, 13, 1, 13, 2, 13, 3, 13,
            5, 13, 8, 13, 13, 13, 21, 13, -1, 13,
            -2, 13, -3, 13, -5, 13, -8, 13, -13, 13,
            -17, 13, -21, 13, 0, 21, 1, 21, 2, 21,
            3, 21, 5, 21, 8, 21, 13, 21, 21, 21,
            -1, 21, -2, 21, -3, 21, -5, 21, -8, 21,
            -13, 21, -17, 21, -21, 21, 0, -1, 1, -1,
            2, -1, 3, -1, 5, -1, 8, -1, 13, -1,
            21, -1, -1, -1, -2, -1, -3, -1, -5, -1,
            -8, -1, -13, -1, -17, -1, -21, -1, 0, -2,
            1, -2, 2, -2, 3, -2, 5, -2, 8, -2,
            13, -2, 21, -2, -1, -2, -2, -2, -3, -2,
            -5, -2, -8, -2, -13, -2, -17, -2, -21, -2,
            0, -3, 1, -3, 2, -3, 3, -3, 5, -3,
            8, -3, 13, -3, 21, -3, -1, -3, -2, -3,
            -3, -3, -5, -3, -8, -3, -13, -3, -17, -3,
            -21, -3, 0, -5, 1, -5, 2, -5, 3, -5,
            5, -5, 8, -5, 13, -5, 21, -5, -1, -5,
            -2, -5, -3, -5, -5, -5, -8, -5, -13, -5,
            -17, -5, -21, -5, 0, -8, 1, -8, 2, -8,
            3, -8, 5, -8, 8, -8, 13, -8, 21, -8,
            -1, -8, -2, -8, -3, -8, -5, -8, -8, -8,
            -13, -8, -17, -8, -21, -8, 0, -13, 1, -13,
            2, -13, 3, -13, 5, -13, 8, -13, 13, -13,
            21, -13, -1, -13, -2, -13, -3, -13, -5, -13,
            -8, -13, -13, -13, -17, -13, -21, -13, 0, -17,
            1, -17, 2, -17, 3, -17, 5, -17, 8, -17,
            13, -17, 21, -17, -1, -17, -2, -17, -3, -17,
            -5, -17, -8, -17, -13, -17, -17, -17, -21, -17,
            0, -21, 1, -21, 2, -21, 3, -21, 5, -21,
            8, -21, 13, -21, 21, -21, -1, -21, -2, -21,
            -3, -21, -5, -21, -8, -21, -13, -21, -17, -21,
            0, 0, -8, -29, 8, -29, -18, -25, 17, -25,
            0, -23, -6, -22, 6, -22, -13, -19, 12, -19,
            0, -18, 25, -18, -25, -17, -5, -17, 5, -17,
            -10, -15, 10, -15, 0, -14, -4, -13, 4, -13,
            19, -13, -19, -12, -8, -11, -2, -11, 0, -11,
            2, -11, 8, -11, -15, -10, -4, -10, 4, -10,
            15, -10, -6, -9, -1, -9, 1, -9, 6, -9,
            -29, -8, -11, -8, -8, -8, -3, -8, 3, -8,
            8, -8, 11, -8, 29, -8, -5, -7, -2, -7,
            0, -7, 2, -7, 5, -7, -22, -6, -9, -6,
            -6, -6, -3, -6, -1, -6, 1, -6, 3, -6,
            6, -6, 9, -6, 22, -6, -17, -5, -7, -5,
            -4, -5, -2, -5, 0, -5, 2, -5, 4, -5,
            7, -5, 17, -5, -13, -4, -10, -4, -5, -4,
            -3, -4, -1, -4, 0, -4, 1, -4, 3, -4,
            5, -4, 10, -4, 13, -4, -8, -3, -6, -3,
            -4, -3, -3, -3, -2, -3, -1, -3, 0, -3,
            1, -3, 2, -3, 4, -3, 6, -3, 8, -3,
            -11, -2, -7, -2, -5, -2, -3, -2, -2, -2,
            -1, -2, 0, -2, 1, -2, 2, -2, 3, -2,
            5, -2, 7, -2, 11, -2, -9, -1, -6, -1,
            -4, -1, -3, -1, -2, -1, -1, -1, 0, -1,
            1, -1, 2, -1, 3, -1, 4, -1, 6, -1,
            9, -1, -31, 0, -23, 0, -18, 0, -14, 0,
            -11, 0, -7, 0, -5, 0, -4, 0, -3, 0,
            -2, 0, -1, 0, 0, -31, 1, 0, 2, 0,
            3, 0, 4, 0, 5, 0, 7, 0, 11, 0,
            14, 0, 18, 0, 23, 0, 31, 0, -9, 1,
            -6, 1, -4, 1, -3, 1, -2, 1, -1, 1,
            0, 1, 1, 1, 2, 1, 3, 1, 4, 1,
            6, 1, 9, 1, -11, 2, -7, 2, -5, 2,
            -3, 2, -2, 2, -1, 2, 0, 2, 1, 2,
            2, 2, 3, 2, 5, 2, 7, 2, 11, 2,
            -8, 3, -6, 3, -4, 3, -2, 3, -1, 3,
            0, 3, 1, 3, 2, 3, 3, 3, 4, 3,
            6, 3, 8, 3, -13, 4, -10, 4, -5, 4,
            -3, 4, -1, 4, 0, 4, 1, 4, 3, 4,
            5, 4, 10, 4, 13, 4, -17, 5, -7, 5,
            -4, 5, -2, 5, 0, 5, 2, 5, 4, 5,
            7, 5, 17, 5, -22, 6, -9, 6, -6, 6,
            -3, 6, -1, 6, 1, 6, 3, 6, 6, 6,
            9, 6, 22, 6, -5, 7, -2, 7, 0, 7,
            2, 7, 5, 7, -29, 8, -11, 8, -8, 8,
            -3, 8, 3, 8, 8, 8, 11, 8, 29, 8,
            -6, 9, -1, 9, 1, 9, 6, 9, -15, 10,
            -4, 10, 4, 10, 15, 10, -8, 11, -2, 11,
            0, 11, 2, 11, 8, 11, 19, 12, -19, 13,
            -4, 13, 4, 13, 0, 14, -10, 15, 10, 15,
            -5, 17, 5, 17, 25, 17, -25, 18, 0, 18,
            -12, 19, 13, 19, -6, 22, 6, 22, 0, 23,
            -17, 25, 18, 25, -8, 29, 8, 29, 0, 31,
            0, 0, -6, -22, 6, -22, -13, -19, 12, -19,
            0, -18, -5, -17, 5, -17, -10, -15, 10, -15,
            0, -14, -4, -13, 4, -13, 19, -13, -19, -12,
            -8, -11, -2, -11, 0, -11, 2, -11, 8, -11,
            -15, -10, -4, -10, 4, -10, 15, -10, -6, -9,
            -1, -9, 1, -9, 6, -9, -11, -8, -8, -8,
            -3, -8, 0, -8, 3, -8, 8, -8, 11, -8,
            -5, -7, -2, -7, 0, -7, 2, -7, 5, -7,
            -22, -6, -9, -6, -6, -6, -3, -6, -1, -6,
            1, -6, 3, -6, 6, -6, 9, -6, 22, -6,
            -17, -5, -7, -5, -4, -5, -2, -5, -1, -5,
            0, -5, 1, -5, 2, -5, 4, -5, 7, -5,
            17, -5, -13, -4, -10, -4, -5, -4, -3, -4,
            -2, -4, -1, -4, 0, -4, 1, -4, 2, -4,
            3, -4, 5, -4, 10, -4, 13, -4, -8, -3,
            -6, -3, -4, -3, -3, -3, -2, -3, -1, -3,
            0, -3, 1, -3, 2, -3, 3, -3, 4, -3,
            6, -3, 8, -3, -11, -2, -7, -2, -5, -2,
            -4, -2, -3, -2, -2, -2, -1, -2, 0, -2,
            1, -2, 2, -2, 3, -2, 4, -2, 5, -2,
            7, -2, 11, -2, -9, -1, -6, -1, -5, -1,
            -4, -1, -3, -1, -2, -1, -1, -1, 0, -1,
            1, -1, 2, -1, 3, -1, 4, -1, 5, -1,
            6, -1, 9, -1, -23, 0, -18, 0, -14, 0,
            -11, 0, -7, 0, -5, 0, -4, 0, -3, 0,
            -2, 0, -1, 0, 0, -23, 1, 0, 2, 0,
            3, 0, 4, 0, 5, 0, 7, 0, 11, 0,
            14, 0, 18, 0, 23, 0, -9, 1, -6, 1,
            -5, 1, -4, 1, -3, 1, -2, 1, -1, 1,
            0, 1, 1, 1, 2, 1, 3, 1, 4, 1,
            5, 1, 6, 1, 9, 1, -11, 2, -7, 2,
            -5, 2, -4, 2, -3, 2, -2, 2, -1, 2,
            0, 2, 1, 2, 2, 2, 3, 2, 4, 2,
            5, 2, 7, 2, 11, 2, -8, 3, -6, 3,
            -4, 3, -3, 3, -2, 3, -1, 3, 0, 3,
            1, 3, 2, 3, 3, 3, 4, 3, 6, 3,
            8, 3, -13, 4, -10, 4, -5, 4, -3, 4,
            -2, 4, -1, 4, 0, 4, 1, 4, 2, 4,
            3, 4, 5, 4, 10, 4, 13, 4, -17, 5,
            -7, 5, -4, 5, -2, 5, -1, 5, 0, 5,
            1, 5, 2, 5, 4, 5, 7, 5, 17, 5,
            -22, 6, -9, 6, -6, 6, -3, 6, -1, 6,
            1, 6, 3, 6, 6, 6, 9, 6, 22, 6,
            -5, 7, -2, 7, 0, 7, 2, 7, 5, 7,
            -11, 8, -8, 8, -3, 8, 0, 8, 3, 8,
            8, 8, 11, 8, -6, 9, -1, 9, 1, 9,
            6, 9, -15, 10, -4, 10, 4, 10, 15, 10,
            -8, 11, -2, 11, 0, 11, 2, 11, 8, 11,
            19, 12, -19, 13, -4, 13, 4, 13, 0, 14,
            -10, 15, 10, 15, -5, 17, 5, 17, 0, 18,
            -12, 19, 13, 19, -6, 22, 6, 22, 0, 23,
    };
    //endregion

    private static enum GlyphEdge {
        LEFT_EDGE,
        TOP_EDGE,
        RIGHT_EDGE,
        BOTTOM_EDGE,
        NO_EDGE;

        public static GlyphEdge which(int x, int y, int edge_size) {
            final int edge_max = edge_size - 1;
            return y == 0 ? BOTTOM_EDGE : y == edge_max ? TOP_EDGE : x == 0 ? LEFT_EDGE : x == edge_max ? RIGHT_EDGE
                    : NO_EDGE;
        }
    }

    private static enum GlyphDir {
        DIR_LEFT,
        DIR_UP,
        DIR_RIGHT,
        DIR_DOWN,
        NO_DIR;

        public static GlyphDir which(GlyphEdge edge0, GlyphEdge edge1) {
            if ((edge0 == GlyphEdge.LEFT_EDGE && edge1 == GlyphEdge.RIGHT_EDGE) ||
                    (edge1 == GlyphEdge.LEFT_EDGE && edge0 == GlyphEdge.RIGHT_EDGE) ||
                    (edge0 == GlyphEdge.BOTTOM_EDGE && edge1 != GlyphEdge.TOP_EDGE) ||
                    (edge1 == GlyphEdge.BOTTOM_EDGE && edge0 != GlyphEdge.TOP_EDGE)) {
                return DIR_UP;
            } else if ((edge0 == GlyphEdge.TOP_EDGE && edge1 != GlyphEdge.BOTTOM_EDGE) ||
                    (edge1 == GlyphEdge.TOP_EDGE && edge0 != GlyphEdge.BOTTOM_EDGE)) {
                return DIR_DOWN;
            } else if ((edge0 == GlyphEdge.LEFT_EDGE && edge1 != GlyphEdge.RIGHT_EDGE) ||
                    (edge1 == GlyphEdge.LEFT_EDGE && edge0 != GlyphEdge.RIGHT_EDGE)) {
                return DIR_LEFT;
            } else if ((edge0 == GlyphEdge.TOP_EDGE && edge1 == GlyphEdge.BOTTOM_EDGE) ||
                    (edge1 == GlyphEdge.TOP_EDGE && edge0 == GlyphEdge.BOTTOM_EDGE) ||
                    (edge0 == GlyphEdge.RIGHT_EDGE && edge1 != GlyphEdge.LEFT_EDGE) ||
                    (edge1 == GlyphEdge.RIGHT_EDGE && edge0 != GlyphEdge.LEFT_EDGE)) {
                return DIR_RIGHT;
            }

            return NO_DIR;
        }
    }

    public final List<FrameHeader> frames = new LinkedList<FrameHeader>();
    private final SANMVideoContext ctx;
    private int frame_idx, last_frame = -1;

    public SANMVideoStream(SANMVideoContext ctx) {
        this.ctx = ctx;
        make_glyphs(ctx.p4x4glyphs, glyph4_x, glyph4_y, 4);
        make_glyphs(ctx.p8x8glyphs, glyph8_x, glyph8_y, 8);
    }

    public void seek(int pos) throws IOException {
        // Find the frame...
        frame_idx = -1;
        for (FrameHeader header : frames) {
            if (header.off == pos) {
                frame_idx = pos;
                break;
            }
        }
        if (frame_idx == -1)
            throw new UnsupportedOperationException("Could not find frame at specified position");
    }

    public void setFrame(int frame) throws IOException {
        frame_idx = frame; // Todo: locate nearest keyframe, interpolate from there
        if (frame_idx >= frames.size())
            frame_idx = frames.size() - 1;
    }

    private void copyToOutput(WritableRaster raster, int width, int height, short[] source) {
        raster.setDataElements(0, 0, width, height, source);
    }

    public boolean readFrame(WritableRaster raster, int width, int height) throws IOException {
        if (last_frame == frame_idx) {
            copyToOutput(raster, ctx.width, ctx.height, ctx.old_frame);
            return true;
        }
        if (frame_idx >= frames.size())
            return false;
        last_frame = frame_idx;
        FrameHeader header = frames.get(frame_idx++);
        ctx.rotate_code = header.rotate_code;
        if (header.seq_num == 0) { // First frame...
            Arrays.fill(ctx.frm1, 0, ctx.npixels, header.bg_color);
            Arrays.fill(ctx.frm2, 0, ctx.npixels, header.bg_color);
        }
        ctx.current_frame = header;

        DataSource source = ctx.source;
        boolean rootedIo = source instanceof DiskDataSource;
        if(rootedIo)
            ctx.source = ((DiskDataSource) source).subsection((int) header.off, header.size);
        else
            source.seek(header.off);

        long start = ctx.source.getPosition();
        if (header.codec < v1_decoders.length) {
            v1_decoders[header.codec].decode(ctx);
        } else {
            System.err.println("Unhandled subcodec: " + header.codec);
            return false;
        }

        if(rootedIo)
            ctx.source = source;

        // Copy to output buffer...
        copyToOutput(raster, ctx.width, ctx.height, ctx.frm0);
        ctx.old_frame = ctx.frm0;

        // Rotate buffers...
        if (ctx.rotate_code != 0)
            rotate_bufs(ctx, ctx.rotate_code);

        return true;
    }

    private static final Subcodec[] v1_decoders = {new Subcodec0(), new UnknownCodec(1), new Subcodec2(),
            new Subcodec3(), new Subcodec4(), new Subcodec5()};

    private static void rotate_bufs(SANMVideoContext ctx, int code) {
        short[] temp;
        if (code == 2) {
            temp = ctx.frm1;
            ctx.frm1 = ctx.frm2;
            ctx.frm2 = temp;
        }
        temp = ctx.frm2;
        ctx.frm2 = ctx.frm0;
        ctx.frm0 = temp;
    }

    private static void interp_point(Point point, int x0, int y0, int x1, int y1, int pos, int npoints) {
        if (npoints == 0) {
            point.x = x0;
            point.y = y0;
        } else {
            final int remaining = npoints - pos;
            final int halfTotal = npoints >> 2;
            point.x = ((x0 * pos + x1 * remaining + halfTotal) / npoints);
            point.y = ((y0 * pos + y1 * remaining + halfTotal) / npoints);
        }
    }

    private static void make_glyphs(boolean[][] pglyphs, final byte[] xvec, final byte[] yvec, final int side_length) {
        int pglyph_off = 0;
        boolean[] pglyph;
        Point point = new Point();

        int i, j;
        for (i = 0; i < 16; i++) {
            int x0 = xvec[i];
            int y0 = yvec[i];
            GlyphEdge edge0 = GlyphEdge.which(x0, y0, side_length);

            for (j = 0, pglyph = pglyphs[pglyph_off++]; j < 16; j++) {
                int x1 = xvec[j];
                int y1 = yvec[j];
                GlyphEdge edge1 = GlyphEdge.which(x1, y1, side_length);
                GlyphDir dir = GlyphDir.which(edge0, edge1);
                int npoints = Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0));
                int ipoint;

                for (ipoint = 0; ipoint <= npoints; ipoint++) {
                    int irow, icol;

                    interp_point(point, x0, y0, x1, y1, ipoint, npoints);

                    switch (dir) {
                        case DIR_UP:
                            for (irow = point.y; irow >= 0; irow--)
                                pglyph[point.x + irow * side_length] = true;
                            break;
                        case DIR_DOWN:
                            for (irow = point.y; irow < side_length; irow++)
                                pglyph[point.x + irow * side_length] = true;
                            break;
                        case DIR_LEFT:
                            for (icol = point.x; icol >= 0; icol--)
                                pglyph[icol + point.y * side_length] = true;
                            break;

                        case DIR_RIGHT:
                            for (icol = point.x; icol < side_length; icol++)
                                pglyph[icol + point.y * side_length] = true;
                            break;
                    }
                }
            }
        }
    }

    private static void copy_block(short[] dst, int dstOff, short[] src, int srcOff, final int block_size, final int pitch) {
        for(int i = 0; i < block_size; i++) {
            System.arraycopy(src, srcOff, dst, dstOff, block_size);
            dstOff += pitch;
            srcOff += pitch;
        }
    }

    private static void fill_block(short[] dst, int dstOff, short color, int block_size, int pitch) {
        for (int y = 0; y < block_size; y++, dstOff += pitch)
            Arrays.fill(dst, dstOff, dstOff + block_size, color);
    }

    static void draw_glyph(SANMVideoContext ctx, short[] dst, int dstOff, int index, short fg_color,
                           short bg_color, int block_size, int pitch) {
        boolean [] pglyph;
        int x, y;

        if (index >= 256) {
            System.err.println("Ignoring nonexistant glyph: " + index);
            return;
        }

        pglyph = block_size == 8 ? ctx.p8x8glyphs[index] : ctx.p4x4glyphs[index];
        int glyphOff = 0;
        pitch -= block_size;

        for (y = 0; y < block_size; y++, dstOff += pitch)
            for (x = 0; x < block_size; x++)
                dst[dstOff++] = pglyph[glyphOff++] ? fg_color : bg_color;
    }

    private static interface Subcodec {
        void decode(SANMVideoContext ctx) throws IOException;
    }

    private static class UnknownCodec implements Subcodec {
        private int id;

        public UnknownCodec(int id) {
            this.id = id;
        }

        public void decode(SANMVideoContext ctx) throws IOException {
            System.err.println("Unknown codec: " + id);
        }
    }

    private static class Subcodec0 implements Subcodec {
        public void decode(SANMVideoContext ctx) throws IOException {
            for (int i = 0; i < ctx.npixels; i++)
                ctx.frm0[i] = ctx.source.readShortLE();
        }
    }

    private static class Subcodec2 implements Subcodec {
        public void decode(SANMVideoContext ctx) throws IOException {
            for (int cy = 0; cy < ctx.aligned_height; cy += 8) {
                for (int cx = 0; cx < ctx.aligned_width; cx += 8) {
                    codec2subblock(ctx, cx, cy, 8);
                }
            }
        }

        private static void codec2subblock(SANMVideoContext ctx, int cx, int cy, int blk_size) throws IOException {
            int mx, my, index, opcode;

            DataSource source = ctx.source;
            opcode = source.readUnsignedByte();
            switch (opcode) {
                default:
                    mx = motion_vectors[opcode][0];
                    my = motion_vectors[opcode][1];

                    if (good_mvec(ctx, cx, cy, mx, my, blk_size))
                        copy_block(ctx.frm0, cx + ctx.pitch * cy, ctx.frm2, cx + mx + ctx.pitch * (cy + my), blk_size, ctx.pitch);
                    break;
                case 0xF5:
                    index = source.readShortLE();
                    mx = index % ctx.width;
                    my = index / ctx.width;
                    if (good_mvec(ctx, cx, cy, mx, my, blk_size))
                        copy_block(ctx.frm0, cx + ctx.pitch * cy, ctx.frm2, cx + mx + ctx.pitch * (cy + my), blk_size, ctx.pitch);
                    break;
                case 0xF6:
                    copy_block(ctx.frm0, cx + ctx.pitch * cy, ctx.frm1, cx + ctx.pitch * cy, blk_size, ctx.pitch);
                    break;

                // Coplicated
                case 0xF7:
                    opcode_0xf7(ctx, cx, cy, blk_size, ctx.pitch);
                    break;

                // Complicated
                case 0xF8:
                    opcode_0xf8(ctx, cx, cy, blk_size, ctx.pitch);
                    break;

                // Fill from small codebook
                case 0xF9:
                case 0xFA:
                case 0xFB:
                case 0xFC:
                    fill_block(ctx.frm0, cx + cy * ctx.pitch, ctx.small_codebook[opcode - 0xf9], blk_size, ctx.pitch);
                    break;

                // Fill from codebook
                case 0xFD:
                    fill_block(ctx.frm0, cx + cy * ctx.pitch, ctx.readCodebook(source.readUnsignedByte()), blk_size, ctx.pitch);
                    break;

                // Fill with specified color
                case 0xFE:
                    fill_block(ctx.frm0, cx + cy * ctx.pitch, source.readShortLE(), blk_size, ctx.pitch);
                    break;

                // Complicated...
                case 0xFF:
                    if (blk_size == 2) {
                        opcode_0xf8(ctx, cx, cy, blk_size, ctx.pitch);
                    } else {
                        blk_size /= 2;
                        codec2subblock(ctx, cx, cy, blk_size);
                        codec2subblock(ctx, cx + blk_size, cy, blk_size);
                        codec2subblock(ctx, cx, cy + blk_size, blk_size);
                        codec2subblock(ctx, cx + blk_size, cy + blk_size, blk_size);
                    }
                    break;
            }
        }

        private static void opcode_0xf7(SANMVideoContext ctx, int cx, int cy, int block_size, int pitch) throws IOException {
            DataSource source = ctx.source;
            short[] dst = ctx.frm0;
            int dstOff = cx + cy * ctx.pitch;

            if (block_size == 2) {
                int indices;
                indices = source.readIntLE();
                dst[dstOff + 0] = ctx.readCodebook(indices & 0xFF);
                indices >>>= 8;
                dst[dstOff + 1] = ctx.readCodebook(indices & 0xFF);
                indices >>>= 8;
                dst[dstOff + pitch] = ctx.readCodebook(indices & 0xFF);
                indices >>>= 8;
                dst[dstOff + pitch + 1] = ctx.readCodebook(indices & 0xFF);
            } else {
                short fgcolor, bgcolor;
                int glyph;

                glyph = source.readUnsignedByte();
                bgcolor = ctx.readCodebook(source.readUnsignedByte());
                fgcolor = ctx.readCodebook(source.readUnsignedByte());

                draw_glyph(ctx, dst, dstOff, glyph, fgcolor, bgcolor, block_size, pitch);
            }
        }

        private static void opcode_0xf8(SANMVideoContext ctx, int cx, int cy, int block_size, int pitch) throws IOException {
            DataSource source = ctx.source;
            short[] dst = ctx.frm0;
            int dstOff = cx + cy * ctx.pitch;

            if (block_size == 2) {
                dst[dstOff] = source.readShortLE();
                dst[dstOff + 1] = source.readShortLE();
                dst[dstOff + pitch] = source.readShortLE();
                dst[dstOff + pitch + 1] = source.readShortLE();
            } else {
                short fgcolor, bgcolor;
                int glyph;

                glyph = source.readUnsignedByte();
                bgcolor = source.readShortLE();
                fgcolor = source.readShortLE();

                draw_glyph(ctx, dst, dstOff, glyph, fgcolor, bgcolor, block_size, pitch);
            }
        }

        static boolean good_mvec(SANMVideoContext ctx, int cx, int cy, int mx, int my,
                                 int block_size) {
            int start_pos = cx + mx + (cy + my) * ctx.pitch;
            int end_pos = start_pos + (block_size - 1) * (ctx.pitch + 1);
            boolean good = start_pos >= 0 && end_pos < ctx.buf_size;

            if (!good)
                System.err.println(String.format("ignoring invalid motion vector (%d, %d)->(%d, %d), block size = %d\n",
                        cx + mx, cy + my, cx, cy, block_size));
            return good;
        }
    }

    private static class Subcodec3 implements Subcodec {
        public void decode(SANMVideoContext ctx) throws IOException {
            System.arraycopy(ctx.frm2, 0, ctx.frm0, 0, ctx.npixels);
        }
    }

    private static class Subcodec4 implements Subcodec {
        public void decode(SANMVideoContext ctx) throws IOException {
            System.arraycopy(ctx.frm1, 0, ctx.frm0, 0, ctx.npixels);
        }
    }

    private static class Subcodec5 implements Subcodec {
        private byte[] buf;

        public void decode(SANMVideoContext ctx) throws IOException {
            DataSource source = ctx.source;
            int remaining = ctx.current_frame.rle_output_size;
            if(buf == null || buf.length < remaining)
                buf = new byte[remaining];
            int dstOff = 0;
            while(remaining != 0) {
                int code = source.readUnsignedByte();
                int line_length = (code >> 1) + 1;

                if((code & 1) != 0) { // RLE run
                    Arrays.fill(buf, dstOff, dstOff+line_length, source.readByte());
                } else
                    source.readFully(buf, dstOff, line_length);
                dstOff += line_length;
                remaining -= line_length;
            }
            for(int i = 0; i < ctx.npixels*2; i+=2) {
                ctx.frm0[i/2] = (short) (((buf[i+1] & 0xff) << 8) | (buf[i] & 0xff));
            }
        }
    }
}