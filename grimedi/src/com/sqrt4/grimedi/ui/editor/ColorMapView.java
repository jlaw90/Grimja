/*
 * Created by JFormDesigner on Tue Mar 19 17:49:41 GMT 2013
 */

package com.sqrt4.grimedi.ui.editor;

import com.sqrt.liblab.model.ColorMap;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListDataListener;

/**
 * @author James Lawrence
 */
public class ColorMapView extends EditorPanel<ColorMap> {
    public ColorMapView() {
        initComponents();
    }

    public void onNewData() {
        colorList.setModel(new ListModel() {
            public int getSize() {
                return data.colors.length;
            }

            public Object getElementAt(int index) {
                return data.colors[index];
            }

            public void addListDataListener(ListDataListener l) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public void removeListDataListener(ListDataListener l) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });
        colorList.setCellRenderer(new ListCellRenderer() {
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                int color = (Integer) value;
                JLabel label = new JLabel(String.format("#0%06x", color));
                label.setHorizontalTextPosition(SwingConstants.CENTER);
                int r = (color >> 16) & 0xff;
                int g = (color >> 8) & 0xff;
                int b = color & 0xff;
                int bb = (int) Math.sqrt(r * r + g * g + b * b);
                label.setOpaque(true);
                Color c = new Color(color);
                label.setBackground(c);
                Border border = BorderFactory.createLineBorder(isSelected ? list.getSelectionBackground().darker() : c.darker(), 2);
                label.setBorder(border);
                label.setForeground(bb < 127 ? Color.white : Color.black);
                JPanel container = new JPanel(new BorderLayout());
                container.add(label);
                container.setBorder(BorderFactory.createEmptyBorder());
                return container;
            }
        });
        colorList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        colorList.setVisibleRowCount(0);
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        splitPane1 = new JSplitPane();
        scrollPane1 = new JScrollPane();
        colorList = new JList();

        //======== this ========
        setLayout(new BorderLayout());

        //======== splitPane1 ========
        {
            splitPane1.setOrientation(JSplitPane.VERTICAL_SPLIT);
            splitPane1.setResizeWeight(0.8);

            //======== scrollPane1 ========
            {
                scrollPane1.setViewportView(colorList);
            }
            splitPane1.setTopComponent(scrollPane1);
        }
        add(splitPane1, BorderLayout.CENTER);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JSplitPane splitPane1;
    private JScrollPane scrollPane1;
    private JList colorList;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}