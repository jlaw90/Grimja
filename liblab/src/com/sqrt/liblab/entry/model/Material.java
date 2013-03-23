package com.sqrt.liblab.entry.model;

import com.sqrt.liblab.LabEntry;
import com.sqrt.liblab.LabFile;

import java.util.LinkedList;
import java.util.List;

public class Material extends LabEntry {
    public final List<Texture> textures = new LinkedList<Texture>();

    public Material(LabFile container, String name) {
        super(container, name);
    }
}