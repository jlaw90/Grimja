package com.sqrt.liblab.entry.model;

import com.sqrt.liblab.LabEntry;
import com.sqrt.liblab.LabFile;

public class ColorMap extends LabEntry {
    public final int[] colors = new int[256];

    public ColorMap(LabFile container, String name) {
        super(container, name);
    }
}