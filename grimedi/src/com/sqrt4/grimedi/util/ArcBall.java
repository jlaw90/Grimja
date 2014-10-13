

/*
 * Copyright (C) 2014  James Lawrence.
 *
 *     This file is part of GrimEdi.
 *
 *     GrimEdi is free software: you can redistribute it and/or modify
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

package com.sqrt4.grimedi.util;

import com.sqrt.liblab.threed.Quaternion;
import com.sqrt.liblab.threed.Vector2f;
import com.sqrt.liblab.threed.Vector3f;

import java.awt.*;

public class ArcBall {
    private static final float Epsilon = 1.0e-5f;

    Vector3f start;
    Vector3f end;
    float adjustWidth;
    float adjustHeight;

    Quaternion total = Quaternion.zero;

    public ArcBall(float width, float height) {
        start = Vector3f.zero;
        end = Vector3f.zero;
        setBounds(width, height);
    }

    public void setBounds(float width, float height) {
        adjustWidth = 1.0f / ((width - 1.0f) * 0.5f);
        adjustHeight = 1.0f / ((height - 1.0f) * 0.5f);
    }

    public Vector3f mapToSphere(Point pt) {
        Vector2f n = new Vector2f((pt.x * adjustWidth) - 1f, 1f - (pt.y * adjustHeight));
        float length = n.x * n.x + n.y * n.y;

        if (length > 1.0f) {
            float norm = -(float) (1.0 / Math.sqrt(length));
            return new Vector3f(n.x * norm, n.y * norm, 0);
        } else
            return new Vector3f(n.x, n.y, (float) Math.sqrt(1f - length));
    }

    public void dragStart(Point pt) {
        start = mapToSphere(pt);
    }

    public Quaternion drag(Point pt) {
        end = mapToSphere(pt);

        Vector3f perp = start.cross(end);
        Quaternion local;
        if (perp.length() > Epsilon)
            local = new Quaternion(perp.x, perp.y, perp.z, 0.3f * start.dot(end));
        else
            local = Quaternion.zero;
        total = total.multiply(local);
        return total;
    }

    public void reset() {
        total = Quaternion.zero;
    }
}