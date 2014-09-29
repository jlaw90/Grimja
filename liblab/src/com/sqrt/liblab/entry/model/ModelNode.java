/*
 * Copyright (C) 2014  James Lawrence.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.sqrt.liblab.entry.model;

import com.sqrt.liblab.threed.Angle;
import com.sqrt.liblab.threed.Bounds3;
import com.sqrt.liblab.threed.Vector3f;

/**
 * A ModelNode to be used for animation
 */
public class ModelNode {
    /**
     * The name of this node
     */
    public String name;
    /**
     * The mesh of this node (can be null)
     */
    public Mesh mesh;
    /**
     * The parent of this node
     */
    public ModelNode parent;
    /**
     * The first child of this node
     */
    public ModelNode child;
    /**
     * The next sibling in this layer
     */
    public ModelNode sibling;
    /**
     * The position of this node
     */
    public Vector3f pos;
    /**
     * The pivot point for rotations
     */
    public Vector3f pivot;
    /**
     * The yaw rotation
     */
    public Angle yaw;
    /**
     * The pitch rotation
     */
    public Angle pitch;
    /**
     * The roll rotation
     */
    public Angle roll;
    public int flags, type, depth;
    public int childIdx = -1, siblingIdx = -1, parentIdx = -1;
    public boolean hierarchyVisible = true;
    public boolean meshVisible = true;
    public Vector3f animPos = Vector3f.zero;
    public Angle animYaw = Angle.zero, animPitch = Angle.zero, animRoll = Angle.zero;

    public String toString() {
        return name;
    }

    /**
     * Calculates the bounds of this node from the specified position
     * @param pos the position
     * @return the bounds
     */
    public Bounds3 getBounds(Vector3f pos) {
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