package com.sqrt.liblab.entry.model;

import com.sqrt.liblab.entry.LabEntry;
import com.sqrt.liblab.LabFile;
import com.sqrt.liblab.threed.Angle;
import com.sqrt.liblab.threed.Bounds3;
import com.sqrt.liblab.threed.Vector3f;

import java.util.LinkedList;
import java.util.List;

/**
 * A 3D model
 */
public class GrimModel extends LabEntry {
    /**
     * The geosets of this model
     */
    public final List<Geoset> geosets = new LinkedList<Geoset>();
    /**
     * The hierarchy used for animation
     */
    public final List<ModelNode> hierarchy = new LinkedList<ModelNode>();
    /**
     * The 3d offset of this model
     */
    public Vector3f off;
    /**
     * The radius of a bounding sphere?
     */
    public float radius;

    public GrimModel(LabFile container, String name) {
        super(container, name);
    }

    /**
     * Returns the 3d bounds of this model
     * @return the bounds
     */
    public Bounds3 getBounds() {
        return hierarchy.get(0).getBounds(new Vector3f(0, 0, 0));
    }

    /**
     * Attempts to locate the ModelNode with the specified name
     * @param meshName the name of the mesh
     * @return the located node or null if not found
     */
    public ModelNode findNode(String meshName) {
        for(ModelNode node: hierarchy) {
            if(node.name.equalsIgnoreCase(meshName))
                return node;
            if(node.mesh != null && node.mesh.name.equalsIgnoreCase(meshName))
                return node;
        }
        return null;
    }

    public void reset() {
        for(ModelNode node: hierarchy) {
            node.animPos = Vector3f.zero;
            node.animYaw = node.animRoll = node.animPitch = Angle.zero;
        }
    }
}