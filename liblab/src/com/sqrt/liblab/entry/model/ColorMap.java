package com.sqrt.liblab.entry.model;

import com.sqrt.liblab.entry.LabEntry;
import com.sqrt.liblab.LabFile;

import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;

/**
 * A palette of colors.  Used for texturing
 */
public class ColorMap extends LabEntry {
    /**
     * The palette
     */
    public final int[] colors = new int[256];

    public ColorMap(LabFile container, String name) {
        super(container, name);
    }

    /**
     * Creates a colormodel that can be used for mapping a texture to a BufferedImage
     * @return an indexcolormodel with this colormap as the palette
     */
    public IndexColorModel toColorModel() {
        return new IndexColorModel(8, 256, colors, 0, false, 0, DataBuffer.TYPE_BYTE);
    }
}