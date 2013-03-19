package com.sqrt4.grimedi.ui.editor;

import com.sqrt.liblab.LabEntry;

import javax.swing.*;

public abstract class EditorPanel<T extends LabEntry> extends JPanel {
    protected T data;

    public final void setData(T data) {
        this.data = data;
        onNewData();
    }

    public abstract void onNewData();
}