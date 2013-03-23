package com.sqrt.liblab.entry.model;

import com.sqrt.liblab.threed.Bounds3;
import com.sqrt.liblab.threed.Vector3;

import java.util.LinkedList;
import java.util.List;

public class Mesh {
    public String name;
    public int geomMode, lightMode, texMode, shadow;
    public float radius;
    public final List<MeshFace> faces = new LinkedList<MeshFace>();

    public Bounds3 getBounds(Vector3 pos) {
        Bounds3 result = faces.get(0).getBounds(pos);
        for(int i = 1; i < faces.size(); i++)
            result = result.encapsulate(faces.get(i).getBounds(pos));
        return result;
    }
}