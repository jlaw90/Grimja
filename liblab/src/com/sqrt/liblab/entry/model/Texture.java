package com.sqrt.liblab.entry.model;

import java.awt.image.BufferedImage;

/**
 * A texture
 */
public class Texture {
    /**
     * Whether index 0 should be transparent
     */
    public boolean hasAlpha;
    /**
     * The width of this texture in pixels
     */
    public final int width;
    /**
     * The height of this texture in pixels
     */
    public final int height;
    /**
     * Indices into a colormap
     */
    public final byte[] indices;

    /**
     * Constructs a new texture of the specified dimension with the specified indices
     * @param width the width of the texture
     * @param height the height of the texture
     * @param indices the indices into the colormap
     */
    public Texture(int width, int height, byte[] indices) {
        this.width = width;
        this.height = height;
        this.indices = indices;
    }

    /**
     * Returns a bufferedimage that represents this texture when indexed against the specified colormap
     * @param colorMap the colormap that contains the colors
     * @return an image
     */
    public BufferedImage render(ColorMap colorMap) {
        if (colorMap == null)
            return null;
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED, colorMap.toColorModel());
        bi.getRaster().setDataElements(0, 0, width, height, indices);
        return bi;
    }
}