package com.sqrt.liblab.entry.model;

import com.sqrt.liblab.LabEntry;
import com.sqrt.liblab.LabFile;
import com.sqrt.liblab.threed.Bounds3;
import com.sqrt.liblab.threed.Vector3;

import java.util.LinkedList;
import java.util.List;

public class GrimModel extends LabEntry {
    public final List<Geoset> geosets = new LinkedList<Geoset>();
    public final List<ModelNode> hierarchy = new LinkedList<ModelNode>();
    public Vector3 off;
    public float radius;

    public GrimModel(LabFile container, String name) {
        super(container, name);
    }

    public Bounds3 getBounds() {
        return hierarchy.get(0).getBounds(new Vector3(0, 0, 0));
    }

    public ModelNode findNode(String meshName) {
        for(ModelNode node: hierarchy) {
            if(node.name.equalsIgnoreCase(meshName))
                return node;
            if(node.mesh != null && node.mesh.name.equalsIgnoreCase(meshName))
                return node;
        }
        return null;
    }
}