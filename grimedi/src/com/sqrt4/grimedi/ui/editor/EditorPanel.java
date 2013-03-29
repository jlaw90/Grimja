package com.sqrt4.grimedi.ui.editor;

import com.sqrt.liblab.LabEntry;
import com.sqrt4.grimedi.ui.MainWindow;

import javax.swing.*;

public abstract class EditorPanel<T extends LabEntry> extends JPanel {
    public static final ImageIcon defaultIcon = new ImageIcon(EditorPanel.class.getResource("/file.png"));
    protected T data;
    protected MainWindow window;

    public final void setData(T data) {
        this.data = data;
        onNewData();
    }

    public abstract void onNewData();

    public final void setWindow(MainWindow mw) {
        window = mw;
    }

    public void onHide() {

    }

    public void onShow() {

    }

    public ImageIcon getIcon() {
        return defaultIcon;
    }
}