package com.sqrt.liblab.model;

public class Vector3f implements Comparable<Vector3f> {
    public final float x, y, z;
    public static final Vector3f zero = new Vector3f(0, 0, 0);

    public Vector3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public boolean equals(Object o) {
        return o == this || (o instanceof Vector3f && equals((Vector3f) o));
    }

    public boolean equals(Vector3f v) {
        return v == this || (v.x == x && v.y == y && v.z == z);
    }

    public int hashCode() {
        return ((Float.floatToIntBits(x) & 0x3ff) << 20) | ((Float.floatToIntBits(y) & 0x3ff) << 10) |
                (Float.floatToIntBits(z)&0x3ff);
    }

    public int compareTo(Vector3f o) {
        return Float.compare(x, o.x) + Float.compare(y, o.y) + Float.compare(z, o.z);
    }

    public Vector3f add(Vector3f v) {
        return new Vector3f(x+v.x, y+v.y, z+v.z);
    }

    public Vector3f sub(Vector3f v) {
        return new Vector3f(x-v.x, y-v.y, z-v.z);
    }

    public Vector3f mult(float f) {
        return new Vector3f(x*f, y*f, z*f);
    }

    public Vector3f div(float f) {
        return new Vector3f(x/f, y/f, z/f);
    }

    public float magnitude() {
        return (float)Math.sqrt(x*x+y*y+z*z);
    }

    public float dotProduct(Vector3f o) {
        return (x*o.x)+(y*o.y)+(z*o.z);
    }
}