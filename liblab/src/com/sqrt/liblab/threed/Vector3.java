package com.sqrt.liblab.threed;

/**
 * A 3D vector
 */
public class Vector3 implements Comparable<Vector3> {
    public final float x, y, z;
    public static final Vector3 zero = new Vector3(0, 0, 0);

    public Vector3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public boolean equals(Object o) {
        return o == this || (o instanceof Vector3 && equals((Vector3) o));
    }

    public boolean equals(Vector3 v) {
        return v == this || (v.x == x && v.y == y && v.z == z);
    }

    public int hashCode() {
        return ((Float.floatToIntBits(x) & 0x3ff) << 20) | ((Float.floatToIntBits(y) & 0x3ff) << 10) |
                (Float.floatToIntBits(z)&0x3ff);
    }

    public int compareTo(Vector3 o) {
        return Float.compare(x, o.x) + Float.compare(y, o.y) + Float.compare(z, o.z);
    }

    public Vector3 add(Vector3 v) {
        return new Vector3(x+v.x, y+v.y, z+v.z);
    }

    public Vector3 sub(Vector3 v) {
        return new Vector3(x-v.x, y-v.y, z-v.z);
    }

    public Vector3 mult(float f) {
        return new Vector3(x*f, y*f, z*f);
    }

    public Vector3 div(float f) {
        return new Vector3(x/f, y/f, z/f);
    }

    public float magnitude() {
        return (float)Math.sqrt(x*x+y*y+z*z);
    }

    public float dotProduct(Vector3 o) {
        return (x*o.x)+(y*o.y)+(z*o.z);
    }
}