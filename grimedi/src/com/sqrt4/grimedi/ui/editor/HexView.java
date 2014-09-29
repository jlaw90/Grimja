

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
 * Created by JFormDesigner on Fri Mar 15 22:45:17 GMT 2013
 */

package com.sqrt4.grimedi.ui.editor;

import com.sqrt.liblab.io.DataSource;
import com.sqrt4.grimedi.ui.MainWindow;
import sun.swing.SwingUtilities2;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.IOException;

/**
 * @author James Lawrence
 */
public class HexView extends JPanel {
    private Font fixedWidth = new Font("Monospaced", Font.PLAIN, 12);
    private DataSource source;

    private HexView() {
        initComponents();
    }

    public HexView(DataSource source) {
        this();
        this.source = source;
        //hexTable.revalidate();
        //JTableHeader rowHeader = new JTableHeader(hexTable.getColumnModel());
        //rowHeader.setTable(hexTable);
        //panel1.setRowHeaderView(rowHeader);
    }

    private void createUIComponents() {
        hexTable = new JTable() {
            public String getToolTipText(MouseEvent event) {
                Point p = event.getPoint();

                // Locate the renderer under the event location
                int col = columnAtPoint(p);
                if (col == 16)
                    return null;
                if (col > 16)
                    col -= 17;
                int row = rowAtPoint(p);
                if (col == -1 || row == -1)
                    return null;
                int idx = row * 16 + col;
                int bytes = (int) (source.length() - idx);
                if (bytes <= 0)
                    return null;
                StringBuilder sb = new StringBuilder("<html>");
                sb.append(String.format("Offset: %06x<br/>", idx));
                try {
                    source.position(idx);
                    byte b = source.get();
                    sb.append(String.format("Byte: hex: %02x, s: %d, u: %d<br/>", b, b, b & 0xff));
                    if (bytes >= 2) {
                        source.position(idx);
                        int s = source.getShort();
                        source.position(idx);
                        int sle = source.getShortLE();
                        sb.append(String.format("Short (BE): hex: %04x, s: %d, u: %d<br/>", s, s, s & 0xffff));
                        sb.append(String.format("Short (LE): hex: %04x, s: %d, u: %d<br/>", sle, sle, sle & 0xffff));
                        if (bytes >= 4) {
                            source.position(idx);
                            int i = source.getInt();
                            source.position(idx);
                            int ile = source.getIntLE();
                            sb.append(String.format("Int (BE): hex: %08x, s: %d, u: %d<br/>", i, i, i & 0xffffffffL));
                            sb.append(String.format("Int (LE): hex: %08x, s: %d, u: %d<br/>", ile, ile, ile & 0xffffffffL));
                        }
                    }
                    source.position(idx);
                    StringBuilder str = new StringBuilder();
                    int slen = 0;
                    boolean cleanString = false;
                    while (source.remaining() > 0 && slen < 32) {
                        byte o = source.get();
                        if (o == 0) {
                            cleanString = true;
                            break;
                        }
                        slen++;
                        str.append((char) o);
                    }
                    String string = str.toString();
                    if (string.length() > 0) {
                        string = string.replace("<", "&lt;").replace(">", "&gt;");
                        if (slen == 32)
                            string += "...";
                        sb.append("String: ").append(string);
                    }
                } catch (IOException e) {
                    MainWindow.getInstance().handleException(e);
                }
                sb.append("</html>");
                return sb.toString();
            }
        };
        hexTable.setRowSelectionAllowed(false);
        hexTable.setColumnSelectionAllowed(false);
        hexTable.setCellSelectionEnabled(true);
        hexTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION); // cbf'd with the hassle of selecting the same ascii as selected hex etc. TODO
        ListSelectionListener listener = new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting())
                    return;
                ListSelectionModel colSelModel = hexTable.getColumnModel().getSelectionModel();
                int lead = colSelModel.getLeadSelectionIndex();
                int anchor = colSelModel.getAnchorSelectionIndex();
                for (int col : hexTable.getSelectedColumns()) {
                    for (int row : hexTable.getSelectedRows()) {
                        if (col == 16)
                            continue;
                        if (col > 16)
                            col -= 17;
                        int idx = (row * 16) + col;
                        int linkCol = col + 17;
                        if (!colSelModel.isSelectedIndex(col))
                            colSelModel.addSelectionInterval(col, col);
                        if (!colSelModel.isSelectedIndex(linkCol))
                            colSelModel.addSelectionInterval(linkCol, linkCol);
                    }
                }
                if (colSelModel.getAnchorSelectionIndex() != anchor || colSelModel.getLeadSelectionIndex() != lead)
                    SwingUtilities2.setLeadAnchorWithoutSelection(colSelModel, anchor, lead);
            }
        };
        hexTable.getSelectionModel().addListSelectionListener(listener);
        hexTable.getColumnModel().getSelectionModel().addListSelectionListener(listener);
        hexTable.getTableHeader().setFont(fixedWidth);
        hexTable.setFont(fixedWidth);
        hexTable.setRowMargin(0);
        hexTable.setRowHeight(hexTable.getFontMetrics(fixedWidth).getHeight());
        hexTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int hexWidth = hexTable.getFontMetrics(fixedWidth).stringWidth("000");
        int asciiWidth = hexTable.getFontMetrics(fixedWidth).stringWidth("00");

        hexTable.setModel(new TableModel() {
            public int getRowCount() {
                return (int) ((source.length() + 15) / 16);
            }

            public int getColumnCount() {
                return 33;
            }

            public String getColumnName(int columnIndex) {
                return columnIndex < 16 ? String.format("%02x", columnIndex) : null;
            }

            public Class<?> getColumnClass(int columnIndex) {
                return String.class;
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }

            public Object getValueAt(int rowIndex, int columnIndex) {
                if (columnIndex == 16)
                    return null;
                try {
                    boolean ascii = columnIndex > 16;
                    if (ascii)
                        columnIndex -= 17;
                    int pos = rowIndex * 16 + columnIndex;
                    if (pos >= source.length())
                        return null;
                    source.position(pos);
                    int b = source.getUByte();
                    return ascii ? new String(new char[]{(char) b}) : String.format("%02x", b);
                } catch (IOException e) {
                    MainWindow.getInstance().handleException(e);
                    return "xx";
                }
            }

            public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
                throw new UnsupportedOperationException();
            }

            public void addTableModelListener(TableModelListener l) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public void removeTableModelListener(TableModelListener l) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });

        for (int i = 0; i < 33; i++) {
            hexTable.getColumnModel().getColumn(i).setPreferredWidth(i <= 16 ? hexWidth : asciiWidth);
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        createUIComponents();

        panel1 = new JScrollPane();

        //======== this ========
        setLayout(new BorderLayout());

        //======== panel1 ========
        {
            panel1.setViewportView(hexTable);
        }
        add(panel1, BorderLayout.CENTER);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JScrollPane panel1;
    private JTable hexTable;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
