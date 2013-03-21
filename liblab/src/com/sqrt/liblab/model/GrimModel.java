package com.sqrt.liblab.model;

import com.sqrt.liblab.LabEntry;
import com.sqrt.liblab.LabFile;

import java.util.LinkedList;
import java.util.List;

public class GrimModel extends LabEntry {
    public final List<Geoset> geosets = new LinkedList<Geoset>();
    public final List<ModelNode> hierarchy = new LinkedList<ModelNode>();
    public Vector3f off;
    public float radius;

    public GrimModel(LabFile container, String name) {
        super(container, name);
    }

    public Bounds3d getBounds() {
        return hierarchy.get(0).getBounds(new Vector3f(0, 0, 0));
    }
}