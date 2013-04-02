/*
 * Created by JFormDesigner on Sat Mar 30 01:42:39 GMT 2013
 */

package com.sqrt4.grimedi.ui.editor;

import java.awt.*;
import javax.swing.*;

/**
 * @author James Lawrence
 */
public class TextView extends JPanel {
    public TextView() {
        initComponents();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        scrollPane1 = new JScrollPane();
        textPane = new JTextPane();

        //======== this ========
        setLayout(new BorderLayout());

        //======== scrollPane1 ========
        {
            scrollPane1.setViewportView(textPane);
        }
        add(scrollPane1, BorderLayout.CENTER);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JScrollPane scrollPane1;
    private JTextPane textPane;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
