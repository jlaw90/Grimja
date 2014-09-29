/*
 * Copyright (C) 2014  James Lawrence.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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