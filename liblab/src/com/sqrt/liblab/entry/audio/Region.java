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

package com.sqrt.liblab.entry.audio;

import java.util.LinkedList;
import java.util.List;

/**
 * A region within an iMUSE audio file
 */
public class Region {
    /**
     * The offset into the audio stream that this region starts at
     */
    public int offset;
    /**
     * The length of this region in bytes
     */
    public int length;
    /**
     * Comments from the developers about this region
     */
    public String comments;
    /**
     * Jumps to other regions
     */
    public List<Jump> jumps = new LinkedList<Jump>();
}