package com.sqrt.liblab.codec;

import com.sqrt.liblab.EntryDataProvider;
import com.sqrt.liblab.entry.model.ColorMap;

import java.io.IOException;

public class ColorMapCodec extends EntryCodec<ColorMap> {
    public ColorMap _read(EntryDataProvider source) throws IOException {
        if(source.readInt() != (('C' << 24) | ('M' << 16) | ('P' << 8) | ' '))
            throw new IOException("Invalid colormap header");
        source.seek(64);
        ColorMap cm = new ColorMap(source.container, source.getName());
        for(int i = 0; i < cm.colors.length; i++)
            cm.colors[i] = (source.readUByte()) | (source.readUByte() << 8) | (source.readUByte() << 16);
        return cm;
    }

    public EntryDataProvider write(ColorMap source) throws IOException {
        return null;
    }

    public String[] getFileExtensions() {
        return new String[]{"cmp"};
    }

    public byte[][] getFileHeaders() {
        return new byte[][]{{'C', 'M', 'P', ' '}};
    }

    public Class<ColorMap> getEntryClass() {
        return ColorMap.class;
    }
}