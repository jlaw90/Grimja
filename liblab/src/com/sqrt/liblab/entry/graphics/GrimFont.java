package com.sqrt.liblab.entry.graphics;

import com.sqrt.liblab.entry.LabEntry;
import com.sqrt.liblab.LabFile;

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