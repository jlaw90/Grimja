/*
 * Created by JFormDesigner on Thu Mar 21 13:39:24 GMT 2013
 */

package com.sqrt4.grimedi.ui.component;

import com.sqrt.liblab.EntryDataProvider;
import com.sqrt.liblab.LabFile;
import com.sqrt.liblab.codec.CodecMapper;
import com.sqrt.liblab.entry.model.ColorMap;

import java.awt.*;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;
import javax.swing.*;

public class ColorMapSelector extends JPanel {
    private LabFile _last;

    public ColorMapSelector() {
        initComponents();
    }

    public void setLabFile(LabFile container) {
        if (container == _last)
            return;
        _last = container;
        Vector<EntryDataProvider> colorMaps = new Vector<EntryDataProvider>();
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
        EntryDataProvider edp = (EntryDataProvider) colorMapSelector.getSelectedItem();
        try {
            return (ColorMap) CodecMapper.codecForProvider(edp).read(edp);
        } catch (IOException e) {
            e.printStackTrace();
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
