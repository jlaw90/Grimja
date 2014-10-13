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

/**
 * A 3d bounding box
 */
public class Bounds3 {
    /**
     * The minimum vector of this bounding box
     */
    public final Vector3f min;
    /**
     * The maximum vector of this bounding box
     */
    public final Vector3f max;
    /**
     * The center of this bounding box
     */
    public final Vector3f center;
    /**
     * The extents of this bounding box
     */
    public final Vector3f extent;


    /**
     * Constructs a new bounding box from the specified minimum and maximum vectors
     * @param min the minimum vector
     * @param max the maximum vector
     */
    public Bounds3(Vector3f min, Vector3f max) {
        this.min = min;
        this.max = max;
        this.extent = max.sub(min).div(2f);
        this.center = min.add(extent);
    }

    /**
     * Returns a bounding box that encapsulates both this and the specified bounding box
     * @param b the bounding box we also want the result to contain
     * @return a bounding box that contains the specified bounding box and ourselves
     */
    public Bounds3 encapsulate(Bounds3 b) {
        return new Bounds3(
                new Vector3f(Math.min(b.min.x, min.x), Math.min(b.min.y, min.y), Math.min(b.min.z, min.z)),
                new Vector3f(Math.max(b.max.x, max.x), Math.max(b.max.y, max.y), Math.max(b.max.z, max.z))
        );
    }
}