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

package com.sqrt.liblab.entry.video;

import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;

public abstract class VideoInputStream {
    public abstract void seek(int pos) throws IOException;

    public abstract void setFrame(int frame) throws IOException;

    public abstract boolean readFrame(WritableRaster raster, int width, int height) throws IOException;
}