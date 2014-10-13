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

/*
 * Created by JFormDesigner on Thu Mar 21 13:39:24 GMT 2013
 */

package com.sqrt4.grimedi.ui.component;

import com.sqrt.liblab.LabCollection;
import com.sqrt.liblab.LabFile;
import com.sqrt.liblab.codec.CodecMapper;
import com.sqrt.liblab.entry.model.ColorMap;
import com.sqrt.liblab.io.DataSource;
import com.sqrt4.grimedi.ui.MainWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.Vector;

public class ColorMapSelector extends JPanel {
    private LabCollection _last;

    public ColorMapSelector() {
        initComponents();
    }

    public void setLabFile(LabFile container) {
        if (container.container == _last)
            return;
        _last = container.container;
        Vector<DataSource> colorMaps = new Vector<DataSource>();
        colorMaps.addAll(container.container.findByType(ColorMap.class));
        colorMapSelector.setModel(new DefaultComboBoxModel(colorMaps));
    }

    public void addItemListener(ItemListener listener) {
        colorMapSelector.addItemListener(listener);
    }

    public void removeItemListener(ItemListener listener) {
        colorMapSelector.removeItemListener(listener);
    }

    public ColorMap getSelected() {
        DataSource edp = (DataSource) colorMapSelector.getSelectedItem();
        try {
            edp.position(0);
            return (ColorMap) CodecMapper.codecForProvider(edp).read(edp);
        } catch (IOException e) {
            MainWindow.getInstance().handleException(e);
            return null;
        }
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        colorMapSelector.setEnabled(enabled);
    }

    private void createUIComponents() {
        colorMapSelector = new JComboBox();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        createUIComponents();


        //======== this ========
        setLayout(new BorderLayout());
        add(colorMapSelector, BorderLayout.CENTER);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JComboBox colorMapSelector;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
