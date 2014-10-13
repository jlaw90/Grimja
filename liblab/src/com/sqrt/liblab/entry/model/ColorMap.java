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

package com.sqrt.liblab.entry.model;

import com.sqrt.liblab.LabFile;
import com.sqrt.liblab.entry.LabEntry;

import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;

/**
 * A palette of colors.  Used for texturing
 */
public class ColorMap extends LabEntry {
    /**
     * The palette
     */
    public final int[] colors = new int[256];

    public ColorMap(LabFile container, String name) {
        super(container, name);
    }

    /**
     * Creates a colormodel that can be used for mapping a texture to a BufferedImage
     * @return an indexcolormodel with this colormap as the palette
     */
    public IndexColorModel toColorModel() {
        return new IndexColorModel(8, 256, colors, 0, false, 0, DataBuffer.TYPE_BYTE);
    }
}