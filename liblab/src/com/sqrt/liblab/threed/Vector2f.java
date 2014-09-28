package com.sqrt.liblab.threed;

/**
 * A 2D vector
 */
public class Vector2f implements Comparable<Vector2f> {
    public final float x, y;

    public Vector2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float length() {
        return (float) Math.sqrt(x*x+y*y);
    }

    public Vector2f div(Vector2f o) {
        return new Vector2f(x/o.x, y/o.y);
    }

    public boolean equals(Object o) {
        return o == this || (o instanceof Vector2f && equals((Vector2f) o));
    }

    public boolean equals(Vector2f v) {
        return v == this || (v.x == x && v.y == y);
    }

    public int hashCode() {
        return ((Float.floatToIntBits(x) & 0xffff) << 16) | (Float.floatToIntBits(y) & 0xffff);
    }

    public int compareTo(Vector2f o) {
        return Float.compare(x, o.x) + Float.compare(y, o.y);
    }
}