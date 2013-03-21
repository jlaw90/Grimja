package com.sqrt.liblab.model;

public class Bounds3d {
    public final Vector3f min, max;

    public Bounds3d(Vector3f min, Vector3f max) {
        this.min = min;
        this.max = max;
    }

    public Vector3f center() {
        return min.add(max.sub(min).div(2f));
    }

    public Bounds3d encapsulate(Bounds3d b) {
        return new Bounds3d(
                new Vector3f(Math.min(b.min.x, min.x), Math.min(b.min.y, min.y), Math.min(b.min.z, min.z)),
                new Vector3f(Math.max(b.max.x, max.x), Math.max(b.max.y, max.y), Math.max(b.max.z, max.z))
        );
    }
}