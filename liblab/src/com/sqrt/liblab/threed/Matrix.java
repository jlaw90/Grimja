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