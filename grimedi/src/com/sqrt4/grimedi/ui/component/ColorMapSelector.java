/*
 * Created by JFormDesigner on Thu Mar 21 13:39:24 GMT 2013
 */

package com.sqrt4.grimedi.ui.component;

import com.sqrt.liblab.EntryDataProvider;
import com.sqrt.liblab.LabFile;
import com.sqrt.liblab.codec.CodecMapper;
import com.sqrt.liblab.codec.EntryCodec;
import com.sqrt.liblab.model.ColorMap;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import javax.swing.*;
import javax.swing.event.*;

public class ColorMapSelector extends JPanel {
    private LabFile _last;

    public ColorMapSelector() {
        initComponents();
    }

    public ColorMapSelector(LabFile container) {
        this();
        setLabFile(container);
    }

    public void setLabFile(LabFile container) {
        if(container == _last)
            return;
        _last = container;
        final java.util.List<EntryDataProvider> colorMaps = new ArrayList<EntryDataProvider>();
        for (EntryDataProvider prov : container.entries) {
            EntryCodec<?> codec = CodecMapper.codecForProvider(prov);
            if (codec == null || codec.getEntryClass() != ColorMap.class)
                continue;
            colorMaps.add(prov);
        }
        Collections.sort(colorMaps, new Comparator<EntryDataProvider>() {
            public int compare(EntryDataProvider o1, EntryDataProvider o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        colorMapSelector.setModel(new SpinnerListModel(colorMaps) {
            public void setValue(Object elt) {
                if(elt instanceof String) {
                    for(EntryDataProvider cm: colorMaps) {
                        if(cm.getName().equals(elt)) {
                            super.setValue(cm);
                            return;
                        }
                    }
                    throw new IllegalArgumentException();
                }
                super.setValue(elt);
            }
        });
    }

    public void addChangeListener(ChangeListener listener) {
        colorMapSelector.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        colorMapSelector.removeChangeListener(listener);
    }

    public ColorMap getSelected() {
        EntryDataProvider edp = (EntryDataProvider) colorMapSelector.getValue();
        try {
            return (ColorMap) CodecMapper.codecForProvider(edp).read(edp);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void createUIComponents() {
        colorMapSelector = new JSpinner();
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
    private JSpinner colorMapSelector;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
