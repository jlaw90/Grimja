package com.sqrt.liblab.entry.audio;

import com.sqrt.liblab.entry.LabEntry;
import com.sqrt.liblab.LabFile;

import java.util.LinkedList;
import java.util.List;

/**
 * iMUSE audio data
 */
public class Audio extends LabEntry {
    /**
     * The sample rate of the data
     */
    public int sampleRate;
    /**
     * The number of channels (e.g. 1 for mono, 2 for stereo)
     */
    public int channels;
    /**
     * The number of bits per sample
     */
    public int bits;
    /**
     * The regions of this audio data
     */
    public final List<Region> regions = new LinkedList<Region>();
    /**
     * The audio stream we can use to drive a sound device
     */
    public AudioInputStream stream;

    public Audio(LabFile container, String name) {
        super(container, name);
    }

    public int bytesToTime(long l) {
        return (int) (l / (long) ((sampleRate * channels * (bits / 8)) / 1000));
    }
}