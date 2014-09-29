package com.sqrt.liblab.codec;

import com.sqrt.liblab.entry.LabEntry;
import com.sqrt.liblab.io.DataSource;

import java.util.*;

public final class CodecMapper {
    private CodecMapper() {
    }

    private static List<EntryCodec<? extends LabEntry>> codecs = new LinkedList<>();
    private static Map<String, EntryCodec<? extends LabEntry>> extMap = new HashMap<>();
    private static Map<Class<? extends LabEntry>, EntryCodec<? extends LabEntry>> typeMap = new HashMap<>();

    public static <T extends LabEntry> EntryCodec<T> codecForClass(Class<T> clazz) {
        return (EntryCodec<T>) typeMap.get(clazz);
    }

    public static EntryCodec<?> codecForExtension(String ext) {
        if (ext == null)
            return null;
        return extMap.get(ext.toLowerCase());
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
        typeMap.put(codec.getEntryClass(), codec);
        for(String ext: codec.getFileExtensions())
            extMap.put(ext.toLowerCase(), codec);
    }

    public static void unregisterCodec(EntryCodec<?> codec) {
        codecs.remove(codec);
        typeMap.remove(codec.getEntryClass());
        for(String ext: codec.getFileExtensions())
            extMap.put(ext.toLowerCase(), codec);
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