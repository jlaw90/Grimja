/*
 * Created by JFormDesigner on Fri Mar 15 22:45:17 GMT 2013
 */

package com.sqrt4.grimedi.ui.editor;

import com.sqrt.liblab.io.DataSource;
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
                int bytes = (int) (source.getLength() - idx);
                if (bytes <= 0)
                    return null;
                StringBuilder sb = new StringBuilder("<html>");
                sb.append(String.format("Offset: %06x<br/>", idx));
                try {
                    source.close();
                    source.seek(idx);
                    byte b = source.readByte();
                    sb.append(String.format("Byte: hex: %02x, s: %d, u: %d<br/>", b, b, b & 0xff));
                    if (bytes >= 2) {
                        source.seek(idx);
                        int s = source.readShort();
                        source.seek(idx);
                        int sle = source.readShortLE();
                        sb.append(String.format("Short (BE): hex: %04x, s: %d, u: %d<br/>", s, s, s & 0xffff));
                        sb.append(String.format("Short (LE): hex: %04x, s: %d, u: %d<br/>", sle, sle, sle & 0xffff));
                        if (bytes >= 4) {
                            source.seek(idx);
                            int i = source.readInt();
                            source.seek(idx);
                            int ile = source.readIntLE();
                            sb.append(String.format("Int (BE): hex: %08x, s: %d, u: %d<br/>", i, i, i & 0xffffffffL));
                            sb.append(String.format("Int (LE): hex: %08x, s: %d, u: %d<br/>", ile, ile, ile & 0xffffffffL));
                        }
                    }
                    source.seek(idx);
                    StringBuilder str = new StringBuilder();
                    int slen = 0;
                    boolean cleanString = false;
                    while (source.getRemaining() > 0 && slen < 32) {
                        byte o = source.readByte();
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
                    e.printStackTrace();
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
                return (int) ((source.getLength() + 15) / 16);
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
                    if (pos >= source.getLength())
                        return null;
                    source.seek(pos);
                    int b = source.read();
                    return ascii ? new String(new char[]{(char) b}) : String.format("%02x", b);
                } catch (IOException e) {
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
    private JScrollPane panel1;
    private JTable hexTable;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
