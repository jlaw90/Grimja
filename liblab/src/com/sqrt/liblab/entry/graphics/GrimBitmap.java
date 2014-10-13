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

package com.sqrt.liblab.entry.graphics;

import com.sqrt.liblab.LabFile;
import com.sqrt.liblab.entry.LabEntry;

import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

/**
 * A bitmap
 */
public class GrimBitmap extends LabEntry {
    /**
     * The images contained within this bitmap
     */
    public final List<BufferedImage> images;

    public GrimBitmap(LabFile container, String name) {
        super(container, name);
        images = new LinkedList<BufferedImage>();
    }
}