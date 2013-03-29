package com.sqrt.liblab.entry.audio;

import java.util.LinkedList;
import java.util.List;

public class Region {
    public int offset;
    public int length;
    public String comments;
    public List<Jump> jumps = new LinkedList<Jump>();
}