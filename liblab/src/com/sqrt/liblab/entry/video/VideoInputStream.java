package com.sqrt.liblab.entry.video;

import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;

public abstract class VideoInputStream {
    public abstract void seek(int pos) throws IOException;

    public abstract void setFrame(int frame) throws IOException;

    public abstract boolean readFrame(WritableRaster raster, int width, int height) throws IOException;
}