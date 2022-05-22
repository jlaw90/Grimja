

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
 * Created by JFormDesigner on Tue Mar 19 17:49:41 GMT 2013
 */

package com.sqrt4.grimedi.ui.editor;

import com.sqrt.liblab.codec.CodecMapper;
import com.sqrt.liblab.codec.ColorMapCodec;
import com.sqrt.liblab.entry.model.ColorMap;
import com.sqrt4.grimedi.ui.MainWindow;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListDataListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

/**
 * @author James Lawrence
 */
public class ColorMapView extends EditorPanel<ColorMap> {
    public ColorMapView() {
        initComponents();
    }

    ImageIcon icon = new ImageIcon(getClass().getResource("/palette.png"));
    public ImageIcon getIcon() {
        return icon;
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
                JLabel label = new JLabel(String.format("#%06x", color));
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
        // Generated using JFormDesigner non-commercial license
        scrollPane1 = new JScrollPane();
        colorList = new JList();
        panel1 = new JPanel();
        button1 = new JButton();
        button2 = new JButton();
        exportACTAction = new ExportACTAction();
        importACTAction = new ImportACTAction();

        //======== this ========
        setLayout(new BorderLayout());

        //======== scrollPane1 ========
        {

            //---- colorList ----
            colorList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
            colorList.setVisibleRowCount(0);
            scrollPane1.setViewportView(colorList);
        }
        add(scrollPane1, BorderLayout.CENTER);

        //======== panel1 ========
        {
            panel1.setLayout(new FlowLayout());

            //---- button1 ----
            button1.setAction(exportACTAction);
            panel1.add(button1);

            //---- button2 ----
            button2.setAction(importACTAction);
            panel1.add(button2);
        }
        add(panel1, BorderLayout.SOUTH);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JScrollPane scrollPane1;
    private JList colorList;
    private JPanel panel1;
    private JButton button1;
    private JButton button2;
    private ExportACTAction exportACTAction;
    private ImportACTAction importACTAction;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    private class ExportACTAction extends AbstractAction {
        private ExportACTAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "Export .ACT file");
            putValue(SHORT_DESCRIPTION, "Export to Adobe Color Table");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            JFileChooser jfc = MainWindow.getInstance().createFileDialog();
            jfc.setFileFilter(new FileNameExtensionFilter("Adobe Color Table (*.act)", "act"));

            String name = data.getName();
            int idx = name.lastIndexOf('.');
            if(idx != -1)
                name = name.substring(0, idx);
            name += ".act";
            jfc.setSelectedFile(new File(name));

            if(jfc.showSaveDialog(MainWindow.getInstance()) != JFileChooser.APPROVE_OPTION)
                return;

            File f = jfc.getSelectedFile();
            name = f.getName();
            if(!name.toLowerCase().endsWith(".act"))
                name += ".act";
            f = new File(f.getParentFile(), name);
            try {
                ((ColorMapCodec) CodecMapper.codecForClass(ColorMap.class)).writeACT(data, f);
            } catch (IOException e1) {
                MainWindow.getInstance().handleException(e1);
            }
        }
    }

    private class ImportACTAction extends AbstractAction {
        private ImportACTAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "Import ACT");
            putValue(SHORT_DESCRIPTION, "Import Adobe Color Table");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            JFileChooser jfc = MainWindow.getInstance().createFileDialog();
            jfc.setFileFilter(new FileNameExtensionFilter("Adobe Color Table (*.act)", "act"));

            if(jfc.showOpenDialog(MainWindow.getInstance()) != JFileChooser.APPROVE_OPTION)
                return;

            try {
                ((ColorMapCodec) CodecMapper.codecForClass(ColorMap.class)).readACTFor(data, jfc.getSelectedFile());
                setData(data); // Refresh view (nasty)
            } catch (IOException e1) {
                MainWindow.getInstance().handleException(e1);
            }
        }
    }
}
