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

package com.sqrt.liblab.entry.costume;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by James on 02/10/2014.
 */
public enum ComponentTag {
    MAIN_MODEL("MMDL"),
    MODEL("MODL"),
    COLORMAP("CMAP"),
    KEYFRAME("KEYF"),
    MESH("MESH"),
    LUA_VAR("LUAV"),
    IMUSE("IMLS"),
    BITMAP("BKND"),
    MATERIAL("MAT "),
    SPRITE("SPRT"),
    ANIMATION("ANIM");

    private static final Map<String, ComponentTag> reverseLookup = new HashMap<>();
    public final String tag;

    private ComponentTag(String tag) {
        this.tag = tag;
    }

    public String toString() {
        return tag;
    }

    public static ComponentTag fromTag(String tag) {
        return reverseLookup.get(tag.toLowerCase());
    }

    static {
        for (ComponentTag ct : EnumSet.allOf(ComponentTag.class))
            reverseLookup.put(ct.tag.toLowerCase(), ct);
    }
}