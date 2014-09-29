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