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
 * Created by JFormDesigner on Fri Mar 22 11:21:22 GMT 2013
 */

package com.sqrt4.grimedi.ui.component;

import com.sqrt.liblab.threed.Angle;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;

/**
 * @author James Lawrence
 */
public class AngleEditor extends JPanel {
    private List<PropertyChangeListener> listeners = new LinkedList<PropertyChangeListener>();
    private boolean adjusting = false;

    public AngleEditor() {
        initComponents();
    }

    public void setAngle(float degrees) {
        float old = getDegrees();
        adjusting = true;
        angleDisplay.setValue(degrees);
        angleChooser.setAngle(degrees);
        adjusting = false;

        if(listeners.isEmpty())
            return;
        PropertyChangeEvent pce = new PropertyChangeEvent(this, "angle", old, degrees);
        for(int i = 0; i < listeners.size(); i++)
            listeners.get(i).propertyChange(pce);
    }

    public float getDegrees() {
        if(angleDisplay == null || angleDisplay.getValue() == null)
            return 0;
        return ((Number) angleDisplay.getValue()).floatValue(); // more precise...
    }

    public Angle getAngle() {
        return new Angle(getDegrees());
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        angleDisplay.setEnabled(enabled);
        angleChooser.setEnabled(enabled);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        listeners.add(listener);
    }

    private void angleChooserStateChanged(ChangeEvent e) {
        if(adjusting)
            return;
        setAngle(angleChooser.getAngle());
    }

    private void angleDisplayStateChanged(ChangeEvent e) {
        if(adjusting)
            return;
        setAngle(getDegrees());
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        angleChooser = new RadialAngleChooser();
        angleDisplay = new JSpinner();

        //======== this ========
        setLayout(new BorderLayout());

        //---- angleChooser ----
        angleChooser.setPreferredSize(new Dimension(32, 32));
        angleChooser.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                angleChooserStateChanged(e);
            }
        });
        add(angleChooser, BorderLayout.CENTER);

        //---- angleDisplay ----
        angleDisplay.setModel(new SpinnerNumberModel(0.0, -180.0, 180.0, 1.0));
        angleDisplay.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                angleDisplayStateChanged(e);
            }
        });
        add(angleDisplay, BorderLayout.SOUTH);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private RadialAngleChooser angleChooser;
    private JSpinner angleDisplay;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
