package com.sqrt.liblab.entry.audio;

import java.io.IOException;
import java.io.InputStream;

/**
 * Simply an InputStream that allows us to seek
 */
public abstract class AudioInputStream extends InputStream {
    /**
     * Seeks to the specified position
     * @param pos the position to seek to
     * @throws IOException
     */
    public abstract void seek(int pos) throws IOException;
}