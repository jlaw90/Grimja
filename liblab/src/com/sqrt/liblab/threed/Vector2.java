package com.sqrt.liblab.threed;

public class Vector2 implements Comparable<Vector2> {
    public final float x, y;

    public Vector2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vector2 div(Vector2 o) {
        return new Vector2(x/o.x, y/o.y);
    }

    public boolean equals(Object o) {
        return o == this || (o instanceof Vector2 && equals((Vector2) o));
    }

    public boolean equals(Vector2 v) {
        return v == this || (v.x == x && v.y == y);
    }

    public int hashCode() {
        return ((Float.floatToIntBits(x) & 0xffff) << 16) | (Float.floatToIntBits(y) & 0xffff);
    }

    public int compareTo(Vector2 o) {
        return Float.compare(x, o.x) + Float.compare(y, o.y);
    }
}