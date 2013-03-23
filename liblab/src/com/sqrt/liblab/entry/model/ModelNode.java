package com.sqrt.liblab.entry.model;

import com.sqrt.liblab.threed.Angle;
import com.sqrt.liblab.threed.Bounds3;
import com.sqrt.liblab.threed.Vector3;

public class ModelNode {
    public String name;
    public Mesh mesh;
    public ModelNode parent, child, sibling;
    public Vector3 pos, pivot;
    public Angle yaw, pitch, roll;
    public int flags, type, depth;
    public int childIdx = -1, siblingIdx = -1, parentIdx = -1;
    public boolean hierarchyVisible = true;
    public boolean meshVisible = true;
    public Vector3 animPos = Vector3.zero;
    public Angle animYaw = Angle.zero, animPitch = Angle.zero, animRoll = Angle.zero;

    public String toString() {
        return name;
    }

    public Bounds3 getBounds(Vector3 pos) {
        Bounds3 ourBounds = null;
        pos = pos.add(this.pos);
        if (mesh != null)
            ourBounds = mesh.getBounds(pos.add(pivot));
        // Make sure we encapsulate our children...
        if(child != null) {
            ModelNode child = this.child;
            while(child != null) {
                Bounds3 childBounds = child.getBounds(pos);
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