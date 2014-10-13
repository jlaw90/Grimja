

/*
 * Copyright (C) 2014  James Lawrence.
 *
 *     This file is part of GrimEdi.
 *
 *     GrimEdi is free software: you can redistribute it and/or modify
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

package com.sqrt4.grimedi.ui.editor;

import com.sqrt.liblab.entry.LabEntry;
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

    public T getData() {
        return this.data;
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