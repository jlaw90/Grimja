package com.sqrt.liblab.codec;

import com.sqrt.liblab.entry.LabEntry;
import com.sqrt.liblab.io.DataSource;

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

    public static EntryCodec<?> codecForProvider(DataSource provider) {
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
        return null;

    }

    public static void registerCodec(EntryCodec<?> codec) {
        codecs.add(codec);
    }

    public static void unregisterCodec(EntryCodec<?> codec) {
        codecs.remove(codec);
    }

    public static void registerDefaults() {
        if(!codecs.isEmpty())
            return;
        CodecMapper.registerCodec(new FontCodec());
        CodecMapper.registerCodec(new BitmapCodec());
        CodecMapper.registerCodec(new ColorMapCodec());
        CodecMapper.registerCodec(new MaterialCodec());
        CodecMapper.registerCodec(new ModelCodec());
        CodecMapper.registerCodec(new AnimationCodec());
        CodecMapper.registerCodec(new AudioCodec());
        CodecMapper.registerCodec(new SmushCodec());

        // Todo .set
        // Todo .cos (costume)
        // Todo .lip (lip sync)
        // Todo .lua
        // Todo .txt
    }

    static {
        registerDefaults();
    }
}