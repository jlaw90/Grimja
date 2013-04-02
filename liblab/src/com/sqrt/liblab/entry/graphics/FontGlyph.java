package com.sqrt.liblab.entry.graphics;

import java.awt.image.BufferedImage;

/**
 * A glyph for a font
 */
public class FontGlyph {
    /**
     * The character code of this glyph (not necessarily ASCII)
     */
    public int index;
    /**
     * The width of this glyph
     */
    public int charWidth;
    /**
     * The x offset of this glyph
     */
    public int xOff;
    /**
     * The y offset of this glyph
     */
    public int yOff;
    /**
     * The glyph in ARGB with just the alpha value of each pixel set
     */
    public BufferedImage mask;
}