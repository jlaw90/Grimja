/*
 * Copyright (C) 2014  James Lawrence.
 *
 *     This file is part of LibLab.
 *
 *     LibLab is free software: you can redistribute it and/or modify
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

package com.sqrt.liblab.entry.graphics;

import com.sqrt.liblab.LabFile;
import com.sqrt.liblab.entry.LabEntry;

import java.util.LinkedList;
import java.util.List;

/**
 * A font
 */
public class GrimFont extends LabEntry {
    /**
     * The height of a line in this font
     */
    public int height;
    /**
     * The baseline for this font
     */
    public int yOffset;
    /**
     * The first character contained in this font
     */
    public int firstChar;
    /**
     * The last character contained in this font
     */
    public int lastChar;
    // Todo: firstChar and lastChar needed?
    /**
     * The glyphs of this font
     */
    public List<FontGlyph> glyphs;

    /**
     * Constructs a new font
     * @param container the container of this font
     * @param name the name of this font
     * @param firstChar the first character contained within this font file
     * @param lastChar the last character contained within this font file
     * @param yOffset the baseline of this font
     * @param height the height of this font
     * @param glyphs the glyphs
     */
    public GrimFont(LabFile container, String name, int firstChar, int lastChar, int yOffset, int height, List<FontGlyph> glyphs) {
        super(container, name);
        this.firstChar = firstChar;
        this.lastChar = lastChar;
        this.yOffset = yOffset;
        this.height = height;
        this.glyphs = new LinkedList<FontGlyph>();
        this.glyphs.addAll(glyphs);
    }

    /**
     * Returns the glyph that corresponds to the character code specified
     * @param c the code
     * @return the glyph or null if none was found
     */
    public FontGlyph getGlyph(char c) {
        if(glyphs.get(c).index == c)
            return glyphs.get(c);
        for(FontGlyph g: glyphs) {
            if(g.index == c)
                return g;
        }
        return null;
    }
}