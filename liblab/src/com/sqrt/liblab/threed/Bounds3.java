package com.sqrt.liblab.threed;

public class Bounds3 {
    public final Vector3 min, max, center, extent;

    public Bounds3(Vector3 min, Vector3 max) {
        this.min = min;
        this.max = max;
        this.extent = max.sub(min).div(2f);
        this.center = min.add(extent);
    }

    public Bounds3 encapsulate(Bounds3 b) {
        return new Bounds3(
                new Vector3(Math.min(b.min.x, min.x), Math.min(b.min.y, min.y), Math.min(b.min.z, min.z)),
                new Vector3(Math.max(b.max.x, max.x), Math.max(b.max.y, max.y), Math.max(b.max.z, max.z))
        );
    }
}