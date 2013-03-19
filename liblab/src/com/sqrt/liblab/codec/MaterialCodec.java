package com.sqrt.liblab.codec;

import com.sqrt.liblab.EntryDataProvider;
import com.sqrt.liblab.model.Material;
import com.sqrt.liblab.model.Texture;

import java.io.IOException;

public class MaterialCodec implements EntryCodec<Material> {
    public Material read(EntryDataProvider source) throws IOException {
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
            int hasAlpha = source.readIntLE();
            byte[] data = new byte[width*height];
            source.skip(12);
            source.readFully(data);
            Texture t = new Texture(width, height, data);
            t.hasAlpha = hasAlpha != 0;
            mat.textures.add(t);
        }
        return mat;
    }

    public EntryDataProvider write(Material source) throws IOException {
        throw new UnsupportedOperationException();
    }

    public String[] getFileExtensions() {
        return new String[]{"mat"};
    }

    public byte[][] getFileHeaders() {
        return new byte[][]{{'M', 'A', 'T', ' '}};
    }

    public Class<Material> getEntryClass() {
        return Material.class;
    }
}