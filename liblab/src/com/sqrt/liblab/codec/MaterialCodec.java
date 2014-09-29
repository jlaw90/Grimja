package com.sqrt.liblab.codec;

import com.sqrt.liblab.io.DataSource;
import com.sqrt.liblab.entry.model.Material;
import com.sqrt.liblab.entry.model.Texture;

import java.io.IOException;

public class MaterialCodec extends EntryCodec<Material> {
    public Material _read(DataSource source) throws IOException {
        if(source.readInt() != (('M' << 24) | ('A' << 16) | ('T' << 8) | ' '))
            throw new IOException("Invalid material header");
        source.seek(12);
        int numImages = source.readIntLE();
        Material mat = new Material(source.container, source.getName());
        source.seek(0x4c);
        int offset = source.readIntLE();
        if(offset == 8)
            offset = 16;
        else if(offset != 0)
            System.err.println("Unknown offset: " + offset);
        source.seek(60 + numImages*40+offset);
        for(int i = 0; i < numImages; i++) {
            int width = source.readIntLE();
            int height = source.readIntLE();
            boolean hasAlpha = source.readBoolean();
            byte[] data = new byte[width*height];
            source.skip(12);
            source.readFully(data);
            Texture t = new Texture(width, height, data);
            t.hasAlpha = hasAlpha;
            mat.textures.add(t);
        }
        return mat;
    }

    public DataSource write(Material source) throws IOException {
        throw new UnsupportedOperationException(); // Todo
    }

    public String[] getFileExtensions() {
        return new String[]{"mat"};
    }

    public Class<Material> getEntryClass() {
        return Material.class;
    }
}