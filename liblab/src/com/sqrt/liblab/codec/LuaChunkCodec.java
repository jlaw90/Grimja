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

package com.sqrt.liblab.codec;

import com.sqrt.liblab.entry.lua.LocalVarDefinition;
import com.sqrt.liblab.entry.lua.LuaChunk;
import com.sqrt.liblab.entry.lua.LuaConstant;
import com.sqrt.liblab.entry.lua.LuaFunction;
import com.sqrt.liblab.io.DataSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by James on 29/09/2014.
 */
public class LuaChunkCodec extends EntryCodec<LuaChunk> {
    private String readTString(DataSource source) throws IOException {
        int size = source.getUShort();
        if(size == 0)
            return null;
        byte[] b = new byte[size];
        source.get(b, 0, size);
        for(int i = 0; i < b.length; i++)
            b[i] ^= 0xff;
        String s = new String(b);
        if(s.endsWith("\0"))
            s = s.substring(0, s.length() - 1);
        return s;
    }

    private ArrayList<LuaConstant> readConstants(DataSource source) throws IOException {
        int count = source.getUShort();
        ArrayList<LuaConstant> constants = new ArrayList<>(count);
        for(int i = 0; i < count; i++) {
            int type = source.getUByte();
            switch(type) {
                case 'N': // number, float - 4 bytes
                    constants.add(new LuaConstant<>(source.getFloatLE()));
                    break;
                case 'S': // string
                    constants.add(new LuaConstant<>(readTString(source)));
                    break;
                case 'F': // constant function, for some reason it uses null here :S (https://github.com/residualvm/residualvm/blob/master/engines/grim/lua/lundump.cpp#L112)
                    constants.add(new LuaConstant<LuaFunction>(null));
                    break;
                default:
                    System.err.println("Unknown LUA constant type: " + ((char) type));
                    break;
            }
        }
        return constants;
    }

    private ArrayList<LocalVarDefinition> readLocals(DataSource source) throws IOException {
        int count = source.getUShort();
        ArrayList<LocalVarDefinition> lvars = new ArrayList<>();
        for(int i = 0; i < count; i++)
            lvars.add(new LocalVarDefinition(source.getUShort(), readTString(source)));
        lvars.add(new LocalVarDefinition(-1, null));
        return lvars;
    }

    private void readFunctions(List<LuaConstant> constants, DataSource source) throws IOException {
        while(source.getUByte() == '#') {
            int idx = source.getUShort();
            LuaFunction func = readFunction(source);
            constants.set(idx, new LuaConstant<>(func));
        }
    }

    private LuaFunction readFunction(DataSource source) throws IOException {
        LuaFunction lf = new LuaFunction();
        lf.lineNo = source.getUShort();
        lf.source = readTString(source);

        int codeSize = source.getInt();
        lf.code = new byte[codeSize];
        source.get(lf.code);
        lf.constants = readConstants(source);
        lf.localVars = readLocals(source);
        readFunctions(lf.constants, source);

        return lf;
    }

    @Override
    protected LuaChunk _read(DataSource source) throws IOException {
        int magic = source.getInt();
        if(magic != 0x1B4C7561)
            throw new IOException("Invalid LUA file");
        int version = source.getUByte();
        if(version != 0x31)
            throw new IOException("Invalid LUA file version: " + Integer.toHexString(version));
        if(source.getUByte() != 4)
            throw new IOException("Invalid LUA file, float length isn't 4...");

        source.skip(4); // Todo: wtf are these?

        LuaFunction lf = readFunction(source);

        // Todo: decompile? haha, if it'll be anything like the java decompiler I wrote then fuck that

        return new LuaChunk(source.container, source.getName(), lf);
    }

    @Override
    public DataSource write(LuaChunk source) throws IOException {
        return null;
    }

    @Override
    public String[] getFileExtensions() {
        return new String[]{"lua"};
    }

    @Override
    public Class<LuaChunk> getEntryClass() {
        return LuaChunk.class;
    }
}