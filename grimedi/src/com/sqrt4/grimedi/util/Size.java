

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

package com.sqrt4.grimedi.util;

public class Size implements Comparable<Size> {
    private long size;

    public Size(long size) {
        this.size = size;
    }

    public String toString() {
        final String[] prefixes = {"B", "KB", "MB"};
        String res = size + "B";
        int size = 1;
        for (String prefixe : prefixes) {
            if (this.size >= size)
                res = (this.size / size) + prefixe;
            size *= 1024;
        }
        return res;
    }

    public int compareTo(Size o) {
        return Long.compare(size, o.size);
    }
}