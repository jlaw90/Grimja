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
import com.sqrt.liblab.threed.Vector3f;

import java.util.LinkedList;
import java.util.List;

/**
 * A mesh contained in a model
 */
public class Mesh {
    /**
     * The name of this mesh
     */
    public String name;
    public int geomMode, lightMode, texMode, shadow;
    public float radius;
    /**
     * The faces of this mesh
     */
    public final List<MeshFace> faces = new LinkedList<MeshFace>();

    /**
     * Calculates the 3d bounds of this mesh
     * @param pos the offset of this mesh from the model origin
     * @return the bounds
     */
    public Bounds3 getBounds(Vector3f pos) {
        Bounds3 result = faces.get(0).getBounds(pos);
        for(int i = 1; i < faces.size(); i++)
            result = result.encapsulate(faces.get(i).getBounds(pos));
        return result;
    }
}