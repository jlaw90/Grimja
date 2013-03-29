package com.sqrt.liblab.codec;

import com.sqrt.liblab.io.DataSource;
import com.sqrt.liblab.entry.model.anim.Animation;
import com.sqrt.liblab.entry.model.anim.AnimationNode;
import com.sqrt.liblab.entry.model.anim.KeyFrame;
import com.sqrt.liblab.entry.model.anim.Marker;

import java.io.IOException;

public class KeyFrameCodec extends EntryCodec<Animation> {
    protected Animation _read(DataSource source) throws IOException {
        if (source.readIntLE() == (('K' << 24) | ('E' << 16) | ('Y' << 8) | 'F'))
            return loadBinary(source);
        else {
            System.err.println("Invalid keyframe format"); // Todo: text format
            return null;
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private Animation loadBinary(DataSource source) throws IOException {
        Animation kf = new Animation(source.container, source.getName());
        source.seek(40);
        kf.flags = source.readIntLE();
        source.skip(4);
        kf.type = source.readIntLE();
        source.seek(56);
        kf.numFrames = source.readIntLE();
        int numJoints = source.readIntLE();
        source.skip(4);
        int numMarkers = source.readIntLE();
        source.seek(72);
        for(int i = 0; i < numMarkers; i++) {
            Marker m = new Marker();
            m.frame = source.readFloatLE();
            kf.markers.add(m);
        }
        source.seek(104);
        for(int i = 0; i < numMarkers; i++)
           kf.markers.get(i).val = source.readIntLE();

        source.seek(136);
         // allocate
        for(int i = 0; i < numJoints; i++)
            kf.nodes.add(null);
        for(int i = 0; i < numJoints; i++) {
            String meshName = source.readString(32);
            if(meshName.isEmpty())
                meshName = "(null)";
            int nodeNum = source.readIntLE();
            if(nodeNum >= numJoints) {
                System.err.println("Invalid node number (" + nodeNum + "/" + numJoints + ")");
                continue;
            }
            if(kf.nodes.get(nodeNum) != null) {
                source.skip(8);
                continue;
            }
            AnimationNode node = new AnimationNode();
            node.meshName = meshName;
            int count = source.readIntLE();
            source.skip(4);
            for(int j = 0; j < count; j++) {
                KeyFrame kfe = new KeyFrame();
                kfe.frame = source.readFloatLE();
                kfe.flags = source.readIntLE();
                kfe.pos = source.readVector3();
                kfe.pitch = source.readAngle();
                kfe.yaw = source.readAngle();
                kfe.roll = source.readAngle();
                kfe.dpos = source.readVector3();
                kfe.dpitch = source.readAngle();
                kfe.dyaw = source.readAngle();
                kfe.droll = source.readAngle();
                node.entries.add(kfe);
            }
            kf.nodes.set(nodeNum, node);
        }
        return kf;
    }

    public DataSource write(Animation source) throws IOException {
        throw new UnsupportedOperationException();
    }

    public String[] getFileExtensions() {
        return new String[]{"key"};
    }

    public Class<Animation> getEntryClass() {
        return Animation.class;
    }
}