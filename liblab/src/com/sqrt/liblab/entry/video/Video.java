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

import com.sqrt.liblab.entry.LabEntry;
import com.sqrt.liblab.LabFile;

public class Video extends LabEntry {
    public int format, numFrames;
    public final AudioTrack audio = new AudioTrack();
    public VideoInputStream stream;
    public int width, height;
    public float fps;

    public Video(LabFile container, String name) {
        super(container, name);
    }
}