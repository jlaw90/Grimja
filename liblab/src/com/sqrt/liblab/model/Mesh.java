package com.sqrt.liblab.model;

import java.util.LinkedList;
import java.util.List;

public class Mesh {
    public String name;
    public int geomMode, lightMode, texMode, shadow;
    public float radius;
    public final List<MeshFace> faces = new LinkedList<MeshFace>();

    public Bounds3d getBounds(Vector3f pos) {
        Bounds3d result = faces.get(0).getBounds(pos);
        for(int i = 1; i < faces.size(); i++)
            result = result.encapsulate(faces.get(i).getBounds(pos));
        return result;
    }
}