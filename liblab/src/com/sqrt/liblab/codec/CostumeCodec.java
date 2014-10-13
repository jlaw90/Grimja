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

package com.sqrt.liblab.codec;

import com.sqrt.liblab.entry.costume.ComponentTag;
import com.sqrt.liblab.entry.costume.Costume;
import com.sqrt.liblab.io.DataSource;
import com.sqrt.liblab.io.TextParser;

import java.io.IOException;

/**
 * Created by James on 02/10/2014.
 */
public class CostumeCodec extends EntryCodec<Costume> {
    protected Costume _read(DataSource source) throws IOException {
        TextParser t = new TextParser(source);
        t.expectString("costume v0.1");
        t.skipWhitespace();
        t.expectString("section tags");
        t.skipWhitespace();
        t.expectString("numtags");

        ComponentTag[] tagMap = new ComponentTag[t.readInt()];
        for(int i = 0; i < tagMap.length; i++) {
            t.skipWhitespace();
            int idx = t.readInt();
            t.skipWhitespace();
            t.expectString("'");
            String name = t.readString(4);
            t.expectString("'");
            tagMap[idx] = ComponentTag.fromTag(name);
            t.nextLine();
        }

        t.skipWhitespace();
        t.expectString("section components");
        t.skipWhitespace();
        t.expectString("numcomponents");
        int n = t.readInt();
        for(int i = 0; i < n; i++) {
            int id = t.readInt();
            int tagId = t.readInt();
            int hash = t.readInt(false, false);
            int parentId = t.readInt();
            int nameIdx = t.readInt();

            t.skipWhitespace();
            String name = t.remaining();
            System.out.println(name);
        }

        return null;
    }

    public DataSource write(Costume source) throws IOException {
        throw new UnsupportedOperationException(); // Todo
    }

    public String[] getFileExtensions() {
        return new String[] {"cos"};
    }

    public Class<Costume> getEntryClass() {
        return Costume.class;
    }
}
