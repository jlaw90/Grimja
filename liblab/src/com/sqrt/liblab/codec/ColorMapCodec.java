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
import com.sqrt.liblab.entry.model.ColorMap;

import java.io.*;

public class ColorMapCodec extends EntryCodec<ColorMap> {
    public ColorMap _read(DataSource source) throws IOException {
        if (source.getInt() != (('C' << 24) | ('M' << 16) | ('P' << 8) | ' '))
            throw new IOException("Invalid colormap header");
        source.position(64);
        ColorMap cm = new ColorMap(source.container, source.getName());
        for (int i = 0; i < cm.colors.length; i++)
            cm.colors[i] = (source.getUByte()) | (source.getUByte() << 8) | (source.getUByte() << 16);
        return cm;
    }

    public DataSource write(ColorMap source) throws IOException {
        // Can't write until we know what the first 64 bytes are...
        throw new UnsupportedOperationException(); // Todo
    }

    public void writeACT(ColorMap source, File f) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(f)) {
            // No header or version number, just 768 bytes of color data in RGB
            for (int i = 0; i < 256; i++) {
                int c = source.colors[i];
                fos.write((c >> 16) & 0xff);
                fos.write((c >> 8) & 0xff);
                fos.write(c & 0xff);
            }
        }
    }

    public String[] getFileExtensions() {
        return new String[]{"cmp"};
    }

    public Class<ColorMap> getEntryClass() {
        return ColorMap.class;
    }

    public void readACTFor(ColorMap data, File f) throws IOException {
        try(FileInputStream fin = new FileInputStream(f)) {
            for(int i = 0; i < 256; i++) {
                int r = fin.read();
                int g = fin.read();
                int b = fin.read();
                if((r | g | b) < 0)
                    throw new EOFException();
                data.colors[i] = (r << 16) | (g << 8) | b;
            }
        }
    }
}