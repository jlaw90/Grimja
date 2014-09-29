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

import com.sqrt.liblab.io.DataSource;
import com.sqrt.liblab.entry.model.Material;
import com.sqrt.liblab.entry.model.Texture;

import java.io.IOException;

public class MaterialCodec extends EntryCodec<Material> {
    public Material _read(DataSource source) throws IOException {
        if(source.getInt() != (('M' << 24) | ('A' << 16) | ('T' << 8) | ' '))
            throw new IOException("Invalid material header");
        source.position(12);
        int numImages = source.getIntLE();
        Material mat = new Material(source.container, source.getName());
        source.position(0x4c);
        int offset = source.getIntLE();
        if(offset == 8)
            offset = 16;
        else if(offset != 0)
            System.err.println("Unknown offset: " + offset);
        source.position(60 + numImages * 40 + offset);
        for(int i = 0; i < numImages; i++) {
            int width = source.getIntLE();
            int height = source.getIntLE();
            boolean hasAlpha = source.getBoolean();
            byte[] data = new byte[width*height];
            source.skip(12);
            source.get(data);
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