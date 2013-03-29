package com.sqrt.liblab.entry.audio;

import com.sqrt.liblab.LabEntry;
import com.sqrt.liblab.LabFile;

import java.util.LinkedList;
import java.util.List;

public class Audio extends LabEntry {
    public int sampleRate;
    public int channels;
    public int bits;
    public final List<Region> regions = new LinkedList<Region>();
    public AudioInputStream stream;

    public Audio(LabFile container, String name) {
        super(container, name);
    }

    public int bytesToTime(long l) {
        return (int) (l / (long) ((sampleRate * channels * (bits / 8)) / 1000));
    }

//    public void export(File f) throws IOException {
//        FileOutputStream fos = new FileOutputStream(f);
//        DataOutputStream dos = new DataOutputStream(fos);
//        dos.writeInt(('R' << 24) | ('I' << 16) | ('F' << 8) | 'F');
//        ByteArrayOutputStream baos = writeWaveData();
//        dos.writeInt(baos.size());
//        baos.writeTo(fos);
//        fos.flush();
//        fos.close();
//    }
//
//    private ByteArrayOutputStream writeWaveData() throws IOException {
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        DataOutputStream dos = new DataOutputStream(baos);
//        dos.writeInt(('W' << 24) | ('A' << 16) | ('V' << 8) | 'E');
//        // fmt chunk
//        dos.writeInt(('f' << 24) | ('m' << 16) | ('t' << 8) | ' ');
//
//        dos.writeInt(reverse32(16));
//        dos.writeShort(reverse16(1));
//        dos.writeShort(reverse16(channels));
//        dos.writeInt(reverse32(sampleRate));
//        dos.writeInt(reverse32(sampleRate * channels * (bits / 8)));
//        dos.writeShort(reverse16(channels * (bits / 8)));
//        dos.writeShort(reverse16(bits));
//
//        // data chunk...
//        dos.writeInt(('d' << 24) | ('a' << 16) | ('t' << 8) | 'a');
//        int size = 0;
//        for (Region r : regions)
//            size += r.length;
//        dos.writeInt(reverse32(size));
//        for (Region r : regions) {
//            // big to little endian...
//            final int bytes = bits / 8;
//            for (int i = 0; i < r.data.length; i += bytes) {
//                for (int j = 0; j < bytes; j++) {
//                    dos.writeByte(r.data[i + ((bytes - 1) - j)]);
//                }
//            }
//        }
//        return baos;
//    }
//
//    private ByteBuffer buf = ByteBuffer.allocate(4);
//
//    private int reverse16(int i) {
//        buf.order(ByteOrder.BIG_ENDIAN);
//        buf.putShort(0, (short) i);
//        buf.order(ByteOrder.LITTLE_ENDIAN);
//        return buf.getShort(0) & 0xffff;
//    }
//
//    private int reverse32(int i) {
//        buf.order(ByteOrder.BIG_ENDIAN);
//        buf.putInt(0, i);
//        buf.order(ByteOrder.LITTLE_ENDIAN);
//        return buf.getInt(0);
//    }
}