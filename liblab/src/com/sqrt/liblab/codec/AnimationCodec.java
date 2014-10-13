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

import com.sqrt.liblab.entry.model.anim.Animation;
import com.sqrt.liblab.entry.model.anim.AnimationNode;
import com.sqrt.liblab.entry.model.anim.KeyFrame;
import com.sqrt.liblab.entry.model.anim.Marker;
import com.sqrt.liblab.io.DataSource;
import com.sqrt.liblab.io.TextParser;

import java.io.IOException;

public class AnimationCodec extends EntryCodec<Animation> {
    protected Animation _read(DataSource source) throws IOException {
        if (source.getIntLE() == (('K' << 24) | ('E' << 16) | ('Y' << 8) | 'F'))
            return loadBinary(source);
        else {
            source.position(0);
            return loadText(source);
        }
    }

    private Animation loadText(DataSource data) throws IOException {
        // Todo: this has some major bugs, not sure what the issue is yet
        TextParser source = new TextParser(data);
        source.expectString("section: header");
        Animation anim = new Animation(data.container, data.getName());
        anim.flags = source.expectString("flags").readHex();
        anim.type = source.expectString("type").readHex();
        anim.numFrames = source.expectString("frames").readInt();
        float fps = source.expectString("fps").readFloat();
        int numJoints = source.expectString("joints").readInt();
        if(source.currentLine().equalsIgnoreCase("section: markers")) {
            source.nextLine();
            int numMarkers = source.expectString("markers").readInt();
            for(int i = 0; i < numMarkers; i++) {
                Marker m = new Marker();
                anim.markers.add(m);
                m.frame = source.readFloat();
                m.val = source.readInt();
            }
        }
        source.expectString("section: keyframe nodes");
        int numNodes = source.expectString("nodes").readInt();
        // allocate
        for(int i = 0; i < numJoints; i++)
            anim.nodes.add(null);
        for(int i = 0; i < numNodes; i++) {
            int idx = source.expectString("node").readInt();
            AnimationNode an = new AnimationNode();
            anim.nodes.set(idx, an);
            source.expectString("mesh name ").skipWhitespace();
            an.meshName = source.read(source.remainingChars());
            int numEntries = source.expectString("entries").readInt();
            for(int j = 0; j < numEntries; j++)
                an.entries.add(null);
            for(int j = 0; j < numEntries; j++) {
                idx = source.readInt();
                KeyFrame kf = new KeyFrame();
                an.entries.set(idx, kf);
                source.expectString(":").skipWhitespace(); // Get to the data...
                kf.frame = source.readFloat();
                kf.flags = source.readHex();
                kf.pos = source.readVector3();
                kf.pitch = source.readAngle();
                kf.yaw = source.readAngle();
                kf.roll = source.readAngle();
                kf.dpos = source.readVector3();
                kf.dpitch = source.readAngle();
                kf.dyaw = source.readAngle();
                kf.droll = source.readAngle();
            }
        }
        return anim;
    }

    private Animation loadBinary(DataSource source) throws IOException {
        Animation anim = new Animation(source.container, source.getName());
        source.position(40);
        anim.flags = source.getIntLE();
        source.skip(4);
        anim.type = source.getIntLE();
        source.position(56);
        anim.numFrames = source.getIntLE();
        int numJoints = source.getIntLE();
        source.skip(4);
        int numMarkers = source.getIntLE();
        source.position(72);
        for(int i = 0; i < numMarkers; i++) {
            Marker m = new Marker();
            m.frame = source.getFloatLE();
            anim.markers.add(m);
        }
        source.position(104);
        for(int i = 0; i < numMarkers; i++)
           anim.markers.get(i).val = source.getIntLE();

        source.position(136);
         // allocate
        for(int i = 0; i < numJoints; i++)
            anim.nodes.add(null);
        for(int i = 0; i < numJoints; i++) {
            String meshName = source.getString(32);
            if(meshName.isEmpty())
                meshName = "(null)";
            int nodeNum = source.getIntLE();
            if(nodeNum >= numJoints) {
                System.err.println("Invalid node number (" + nodeNum + "/" + numJoints + ")");
                continue;
            }
            if(anim.nodes.get(nodeNum) != null) {
                source.skip(8);
                continue;
            }
            AnimationNode node = new AnimationNode();
            node.meshName = meshName;
            int count = source.getIntLE();
            source.skip(4);
            for(int j = 0; j < count; j++) {
                KeyFrame kfe = new KeyFrame();
                kfe.frame = source.getFloatLE();
                kfe.flags = source.getIntLE();
                kfe.pos = source.getVector3f();
                kfe.pitch = source.getAngle();
                kfe.yaw = source.getAngle();
                kfe.roll = source.getAngle();
                kfe.dpos = source.getVector3f();
                kfe.dpitch = source.getAngle();
                kfe.dyaw = source.getAngle();
                kfe.droll = source.getAngle();
                node.entries.add(kfe);
            }
            anim.nodes.set(nodeNum, node);
        }
        return anim;
    }

    public DataSource write(Animation source) throws IOException {
        throw new UnsupportedOperationException(); // Todo
    }

    public String[] getFileExtensions() {
        return new String[]{"key"};
    }

    public Class<Animation> getEntryClass() {
        return Animation.class;
    }
}