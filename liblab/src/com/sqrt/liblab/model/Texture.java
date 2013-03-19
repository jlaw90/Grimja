package com.sqrt.liblab.model;

import java.awt.image.BufferedImage;

public class Texture {
    public boolean hasAlpha;
    public final int width, height;
    public final byte[] indices;

    public Texture(int width, int height, byte[] indices) {
        this.width = width;
        this.height = height;
        this.indices = indices;
    }

    public BufferedImage render(ColorMap colorMap) {
        if(colorMap == null)
            return null;
        int[] pix = new int[width*height];
        for(int i = 0; i < indices.length; i++) {
            int p = colorMap.colors[indices[i] & 0xff];
            if(!hasAlpha)
                p |= (0xff << 24);
            pix[i] = p;
        }
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        bi.setRGB(0, 0, width, height, pix, 0, width);
        return bi;
    }
}