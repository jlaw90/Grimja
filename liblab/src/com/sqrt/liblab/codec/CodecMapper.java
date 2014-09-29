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