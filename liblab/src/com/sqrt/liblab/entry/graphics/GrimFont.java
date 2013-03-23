package com.sqrt.liblab.entry.graphics;

import com.sqrt.liblab.LabEntry;
import com.sqrt.liblab.LabFile;

import java.util.LinkedList;
import java.util.List;

public class GrimFont extends LabEntry {
    public int height;
    public int yOffset;
    public int firstChar, lastChar;
    public List<FontGlyph> glyphs;

    public GrimFont(LabFile container, String name, int firstChar, int lastChar, int yOffset, int height, List<FontGlyph> glyphs) {
        super(container, name);
        this.firstChar = firstChar;
        this.lastChar = lastChar;
        this.yOffset = yOffset;
        this.height = height;
        this.glyphs = new LinkedList<FontGlyph>();
        this.glyphs.addAll(glyphs);
    }

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