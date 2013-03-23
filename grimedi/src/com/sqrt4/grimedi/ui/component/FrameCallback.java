package com.sqrt4.grimedi.ui.component;

import javax.media.opengl.GL2;

public interface FrameCallback {
    public void preDisplay(GL2 gl2);

    public void postDisplay(GL2 gl2);
}