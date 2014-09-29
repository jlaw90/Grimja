/*
 * Copyright (C) 2014  James Lawrence.
 *
 *     This program is free software: you can redistribute it and/or modify
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

import java.beans.*;
import javax.swing.event.*;

import java.awt.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

/**
 * @author James Lawrence
 */
public class AngleEditor extends JPanel {
    private List<PropertyChangeListener> listeners = new LinkedList<PropertyChangeListener>();
    private boolean adjusting = false;

    public AngleEditor() {
        initComponents();
        angleDisplay.setFormatterFactory(new DefaultFormatterFactory(new NumberFormatter(NumberFormat.getNumberInstance())));
    }

    public AngleEditor(Angle angle) {
        setAngle(angle);
    }

    public void setAngle(Angle angle) {
        setAngle(angle.degrees);
    }

    public void setAngle(float degrees) {
        float old = getDegrees();
        adjusting = true;
        angleDisplay.setValue(degrees);
        angleslider.setValue((int) degrees);
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
        angleslider.setEnabled(enabled);
    }

    private void angleSliderChanged(ChangeEvent e) {
        if(adjusting)
            return;
        setAngle(new Angle(angleslider.getValue()));
    }

    private void angleDisplayChanged(PropertyChangeEvent e) {
        if(adjusting)
            return;
        if(angleDisplay == null || angleDisplay.getValue() == null)
            setAngle(Angle.zero);
        setAngle(getAngle());
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        listeners.add(listener);
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        angleslider = new JSlider();
        angleDisplay = new JFormattedTextField();

        //======== this ========
        setLayout(new BorderLayout());

        //---- angleslider ----
        angleslider.setMaximum(180);
        angleslider.setPaintTicks(true);
        angleslider.setMajorTickSpacing(90);
        angleslider.setValue(0);
        angleslider.setMinimum(-180);
        angleslider.setMinorTickSpacing(30);
        angleslider.setSnapToTicks(true);
        angleslider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                angleSliderChanged(e);
            }
        });
        add(angleslider, BorderLayout.CENTER);

        //---- angleDisplay ----
        angleDisplay.addPropertyChangeListener("value", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent e) {
                angleDisplayChanged(e);
            }
        });
        add(angleDisplay, BorderLayout.SOUTH);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JSlider angleslider;
    private JFormattedTextField angleDisplay;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
