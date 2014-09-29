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
import com.sqrt.liblab.entry.graphics.FontGlyph;
import com.sqrt.liblab.entry.graphics.GrimFont;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FontCodec extends EntryCodec<GrimFont> {
    public GrimFont _read(DataSource e) throws IOException {
        int numChars = e.getIntLE();
        long dataSize = e.getUIntLE();
        int height = e.getIntLE();
        int yOffset = e.getIntLE();
        e.position(24);
        int firstChar = e.getIntLE();
        int lastChar = e.getIntLE();
        int availableHeight = height - yOffset;
        int[] charIndices = new int[numChars];
        for (int i = 0; i < numChars; i++)
            charIndices[i] = e.getUShortLE();

        List<FontGlyph> glyphs = new ArrayList<FontGlyph>(numChars);
        long[] offsets = new long[numChars];
        int[] widths = new int[numChars];
        int[] heights = new int[numChars];
        for (int i = 0; i < numChars; i++) {
            FontGlyph g = new FontGlyph();
            g.index = charIndices[i];
            offsets[i] = e.getUIntLE();
            g.charWidth = e.get();
            g.xOff = e.get();
            g.yOff = e.get();
            e.skip(1);
            widths[i] = e.getIntLE();
            heights[i] = e.getIntLE();
            long overflow = (heights[i] + g.yOff) - availableHeight;
            if (overflow > 0) {
                System.err.printf("Font %s, char 0x%02x exceeds font height by %d, increasing font height%n", e.getName(), i, overflow);
                availableHeight += overflow;
                height += overflow;
            }
            glyphs.add(g);
        }
        byte[] fontData = new byte[(int) dataSize];
        e.get(fontData);
        for (int i = 0; i < glyphs.size(); i++) {
            FontGlyph glyph = glyphs.get(i);
            byte[] data = new byte[widths[i] * heights[i]];
            System.arraycopy(fontData, (int) offsets[i], data, 0, data.length);
            BufferedImage bi = new BufferedImage(widths[i], heights[i], BufferedImage.TYPE_INT_ARGB);
            int[] argb = new int[data.length];
            for (int j = 0; j < data.length; j++)
                argb[j] = (data[j] & 0xff) << 24;
            bi.setRGB(0, 0, widths[i], heights[i], argb, 0, widths[i]);
            glyph.mask = bi;
        }
        return new GrimFont(e.container, e.getName(), firstChar, lastChar, yOffset, height, glyphs);
    }

    public DataSource write(GrimFont source) throws IOException {
        throw new UnsupportedOperationException(); // Todo
    }

    public String[] getFileExtensions() {
        return new String[]{"laf"};
    }

    public Class<GrimFont> getEntryClass() {
        return GrimFont.class;
    }
}