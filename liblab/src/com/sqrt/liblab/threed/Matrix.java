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

import java.nio.FloatBuffer;

public class Matrix {
    public final FloatBuffer data = FloatBuffer.allocate(16);

    public Matrix(float m00, float m01, float m02, float m03, float m10, float m11, float m12, float m13,
                  float m20, float m21, float m22, float m23, float m30, float m31, float m32, float m33) {

    }

    public float get(int index) {
        return data.get(index);
    }

    public void set(int index, float f) {
        data.put(index, f);
    }
}