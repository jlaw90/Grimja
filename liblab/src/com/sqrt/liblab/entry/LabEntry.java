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

package com.sqrt.liblab.entry;

import com.sqrt.liblab.LabFile;

/**
 * Represents an entry contained within a <code>LabFile</code>
 */
public abstract class LabEntry {
    /**
     * The container of this entry
     */
    public final LabFile container;
    private String name;

    /**
     * The only constructor.  Enforces model objects to provide a container and name
     * @param container the container of this entry
     * @param name the name of this entry
     */
    protected LabEntry(LabFile container, String name) {
        this.container = container;
        setName(name);
    }

    /**
     * Returns the name of this entry
     * @return this entry's name
     */
    public final String getName() {
        return name;
    }

    /**
     * Renames this entry
     * @param name the new name for this entry
     */
    public final void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }
}