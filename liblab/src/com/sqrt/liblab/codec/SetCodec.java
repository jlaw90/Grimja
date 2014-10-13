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

import com.sqrt.liblab.entry.graphics.GrimBitmap;
import com.sqrt.liblab.entry.model.ColorMap;
import com.sqrt.liblab.entry.model.set.Light;
import com.sqrt.liblab.entry.model.set.Sector;
import com.sqrt.liblab.entry.model.set.Set;
import com.sqrt.liblab.entry.model.set.Setup;
import com.sqrt.liblab.io.DataSource;
import com.sqrt.liblab.io.TextParser;
import com.sqrt.liblab.threed.Vector3f;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by James on 30/09/2014.
 */
public class SetCodec extends EntryCodec<Set>{
    @Override
    protected Set _read(DataSource source) throws IOException {
        String s = source.getString(7);
        source.position(0);
        if(s.equalsIgnoreCase("section"))
            return loadText(source);
        return loadBinary(source);
    }

    private Set loadBinary(DataSource source) {
        // This doesn't seem to be in my version of GRIM so I can't test or develop
        System.err.println("Todo: binary loading of SETS");
        return null;
    }

    private Set loadText(DataSource source) throws IOException {
        Set set = new Set(source.container, source.getName());
        TextParser t = new TextParser(source);
        t.expectString("section: colormaps");
        t.skipWhitespace();
        t.expectString("numcolormaps");
        int n = t.readInt();
        set.colorMaps = new ColorMap[n];
        for(int i = 0; i < n; i++) {
            t.skipWhitespace();
            t.expectString("colormap");
            String name = t.readString();
            set.colorMaps[i] = (ColorMap) source.container.container.findByName(name);
        }

        if(t.checkString("section: objectstates") || t.checkString("section: object_states")) {
            t.nextLine();
            t.skipWhitespace();
            t.expectString("tot_objects");
            n = t.readInt();
            for(int i = 0; i < n; i++) {
                t.skipWhitespace();
                t.expectString("object");
                String name = t.readString();
                System.out.println("Object: " + name);
            }
        }

        t.skipWhitespace();
        t.expectString("section: setups");
        t.skipWhitespace();
        t.expectString("numsetups");
        n = t.readInt();
        set.setups = new Setup[n];
        for(int i = 0; i < n; i++) {
            Setup s = set.setups[i] = new Setup();
            t.skipWhitespace();
            t.expectString("setup");
            s.name = t.readString();
            t.skipWhitespace();
            t.expectString("background");
            s.background = (GrimBitmap) source.container.container.findByName(t.readString());

            if(t.checkString("zbuffer")) {
                t.skipWhitespace();
                t.expectString("zbuffer");
                s.zBackground = (GrimBitmap) source.container.container.findByName(t.readString());
            }

            t.skipWhitespace();
            t.expectString("position");
            s.position = t.readVector3();
            t.skipWhitespace();
            t.expectString("interest");
            s.interest = t.readVector3();
            t.skipWhitespace();
            t.expectString("roll");
            s.roll = t.readAngle();
            t.skipWhitespace();
            t.expectString("fov");
            s.fov = t.readAngle();
            t.skipWhitespace();
            t.expectString("nclip");
            s.nclip = t.readFloat();
            t.skipWhitespace();
            t.expectString("fclip");
            s.fclip = t.readFloat();

            while(true) {
                if(!t.checkString("object_art"))
                    break;
                t.skipWhitespace();
                t.expectString("object_art");
                t.skipWhitespace();
                String objName = t.readString();
                String bm = t.readString();
                System.out.println("Obj: " + objName + ", " + bm);

                if(t.checkString("object_z")) {
                    t.skipWhitespace();
                    t.expectString("object_z");
                    String zObjName = t.readString();
                    String zBm = t.readString();
                    System.out.println("ZObj: " + zObjName + ", " + zBm);
                }
                // Todo: add object state (as in https://github.com/residualvm/residualvm/blob/e6aae9206f5bef6d7f49d3f28c2bb5e0886947b5/engines/grim/set.cpp#L364)
            }
        }

        if(!t.checkString("section: lights"))
            return set;

        t.skipWhitespace();
        t.expectString("section: lights");
        t.skipWhitespace();
        t.expectString("numlights");
        n = t.readInt();
        set.lights = new Light[n];
        for(int i = 0; i < n; i++) {
            Light l = set.lights[i] = new Light();
            t.skipWhitespace();
            t.expectString("light");
            if(!t.checkString("light"))
                l.name = "";
            else
                l.name = t.readString();
            t.skipWhitespace();
            t.expectString("type");
            l.type = t.readString();
            t.skipWhitespace();
            t.expectString("position");
            l.position = t.readVector3();
            t.skipWhitespace();
            t.expectString("direction");
            l.direction = t.readVector3();
            t.skipWhitespace();
            t.expectString("intensity");
            l.intensity = t.readFloat();
            t.skipWhitespace();
            t.expectString("umbraangle");
            l.umbra = t.readAngle();
            t.skipWhitespace();
            t.expectString("penumbraangle");
            l.penumbra = t.readAngle();
            t.skipWhitespace();
            t.expectString("color");
            Vector3f cv = t.readVector3();
            l.color = new Color((int) cv.x, (int) cv.y, (int) cv.z);
        }

        // Todo: sectors

        t.skipWhitespace();
        t.expectString("section: sectors");
        if(t.eof())
            return set;

        t.skipWhitespace();
        if(t.checkString("numsectors")) // inv_naut.set in DATA003.LAB seems to have this
            t.nextLine();

        List<Sector> sectors = new ArrayList<>();

        while(!t.eof()) {
            Sector sector = new Sector();
            sectors.add(sector);
            t.skipWhitespace();
            t.expectString("sector");
            if(!t.checkString("sector"))
                sector.name = "";
            else
                sector.name = t.readString();
            t.skipWhitespace();
            t.expectString("id");

            sector.id = t.readInt();
            t.skipWhitespace();
            t.expectString("type");
            sector.type = t.readString();
            t.skipWhitespace();
            t.expectString("default visibility");
            sector.defaultVisibility = t.readString().equals("visible");
            t.skipWhitespace();
            if(!t.checkString("numvertices")) { // inv_naut.set in DATA003.LAB seems to have this
                t.expectString("height");
                sector.height = t.readFloat();
            }
            t.skipWhitespace();
            t.expectString("numvertices");
            n = t.readInt();
            t.skipWhitespace();
            t.expectString("vertices:");
            t.skipWhitespace();
            Vector3f[] vertices = sector.vertices = new Vector3f[n+1];
            for(int i = 0; i < n; i++)
                vertices[i] = t.readVector3();
            // Residual repeats the last vertex so it loops
            vertices[n] = vertices[0];
            sector.normal = vertices[1].sub(vertices[0]).cross(vertices[n-1].sub(vertices[0]));
            float length = sector.normal.length();
            if(length > 0)
                sector.normal = sector.normal.div(length);

            if(t.checkString("numgates")) { // inv_naut.set in DATA003.LAB seems to have this
                t.skipWhitespace();
                t.expectString("numgates");
                n = t.readInt();
                t.expectString("gates:");
            }
        }
        set.sectors = sectors.toArray(new Sector[sectors.size()]);
        return set;
    }

    @Override
    public DataSource write(Set source) throws IOException {
        return null; // Todo:
    }

    @Override
    public String[] getFileExtensions() {
        return new String[] {"set"};
    }

    @Override
    public Class<Set> getEntryClass() {
        return Set.class;
    }
}