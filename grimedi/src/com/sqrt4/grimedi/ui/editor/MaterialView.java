/*
 * Created by JFormDesigner on Mon Mar 18 20:01:59 GMT 2013
 */

package com.sqrt4.grimedi.ui.editor;

import java.awt.event.*;
import com.sqrt.liblab.entry.model.Material;
import com.sqrt.liblab.entry.model.Texture;
import com.sqrt4.grimedi.ui.component.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;

/**
 * @author James Lawrence
 */
public class MaterialView extends EditorPanel<Material> {
    public MaterialView() {
        initComponents();
    }

    private void updatePreview() {
        preview.setIcon(null);
        if (imageList.getSelectedValue() == null || colorMapSelector.getSelected() == null)
            return;
        preview.setIcon(new ImageIcon(((Texture) imageList.getSelectedValue()).render(colorMapSelector.getSelected())));
    }

    private void imageSelected(ListSelectionEvent e) {
        updatePreview();
    }

    private void createUIComponents() {
        colorMapSelector = new ColorMapSelector();
    }

    private void colorMapSelected(ItemEvent e) {
        ListCellRenderer rend = imageList.getCellRenderer();
        if (!(rend instanceof OurCellRenderer))
            return;
        ((OurCellRenderer) rend).cache.clear();
        updatePreview();
        imageList.repaint();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        createUIComponents();

        panel3 = new JSplitPane();
        panel4 = new JPanel();
        panel1 = new JScrollPane();
        preview = new JLabel();
        panel2 = new JPanel();
        scrollPane1 = new JScrollPane();
        imageList = new JList();

        //======== this ========
        setLayout(new BorderLayout());

        //======== panel3 ========
        {
            panel3.setOrientation(JSplitPane.VERTICAL_SPLIT);

            //======== panel4 ========
            {
                panel4.setLayout(new BorderLayout());

                //======== panel1 ========
                {

                    //---- preview ----
                    preview.setHorizontalAlignment(SwingConstants.CENTER);
                    panel1.setViewportView(preview);
                }
                panel4.add(panel1, BorderLayout.CENTER);

                //---- colorMapSelector ----
                colorMapSelector.addItemListener(new ItemListener() {
                    @Override
                    public void itemStateChanged(ItemEvent e) {
                        colorMapSelected(e);
                    }
                });
                panel4.add(colorMapSelector, BorderLayout.SOUTH);
            }
            panel3.setTopComponent(panel4);

            //======== panel2 ========
            {
                panel2.setLayout(new BorderLayout());

                //======== scrollPane1 ========
                {

                    //---- imageList ----
                    imageList.setVisibleRowCount(0);
                    imageList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
                    imageList.addListSelectionListener(new ListSelectionListener() {
                        @Override
                        public void valueChanged(ListSelectionEvent e) {
                            imageSelected(e);
                        }
                    });
                    scrollPane1.setViewportView(imageList);
                }
                panel2.add(scrollPane1, BorderLayout.CENTER);
            }
            panel3.setBottomComponent(panel2);
        }
        add(panel3, BorderLayout.CENTER);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    public void onNewData() {
        colorMapSelector.setLabFile(data.container);
        imageList.setModel(new ListModel() {
            public int getSize() {
                return data.textures.size();
            }

            public Object getElementAt(int index) {
                return data.textures.get(index);
            }

            public void addListDataListener(ListDataListener l) {
            }

            public void removeListDataListener(ListDataListener l) {
            }
        });
        imageList.setCellRenderer(new OurCellRenderer());
        imageList.setSelectedIndex(0);
        updatePreview();
    }

    private class OurCellRenderer implements ListCellRenderer {
        private Map<BufferedImage, JLabel> cache = new HashMap<BufferedImage, JLabel>();

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Texture t = (Texture) value;
            JLabel label;
            if (cache.containsKey(t))
                label = cache.get(t);
            else {
                BufferedImage bi = t.render(colorMapSelector.getSelected());
                if(bi == null)
                    return new JLabel("Invalid colormodel");
                label = new JLabel();
                final int size = 128;
                BufferedImage scale = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                float mult;
                mult = (float) bi.getWidth() / (float) size;
                mult = Math.max((float) bi.getHeight() / (float) size, mult);
                int width = (int) (bi.getWidth() / mult);
                int height = (int) (bi.getHeight() / mult);
                int x = (size - width) / 2;
                int y = (size - height) / 2;
                scale.getGraphics().drawImage(bi, x, y, width, height, null);
                label.setIcon(new ImageIcon(scale));
                cache.put(bi, label);
                label.setOpaque(true);
            }

            if (isSelected) {
                label.setForeground(list.getSelectionForeground());
                label.setBackground(list.getSelectionBackground());
                label.setBorder(BorderFactory.createLineBorder(list.getSelectionBackground().darker()));
            } else {
                label.setForeground(list.getForeground());
                label.setBackground(list.getBackground());
                label.setBorder(BorderFactory.createLineBorder(list.getBackground().darker()));
            }
            return label;
        }
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JSplitPane panel3;
    private JPanel panel4;
    private JScrollPane panel1;
    private JLabel preview;
    private ColorMapSelector colorMapSelector;
    private JPanel panel2;
    private JScrollPane scrollPane1;
    private JList imageList;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
