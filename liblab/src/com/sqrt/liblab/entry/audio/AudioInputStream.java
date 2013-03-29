package com.sqrt.liblab.entry.audio;

import java.io.IOException;
import java.io.InputStream;

public abstract class AudioInputStream extends InputStream {
    public abstract void seek(int pos) throws IOException;
}