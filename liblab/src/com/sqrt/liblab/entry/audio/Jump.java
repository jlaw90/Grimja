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

/**
 * A jump from one region to another
 */
public class Jump {
    /**
     * Unknown, but most likely used to enable or disable jumps from LUA
     */
    public int hookId;
    /**
     * How long it takes to fade from one region to the other? (I guess)
     */
    public int fadeDelay;
    /**
     * The target region for this jump
     */
    public Region target;
}