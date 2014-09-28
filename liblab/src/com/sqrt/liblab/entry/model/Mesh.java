package com.sqrt.liblab.entry.model;

import com.sqrt.liblab.threed.Bounds3;
import com.sqrt.liblab.threed.Vector3f;

import java.util.LinkedList;
import java.util.List;

/**
 * A mesh contained in a model
 */
public class Mesh {
    /**
     * The name of this mesh
     */
    public String name;
    public int geomMode, lightMode, texMode, shadow;
    public float radius;
    /**
     * The faces of this mesh
     */
    public final List<MeshFace> faces = new LinkedList<MeshFace>();

    /**
     * Calculates the 3d bounds of this mesh
     * @param pos the offset of this mesh from the model origin
     * @return the bounds
     */
    public Bounds3 getBounds(Vector3f pos) {
        Bounds3 result = faces.get(0).getBounds(pos);
        for(int i = 1; i < faces.size(); i++)
            result = result.encapsulate(faces.get(i).getBounds(pos));
        return result;
    }
}