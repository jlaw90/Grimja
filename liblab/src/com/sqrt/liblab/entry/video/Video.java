package com.sqrt.liblab.entry.video;

import com.sqrt.liblab.LabEntry;
import com.sqrt.liblab.LabFile;

public class Video extends LabEntry {
    public int format;
    public final AudioTrack audio = new AudioTrack();
    public VideoInputStream stream;
    public int width, height;
    public float fps;

    public Video(LabFile container, String name) {
        super(container, name);
    }
}