package com.sqrt.liblab.model;

import java.util.LinkedList;
import java.util.List;

public class MeshFace {
    public int type;
    public int geo;
    public int light;
    public float extraLight;
    public Vector3f normal;
    public List<Vector3f> vertices = new LinkedList<Vector3f>();
    public List<Vector3f> normals = new LinkedList<Vector3f>();
    public List<Vector2f> uv = new LinkedList<Vector2f>();
    public Texture texture;

    public Bounds3d getBounds(Vector3f pos) {
        float mx, my, mz, MX, MY, MZ;
        mx=my=mz=9999;
        MX=MY=MZ=-9999;
        for(Vector3f v: vertices) {
            v = v.add(pos);
            mx = Math.min(mx, v.x);
            my = Math.min(my, v.y);
            mz = Math.min(mz, v.z);
            MX = Math.max(MX, v.x);
            MY = Math.max(MY, v.y);
            MZ = Math.max(MZ, v.z);
        }
        return new Bounds3d(new Vector3f(mx, my, mz), new Vector3f(MX, MY, MZ));
    }
}