/*
 * Created by JFormDesigner on Sat Mar 23 12:27:44 GMT 2013
 */

package com.sqrt4.grimedi.ui.component;

import java.awt.*;
import javax.swing.*;

/**
 * @author James Lawrence
 */
public class BusyDialog extends JDialog {
    public BusyDialog(Frame owner) {
        super(owner);
        initComponents();
    }

    public BusyDialog(Dialog owner) {
        super(owner);
        initComponents();
    }

    public void setMessage(String msg) {
        label1.setText(msg);
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        label1 = new JLabel();
        progressBar1 = new JProgressBar();

        //======== this ========
        setTitle("Please Wait");
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setResizable(false);
        setType(Window.Type.POPUP);
        setModal(true);
        Container contentPane = getContentPane();
        contentPane.setLayout(new GridBagLayout());
        ((GridBagLayout)contentPane.getLayout()).columnWidths = new int[] {0, 0};
        ((GridBagLayout)contentPane.getLayout()).rowHeights = new int[] {0, 0, 0};
        ((GridBagLayout)contentPane.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
        ((GridBagLayout)contentPane.getLayout()).rowWeights = new double[] {0.0, 1.0, 1.0E-4};

        //---- label1 ----
        label1.setText("text");
        label1.setHorizontalAlignment(SwingConstants.CENTER);
        contentPane.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(5, 5, 5, 5), 0, 0));

        //---- progressBar1 ----
        progressBar1.setIndeterminate(true);
        contentPane.add(progressBar1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(5, 5, 5, 5), 0, 0));
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JLabel label1;
    private JProgressBar progressBar1;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
