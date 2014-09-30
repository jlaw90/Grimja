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

package com.sqrt.liblab.entry.model;

import com.sqrt.liblab.threed.Bounds3;
import com.sqrt.liblab.threed.Vector2f;
import com.sqrt.liblab.threed.Vector3f;

import java.util.LinkedList;
import java.util.List;

/**
 * A face of a mesh
 */
public class MeshFace {

    public int type;
    public int geo;
    public int light;
    public float extraLight;
    /**
     * The normal of this face
     */
    public Vector3f normal;
    /**
     * The vertices that make up this face
     */
    public List<Vector3f> vertices = new LinkedList<Vector3f>();
    /**
     * The vertex normals
     */
    public List<Vector3f> normals = new LinkedList<Vector3f>();
    /**
     * The texture mapping coordinates
     */
    public List<Vector2f> uv = new LinkedList<Vector2f>();
    /**
     * The texture that should be mapped onto this face
     */
    public Texture texture;
    /**
     * The material for this surface
     * Texture above is naive, a material can have multiple textures that are chosen at runtime
     */
    public Material material;

    /**
     * Calculates the bounds of this face
     * @param pos the offset of this face from the origin
     * @return the calculated bounds
     */
    public Bounds3 getBounds(Vector3f pos) {
        float mx, my, mz, MX, MY, MZ;
        mx=my=mz=9999;
        MX=MY=MZ=-9999;
        for(Vector3f v: vertices) {
            v = v.add(pos);
            mx = Math.min(mx, v.x);
            my = Math.min(my, v.y);
            mz = Math.min(mz, v.z);
            MX = Math.max(MX, v.x);
            MY = Math.max(MY, v.y);
            MZ = Math.max(MZ, v.z);
        }
        return new Bounds3(new Vector3f(mx, my, mz), new Vector3f(MX, MY, MZ));
    }
}