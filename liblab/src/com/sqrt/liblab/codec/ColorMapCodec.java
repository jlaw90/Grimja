package com.sqrt.liblab.codec;

import com.sqrt.liblab.io.DataSource;
import com.sqrt.liblab.entry.model.ColorMap;

import java.io.IOException;

public class ColorMapCodec extends EntryCodec<ColorMap> {
    public ColorMap _read(DataSource source) throws IOException {
        if(source.readInt() != (('C' << 24) | ('M' << 16) | ('P' << 8) | ' '))
            throw new IOException("Invalid colormap header");
        source.seek(64);
        ColorMap cm = new ColorMap(source.container, source.getName());
        for(int i = 0; i < cm.colors.length; i++)
            cm.colors[i] = (source.readUnsignedByte()) | (source.readUnsignedByte() << 8) | (source.readUnsignedByte() << 16);
        return cm;
    }

    public DataSource write(ColorMap source) throws IOException {
        throw new UnsupportedOperationException(); // Todo
    }

    public String[] getFileExtensions() {
        return new String[]{"cmp"};
    }

    public Class<ColorMap> getEntryClass() {
        return ColorMap.class;
    }
}