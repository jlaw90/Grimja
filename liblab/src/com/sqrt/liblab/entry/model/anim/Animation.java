package com.sqrt.liblab.entry.model.anim;

import com.sqrt.liblab.LabEntry;
import com.sqrt.liblab.LabFile;

import java.util.LinkedList;
import java.util.List;

public class Animation extends LabEntry {
    public int flags;
    public final List<Marker> markers = new LinkedList<Marker>();
    public final List<AnimationNode> nodes = new LinkedList<AnimationNode>();
    public int type;
    public int numFrames;

    public Animation(LabFile container, String name) {
        super(container, name);
    }
}