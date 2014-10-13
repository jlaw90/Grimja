/*
 * Copyright (C) 2014  James Lawrence.
 *
 *     This file is part of LibLab.
 *
 *     LibLab is free software: you can redistribute it and/or modify
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

public class Quaternion {
    public final float x, y, z, w;
    public static final Quaternion zero = new Quaternion(0,0,0,1);

    public Quaternion(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Quaternion normalize() {
        float norm = (float) Math.sqrt(x * x + y * y + z * z + w * w);
        if (norm > 0.0f)
            return new Quaternion(x/norm, y/norm, z/norm, w/norm);
        else
            return Quaternion.zero;
    }

    public Vector3f rotate(Vector3f v) {
        float q00 = 2.0f * x * x;
        float q11 = 2.0f * y * y;
        float q22 = 2.0f * z * z;

        float q01 = 2.0f * x * y;
        float q02 = 2.0f * x * z;
        float q03 = 2.0f * x * w;

        float q12 = 2.0f * y * z;
        float q13 = 2.0f * y * w;

        float q23 = 2.0f * z * w;

        return new Vector3f((1.0f - q11 - q22) * v.x + (q01 - q23) * v.y
                + (q02 + q13) * v.z, (q01 + q23) * v.x + (1.0f - q22 - q00) * v.y
                + (q12 - q03) * v.z, (q02 - q13) * v.x + (q12 + q03) * v.y
                + (1.0f - q11 - q00) * v.z);
    }

    public final Matrix matrix() {
        float q00 = 2.0f * this.x * this.x;
        float q11 = 2.0f * this.y * this.y;
        float q22 = 2.0f * this.z * this.z;

        float q01 = 2.0f * this.x * this.y;
        float q02 = 2.0f * this.x * this.z;
        float q03 = 2.0f * this.x * this.w;

        float q12 = 2.0f * this.y * this.z;
        float q13 = 2.0f * this.y * this.w;

        float q23 = 2.0f * this.z * this.w;

        float m00 = 1.0f - q11 - q22;
        float m01 = q01 - q23;
        float m02 = q02 + q13;

        float m10 = q01 + q23;
        float m11 = 1.0f - q22 - q00;
        float m12 = q12 - q03;

        float m20 = q02 - q13;
        float m21 = q12 + q03;
        float m22 = 1.0f - q11 - q00;

        float m30 = 0.0f;
        float m31 = 0.0f;
        float m32 = 0.0f;

        float m03 = 0.0f;
        float m13 = 0.0f;
        float m23 = 0.0f;
        float m33 = 1.0f;

        return new Matrix(m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22,
                m23, m30, m31, m32, m33);
    }

    public Quaternion inverse() {
        float sq = (float) Math.sqrt(x*x+y*y+z*z+w*w);
        return new Quaternion(x/-sq, y/-sq, z/-sq, w/sq);
    }

    public Quaternion multiply(Quaternion q1) {
        float x, y, z, w;

        w = this.w * q1.w - this.x * q1.x - this.y * q1.y - this.z * q1.z;
        x = this.w * q1.x + q1.w * this.x + this.y * q1.z - this.z * q1.y;
        y = this.w * q1.y + q1.w * this.y - this.x * q1.z + this.z * q1.x;
        z = this.w * q1.z + q1.w * this.z + this.x * q1.y - this.y * q1.x;
        return new Quaternion(x, y, z, w);
    }


    public Vector3f mult(Vector3f v) {
        float tempX, tempY;
        tempX = w * w * v.x + 2 * y * w * v.z - 2 * z * w * v.y + x * x * v.x
                + 2 * y * x * v.y + 2 * z * x * v.z - z * z * v.x - y * y * v.x;
        tempY = 2 * x * y * v.x + y * y * v.y + 2 * z * y * v.z + 2 * w * z
                * v.x - z * z * v.y + w * w * v.y - 2 * x * w * v.z - x * x
                * v.y;
        float tempZ = 2 * x * z * v.x + 2 * y * z * v.y + z * z * v.z - 2 * w * y * v.x
                - y * y * v.z + 2 * w * x * v.y - x * x * v.z + w * w * v.z;
        return new Vector3f(tempX, tempY, tempZ);
    }


    public float[] toEulerAngles(float[] angles) {
        if (angles == null)
            angles = new float[3];
        else if (angles.length != 3)
            throw new IllegalArgumentException("Angles array must have three elements");

        float sqw = w * w;
        float sqx = x * x;
        float sqy = y * y;
        float sqz = z * z;
        float unit = sqx + sqy + sqz + sqw; // if normalized is one, otherwise
        // is correction factor
        float test = x * y + z * w;
        if (test > 0.499 * unit) { // singularity at north pole
            angles[1] = 2 * (float) Math.atan2(x, w);
            angles[2] = (float) (Math.PI/2d);
            angles[0] = 0;
        } else if (test < -0.499 * unit) { // singularity at south pole
            angles[1] = -2 * (float) Math.atan2(x, w);
            angles[2] = (float) -(Math.PI/2d);
            angles[0] = 0;
        } else {
            angles[1] = (float) Math.atan2(2 * y * w - 2 * x * z, sqx - sqy - sqz + sqw); // roll or heading
            angles[2] = (float) Math.asin(2 * test / unit); // pitch or attitude
            angles[0] = (float) Math.atan2(2 * x * w - 2 * y * z, -sqx + sqy - sqz + sqw); // yaw or bank
        }
        return angles;
    }

    public static Quaternion fromEulerAngles(float yaw, float roll, float pitch) {
        float angle;
        yaw = (float) Math.toRadians(yaw);
        roll = (float) Math.toRadians(roll);
        pitch = (float) Math.toRadians(pitch);
        float sinRoll, sinPitch, sinYaw, cosRoll, cosPitch, cosYaw;
        angle = pitch * 0.5f;
        sinPitch = (float) Math.sin(angle);
        cosPitch = (float) Math.cos(angle);
        angle = roll * 0.5f;
        sinRoll = (float) Math.sin(angle);
        cosRoll = (float) Math.cos(angle);
        angle = yaw * 0.5f;
        sinYaw = (float) Math.sin(angle);
        cosYaw = (float) Math.cos(angle);

        // variables used to reduce multiplication calls.
        float cosRollXcosPitch = cosRoll * cosPitch;
        float sinRollXsinPitch = sinRoll * sinPitch;
        float cosRollXsinPitch = cosRoll * sinPitch;
        float sinRollXcosPitch = sinRoll * cosPitch;

        float w = (cosRollXcosPitch * cosYaw - sinRollXsinPitch * sinYaw);
        float x = (cosRollXcosPitch * sinYaw + sinRollXsinPitch * cosYaw);
        float y = (sinRollXcosPitch * cosYaw + cosRollXsinPitch * sinYaw);
        float z = (cosRollXsinPitch * cosYaw - sinRollXcosPitch * sinYaw);

        return new Quaternion(x, y, z, w).normalize();
    }

    public static Quaternion slerp(Quaternion q1, Quaternion q2, float t) {
        // Create a local quaternion to store the interpolated quaternion
        if (q1.x == q2.x && q1.y == q2.y && q1.z == q2.z && q1.w == q2.w)
            return q1;

        float result = (q1.x * q2.x) + (q1.y * q2.y) + (q1.z * q2.z) + (q1.w * q2.w);

        if (result < 0.0f) {
            // Negate the second quaternion and the result of the dot product
            q2 = new Quaternion(-q2.x, -q2.y, -q2.z, -q2.w);
            result = -result;
        }

        // Set the first and second scale for the interpolation
        float scale0 = 1 - t;
        float scale1 = t;

        // Check if the angle between the 2 quaternions was big enough to
        // warrant such calculations
        if ((1 - result) > 0.1f) {// Get the angle between the 2 quaternions,
            // and then store the sin() of that angle
            float theta = (float) Math.acos(result);
            float invSinTheta = 1f / (float) Math.sin(theta);

            // Calculate the scale for q1 and q2, according to the angle and
            // it's sine value
            scale0 = (float) Math.sin((1 - t) * theta) * invSinTheta;
            scale1 = (float) Math.sin((t * theta)) * invSinTheta;
        }

        // Calculate the x, y, z and w values for the quaternion by using a
        // special
        // form of linear interpolation for quaternions.
        float x = (scale0 * q1.x) + (scale1 * q2.x);
        float y = (scale0 * q1.y) + (scale1 * q2.y);
        float z = (scale0 * q1.z) + (scale1 * q2.z);
        float w = (scale0 * q1.w) + (scale1 * q2.w);

        // Return the interpolated quaternion
        return new Quaternion(x, y, z, w);
    }
}