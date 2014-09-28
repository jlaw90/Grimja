/*
 * Created by JFormDesigner on Fri Mar 22 13:36:17 GMT 2013
 */

package com.sqrt4.grimedi.ui.component;

import com.sqrt.liblab.threed.Vector3f;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.ChangeListener;

/**
 * @author James Lawrence
 */
public class Vector3Editor extends JPanel {
    public Vector3Editor() {
        initComponents();
        final float inc = 0.01f;
        float min = -9999;
        float max = 9999;
        xSpinner.setModel(new SpinnerNumberModel(0, min, max, inc));
        ySpinner.setModel(new SpinnerNumberModel(0, min, max, inc));
        zSpinner.setModel(new SpinnerNumberModel(0, min, max, inc));
    }

    public void addChangeListener(ChangeListener cl) {
        xSpinner.addChangeListener(cl);
        ySpinner.addChangeListener(cl);
        zSpinner.addChangeListener(cl);
    }

    public void removeChangeListener(ChangeListener cl) {
        xSpinner.removeChangeListener(cl);
        ySpinner.removeChangeListener(cl);
        zSpinner.removeChangeListener(cl);
    }

    public Vector3f getValue() {
        return new Vector3f(getValue(xSpinner), getValue(ySpinner), getValue(zSpinner));
    }

    private float getValue(JSpinner spin) {
        Object val = spin.getValue();
        if(val == null)
            return 0f;
        return ((Number) val).floatValue();
    }

    public void setValue(Vector3f v) {
        xSpinner.setValue(v.x);
        ySpinner.setValue(v.y);
        zSpinner.setValue(v.z);
    }

    public void setEnabled(boolean b) {
        xSpinner.setEnabled(b);
        ySpinner.setEnabled(b);
        zSpinner.setEnabled(b);
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        label1 = new JLabel();
        xSpinner = new JSpinner();
        label2 = new JLabel();
        ySpinner = new JSpinner();
        label3 = new JLabel();
        zSpinner = new JSpinner();

        //======== this ========
        setLayout(new GridBagLayout());
        ((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0, 0, 0};
        ((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0, 0};
        ((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
        ((GridBagLayout)getLayout()).rowWeights = new double[] {1.0, 0.0, 0.0, 1.0E-4};

        //---- label1 ----
        label1.setText("X: ");
        label1.setHorizontalAlignment(SwingConstants.TRAILING);
        add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 0), 0, 0));
        add(xSpinner, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 0), 0, 0));

        //---- label2 ----
        label2.setText("Y: ");
        label2.setHorizontalAlignment(SwingConstants.TRAILING);
        add(label2, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 0), 0, 0));
        add(ySpinner, new GridBagConstraints(3, 0, 1, 1, 1.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 0), 0, 0));

        //---- label3 ----
        label3.setText("Z: ");
        label3.setHorizontalAlignment(SwingConstants.TRAILING);
        add(label3, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 0), 0, 0));
        add(zSpinner, new GridBagConstraints(5, 0, 1, 1, 1.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 0), 0, 0));
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JLabel label1;
    private JSpinner xSpinner;
    private JLabel label2;
    private JSpinner ySpinner;
    private JLabel label3;
    private JSpinner zSpinner;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}