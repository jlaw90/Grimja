package com.sqrt.liblab.entry.model;

import com.sqrt.liblab.entry.LabEntry;
import com.sqrt.liblab.LabFile;

import java.util.LinkedList;
import java.util.List;

/**
 * A material contains a list of textures to be used for 3d rendering
 */
public class Material extends LabEntry {
    /**
     * The textures
     */
    public final List<Texture> textures = new LinkedList<Texture>();

    public Material(LabFile container, String name) {
        super(container, name);
    }
}