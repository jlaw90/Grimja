package com.sqrt.liblab.entry.model;

import com.sqrt.liblab.threed.Bounds3;
import com.sqrt.liblab.threed.Vector2;
import com.sqrt.liblab.threed.Vector3;

import java.util.LinkedList;
import java.util.List;

public class MeshFace {
    public int type;
    public int geo;
    public int light;
    public float extraLight;
    public Vector3 normal;
    public List<Vector3> vertices = new LinkedList<Vector3>();
    public List<Vector3> normals = new LinkedList<Vector3>();
    public List<Vector2> uv = new LinkedList<Vector2>();
    public Texture texture;

    public Bounds3 getBounds(Vector3 pos) {
        float mx, my, mz, MX, MY, MZ;
        mx=my=mz=9999;
        MX=MY=MZ=-9999;
        for(Vector3 v: vertices) {
            v = v.add(pos);
            mx = Math.min(mx, v.x);
            my = Math.min(my, v.y);
            mz = Math.min(mz, v.z);
            MX = Math.max(MX, v.x);
            MY = Math.max(MY, v.y);
            MZ = Math.max(MZ, v.z);
        }
        return new Bounds3(new Vector3(mx, my, mz), new Vector3(MX, MY, MZ));
    }
}