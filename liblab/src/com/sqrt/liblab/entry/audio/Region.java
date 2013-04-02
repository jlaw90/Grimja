package com.sqrt.liblab.entry.audio;

import java.util.LinkedList;
import java.util.List;

/**
 * A region within an iMUSE audio file
 */
public class Region {
    /**
     * The offset into the audio stream that this region starts at
     */
    public int offset;
    /**
     * The length of this region in bytes
     */
    public int length;
    /**
     * Comments from the developers about this region
     */
    public String comments;
    /**
     * Jumps to other regions
     */
    public List<Jump> jumps = new LinkedList<Jump>();
}