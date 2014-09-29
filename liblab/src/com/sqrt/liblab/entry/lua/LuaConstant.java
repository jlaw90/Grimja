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

package com.sqrt.liblab.entry.lua;

/**
 * Created by James on 29/09/2014.
 */
public final class LuaConstant<T> {
    protected final T value;

    public LuaConstant(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public String toString() {
        return String.valueOf(value);
    }
}