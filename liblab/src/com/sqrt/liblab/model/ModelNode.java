package com.sqrt.liblab.model;

import com.sqrt.liblab.LabEntry;
import com.sqrt.liblab.LabFile;

public class ModelNode {
    public String name;
    public Mesh mesh;
    public ModelNode parent, child, sibling;
    public Vector3f pos, pivot;
    public float yaw, pitch, roll;
    public int flags, type, depth;
    public int childIdx = -1, siblingIdx = -1, parentIdx = -1;
    public Vector3f animPos;
    public float animYaw, animPitch, animRoll;

    public String toString() {
        return name;
    }

    public Bounds3d getBounds(Vector3f pos) {
        Bounds3d ourBounds = null;
        pos = pos.add(this.pos);
        if (mesh != null)
            ourBounds = mesh.getBounds(pos.add(pivot));
        // Make sure we encapsulate our children...
        if(child != null) {
            ModelNode child = this.child;
            while(child != null) {
                Bounds3d childBounds = child.getBounds(pos);
                if(ourBounds == null)
                    ourBounds = childBounds;
                else if(childBounds != null)
                    ourBounds = ourBounds.encapsulate(childBounds);
                child = child.sibling;
            }
        }
        return ourBounds;
    }
}