package com.sqrt.liblab.entry.graphics;

import com.sqrt.liblab.LabEntry;
import com.sqrt.liblab.LabFile;

import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

public class GrimBitmap extends LabEntry {
    public final List<BufferedImage> images;

    public GrimBitmap(LabFile container, String name) {
        super(container, name);
        images = new LinkedList<BufferedImage>();
    }
}