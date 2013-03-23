package com.sqrt.liblab.codec;

import com.sqrt.liblab.EntryDataProvider;
import com.sqrt.liblab.LabEntry;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public final class CodecMapper {
    private CodecMapper() {
    }

    private static List<EntryCodec<? extends LabEntry>> codecs = new LinkedList<EntryCodec<? extends LabEntry>>();

    public static <T extends LabEntry> EntryCodec<T> codecForClass(Class<T> clazz) {
        for (EntryCodec<?> ec : codecs) {
            if (ec.getEntryClass() == clazz)
                return (EntryCodec<T>) ec;
        }
        return null;
    }

    public static EntryCodec<?> codecForExtension(String ext) {
        if (ext == null)
            return null;
        for (EntryCodec<?> ec : codecs) {
            if (ec.getFileExtensions() == null)
                continue;
            for (String s : ec.getFileExtensions())
                if (ext.equalsIgnoreCase(s))
                    return ec;
        }
        return null;
    }

    public static EntryCodec<?> codecForMagic(EntryDataProvider provider) {
        try {
            for (EntryCodec<?> ec : codecs) {
                if (ec.getFileHeaders() == null)
                    continue;
                hLoop:
                for (byte[] header : ec.getFileHeaders()) {
                    try {
                        provider.seek(0);
                        if (header == null)
                            continue;

                        if (provider.available() < header.length)
                            continue;
                        byte[] read = new byte[header.length];
                        provider.reset();
                        provider.readFully(read);
                        for (int i = 0; i < header.length; i++)
                            if (read[i] != header[i])
                                continue hLoop;
                        return ec;
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            }
            return null;
        } finally {
            try {
                provider.seek(0);
            } catch (IOException e) {
            /**/
            }
        }
    }

    public static EntryCodec<?> codecForProvider(EntryDataProvider provider) {
        try {
            // First try by extension...
            String name = provider.getName();
            if (name != null) {
                int idx = name.lastIndexOf('.');
                if (idx != -1) {
                    EntryCodec<?> codec = codecForExtension(name.substring(idx + 1));
                    if (codec != null)
                        return codec;
                }
            }
            // Then try by "magic" (e.g. 'LABN' at the start of the file...)
            return codecForMagic(provider);
        } finally {
            try {
                provider.seek(0);
            } catch (IOException e) {
            /**/
            }
        }
    }

    public static void registerCodec(EntryCodec<?> codec) {
        codecs.add(codec);
    }

    public static void unregisterCodec(EntryCodec<?> codec) {
        codecs.remove(codec);
    }

    public static void registerDefaults() {
        CodecMapper.registerCodec(new FontCodec());
        CodecMapper.registerCodec(new BitmapCodec());
        CodecMapper.registerCodec(new ColorMapCodec());
        CodecMapper.registerCodec(new MaterialCodec());
        CodecMapper.registerCodec(new ModelCodec());
        CodecMapper.registerCodec(new KeyFrameCodec());
        // Todo .cos (costume)
        // Todo .lip (lip sync)
        // Todo .snm (movie)
        // Todo .wav (audio)
        // Todo .imu (iMuse)
        // Todo .lua
    }

    static {
        registerDefaults();
    }
}