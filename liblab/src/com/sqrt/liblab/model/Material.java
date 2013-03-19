package com.sqrt.liblab.model;

import com.sqrt.liblab.LabEntry;
import com.sqrt.liblab.LabFile;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.util.LinkedList;
import java.util.List;

public class Material extends LabEntry {
    private ColorMap colorMap;
    public final List<Texture> textures = new LinkedList<Texture>();

    public Material(LabFile container, String name) {
        super(container, name);
    }

    public void setColorMap(ColorMap colorMap) {
        this.colorMap = colorMap;
    }

    public BufferedImage renderTexture(int tex) {
        return textures.get(tex).render(colorMap);
    }
}