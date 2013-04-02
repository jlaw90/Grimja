package com.sqrt4.grimedi.util;

public class Size implements Comparable<Size> {
    private long size;

    public Size(long size) {
        this.size = size;
    }

    public String toString() {
        final String[] prefixes = {"B", "KB", "MB"};
        String res = size + "B";
        int size = 1;
        for (String prefixe : prefixes) {
            if (this.size >= size)
                res = (this.size / size) + prefixe;
            size *= 1024;
        }
        return res;
    }

    public int compareTo(Size o) {
        return Long.compare(size, o.size);
    }
}