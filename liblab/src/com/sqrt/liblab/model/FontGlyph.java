package com.sqrt.liblab.model;

import com.sqrt.liblab.LabEntry;
import com.sqrt.liblab.LabFile;

import java.awt.*;
import java.awt.image.BufferedImage;

public class FontGlyph {
    public int index;
    public int charWidth, xOff, yOff;
    public BufferedImage mask;
}