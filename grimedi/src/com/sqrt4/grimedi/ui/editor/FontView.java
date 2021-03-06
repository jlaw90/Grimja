

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
 * Created by JFormDesigner on Sat Mar 16 10:40:39 GMT 2013
 */

package com.sqrt4.grimedi.ui.editor;

import com.sqrt.liblab.entry.graphics.FontGlyph;
import com.sqrt.liblab.entry.graphics.GrimFont;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

/**
 * @author James Lawrence
 */
public class FontView extends EditorPanel<GrimFont> {
    private String _previewedText;
    private static final int mult = 8;
    private BufferedImage _glyphPreview;

    public FontView() {
        initComponents();
    }

    ImageIcon icon = new ImageIcon(getClass().getResource("/font.png"));
    public ImageIcon getIcon() {
        return icon;
    }

    private void updatePreview() {
        int height = data.height;
        int width = 0;
        int y = data.yOffset;
        int x = 0;
        String text = previewText.getText();
        char[] charArray = text.toCharArray();
        for (char c : charArray) {
            FontGlyph g = data.getGlyph(c);
            if (g == null)
                g = data.getGlyph('?');
            width += g.charWidth;
        }
        // Todo: move all this stuff into the font model
        if(width == 0)
            width = 1; // so bufferedimage doesn't bitch...
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (char c : charArray) {
            FontGlyph g = data.getGlyph(c);
            if (g == null)
                g = data.getGlyph('?');
            result.getGraphics().drawImage(g.mask, x+g.xOff, y+g.yOff, null);
            x += g.charWidth;
        }
        fontPreview.setIcon(new ImageIcon(result));
    }

    public void onNewData() {
        int height = data.height;
        int width = 0;
        for(FontGlyph glyph: data.glyphs)
        if(glyph.mask.getWidth() > width)
            width = glyph.mask.getWidth();
        _glyphPreview = new BufferedImage(width*mult, height*mult, BufferedImage.TYPE_INT_ARGB);

        list1.setModel(new ListModel<FontGlyph>() {
            public int getSize() {
                return data.glyphs.size();
            }

            public FontGlyph getElementAt(int index) {
                return data.glyphs.get(index);
            }

            public void addListDataListener(ListDataListener l) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public void removeListDataListener(ListDataListener l) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });

        list1.setCellRenderer(new ListCellRenderer<FontGlyph>() {
            private Map<FontGlyph, JLabel> cache = new HashMap<FontGlyph, JLabel>();

            public Component getListCellRendererComponent(JList list, FontGlyph value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label;
                if(cache.containsKey(value))
                    label = cache.get(value);
                else {
                label = new JLabel();
                    label.setOpaque(true);
                    label.setHorizontalAlignment(SwingConstants.CENTER);
                    label.setHorizontalTextPosition(SwingConstants.CENTER);
                    label.setVerticalAlignment(SwingConstants.BOTTOM);
                    label.setVerticalTextPosition(SwingConstants.BOTTOM);
                    label.setIcon(new ImageIcon(value.mask));
                    cache.put(value, label);
                }
                if (isSelected) {
                    label.setBackground(list.getSelectionBackground());
                    label.setForeground(list.getSelectionForeground());
                } else {
                    label.setBackground(list.getBackground());
                    label.setForeground(list.getForeground());
                }
                label.setBorder(BorderFactory.createLineBorder(isSelected? list.getSelectionBackground().darker(): list.getBackground().darker()));
                label.setText(String.valueOf(value.index));
                return label;
            }
        });
        list1.setVisibleRowCount(0);
        list1.setSelectedIndex(0);

        updatePreview();
    }

    private void previewTextChanged(CaretEvent e) {
        if(!previewText.getText().equals(_previewedText)) {
            updatePreview();
            _previewedText = previewText.getText();
        }
    }

    private void updateGlyphPreview() {
        FontGlyph glyph = (FontGlyph) list1.getSelectedValue();
        if(glyph == null) {
            glyphPreview.setIcon(null);
            return;
        }
        int w = glyph.charWidth;
        int h = data.height;
        int x = glyph.xOff;
        int y = data.yOffset+glyph.yOff;

        Graphics g = _glyphPreview.getGraphics();
        ((Graphics2D)g).setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, _glyphPreview.getWidth(), _glyphPreview.getHeight());
        ((Graphics2D)g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
        g.drawImage(glyph.mask, x*mult, y*mult, glyph.mask.getWidth()*mult, glyph.mask.getHeight()*mult, null);
        g.setColor(Color.RED);
        g.drawLine(x*mult, 0, x*mult, h*mult);
        g.setColor(Color.BLUE);
        g.drawLine(0, y*mult, w*mult, y*mult);
        g.setColor(Color.GREEN);
        g.drawRect(0, 0, w*mult, h*mult-1);
        glyphPreview.setIcon(new ImageIcon(_glyphPreview));
    }

    private boolean initial = false;

    private void glyphSelected(ListSelectionEvent e) {
        if(e.getValueIsAdjusting())
            return;
        updateGlyphPreview();
        FontGlyph glyph = (FontGlyph) list1.getSelectedValue();
        boolean valid = glyph != null;
        xEntry.setEnabled(valid);
        yEntry.setEnabled(valid);
        charEntry.setEnabled(valid);
        widthEntry.setEnabled(valid);
        if(!valid)
            return;
        initial = true;
        xEntry.getModel().setValue(glyph.xOff);
        yEntry.getModel().setValue(glyph.yOff);
        widthEntry.getModel().setValue(glyph.charWidth);
        charEntry.getModel().setValue(glyph.index);
        initial = false;
    }

    private void valueChanged(ChangeEvent e) {
        if(initial)
            return;
        FontGlyph glyph = (FontGlyph) list1.getSelectedValue();
        if(glyph == null)
            return;
        glyph.xOff = (Integer) xEntry.getValue();
        glyph.yOff = (Integer) yEntry.getValue();
        glyph.charWidth = (Integer) widthEntry.getValue();
        glyph.index = (Integer) charEntry.getValue();
        updateGlyphPreview();
        updatePreview();
    }

    private void previewTextPropertyChange(PropertyChangeEvent e) {
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        panel2 = new JPanel();
        fontPreview = new JLabel();
        panel4 = new JPanel();
        previewText = new JTextField();
        label1 = new JLabel();
        panel3 = new JPanel();
        scrollPane1 = new JScrollPane();
        list1 = new JList();
        panel5 = new JPanel();
        glyphPreview = new JLabel();
        panel6 = new JPanel();
        label5 = new JLabel();
        charEntry = new JSpinner();
        label4 = new JLabel();
        widthEntry = new JSpinner();
        label2 = new JLabel();
        xEntry = new JSpinner();
        label3 = new JLabel();
        yEntry = new JSpinner();

        //======== this ========
        setLayout(new GridBagLayout());
        ((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0};
        ((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0};
        ((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
        ((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 1.0, 1.0E-4};

        //======== panel2 ========
        {
            panel2.setBorder(new TitledBorder("Font Preview"));
            panel2.setLayout(new BorderLayout());

            //---- fontPreview ----
            fontPreview.setHorizontalAlignment(SwingConstants.CENTER);
            fontPreview.setBackground(Color.white);
            fontPreview.setPreferredSize(new Dimension(0, 50));
            fontPreview.setMinimumSize(new Dimension(0, 50));
            panel2.add(fontPreview, BorderLayout.NORTH);

            //======== panel4 ========
            {
                panel4.setLayout(new BorderLayout());

                //---- previewText ----
                previewText.setText("!\"\u00a3 $%^&*()_+-=`[]{};:'@#~?/>.<,\\|0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
                previewText.addCaretListener(new CaretListener() {
                    @Override
                    public void caretUpdate(CaretEvent e) {
                        previewTextChanged(e);
                    }
                });
                previewText.addPropertyChangeListener(new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent e) {
                        previewTextPropertyChange(e);
                    }
                });
                panel4.add(previewText, BorderLayout.CENTER);

                //---- label1 ----
                label1.setText("Preview Text: ");
                label1.setHorizontalAlignment(SwingConstants.TRAILING);
                label1.setLabelFor(previewText);
                panel4.add(label1, BorderLayout.WEST);
            }
            panel2.add(panel4, BorderLayout.SOUTH);
        }
        add(panel2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 0), 0, 0));

        //======== panel3 ========
        {
            panel3.setBorder(new TitledBorder("Font Glyphs"));
            panel3.setLayout(new BorderLayout());

            //======== scrollPane1 ========
            {

                //---- list1 ----
                list1.setLayoutOrientation(JList.HORIZONTAL_WRAP);
                list1.setMaximumSize(null);
                list1.setMinimumSize(null);
                list1.setVisibleRowCount(0);
                list1.addListSelectionListener(new ListSelectionListener() {
                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        glyphSelected(e);
                    }
                });
                scrollPane1.setViewportView(list1);
            }
            panel3.add(scrollPane1, BorderLayout.CENTER);

            //======== panel5 ========
            {
                panel5.setBorder(new TitledBorder("Glyph Properties"));
                panel5.setLayout(new BorderLayout());

                //---- glyphPreview ----
                glyphPreview.setHorizontalAlignment(SwingConstants.CENTER);
                glyphPreview.setBackground(Color.white);
                panel5.add(glyphPreview, BorderLayout.NORTH);

                //======== panel6 ========
                {
                    panel6.setLayout(new GridBagLayout());
                    ((GridBagLayout)panel6.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0};
                    ((GridBagLayout)panel6.getLayout()).rowHeights = new int[] {0, 0, 0};
                    ((GridBagLayout)panel6.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0E-4};
                    ((GridBagLayout)panel6.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0E-4};

                    //---- label5 ----
                    label5.setText("Char: ");
                    label5.setHorizontalAlignment(SwingConstants.TRAILING);
                    panel6.add(label5, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 5), 0, 0));

                    //---- charEntry ----
                    charEntry.setModel(new SpinnerNumberModel(0, 0, 65535, 1));
                    charEntry.addChangeListener(new ChangeListener() {
                        @Override
                        public void stateChanged(ChangeEvent e) {
                            valueChanged(e);
                        }
                    });
                    panel6.add(charEntry, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 5), 0, 0));

                    //---- label4 ----
                    label4.setText("Width: ");
                    label4.setHorizontalAlignment(SwingConstants.TRAILING);
                    label4.setLabelFor(widthEntry);
                    panel6.add(label4, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 5), 0, 0));

                    //---- widthEntry ----
                    widthEntry.setModel(new SpinnerNumberModel(0, -128, 127, 1));
                    widthEntry.addChangeListener(new ChangeListener() {
                        @Override
                        public void stateChanged(ChangeEvent e) {
                            valueChanged(e);
                        }
                    });
                    panel6.add(widthEntry, new GridBagConstraints(3, 0, 1, 1, 2.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 0), 0, 0));

                    //---- label2 ----
                    label2.setText("X: ");
                    label2.setHorizontalAlignment(SwingConstants.TRAILING);
                    label2.setLabelFor(xEntry);
                    panel6.add(label2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));

                    //---- xEntry ----
                    xEntry.setModel(new SpinnerNumberModel(0, -128, 127, 1));
                    xEntry.addChangeListener(new ChangeListener() {
                        @Override
                        public void stateChanged(ChangeEvent e) {
                            valueChanged(e);
                        }
                    });
                    panel6.add(xEntry, new GridBagConstraints(1, 1, 1, 1, 2.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));

                    //---- label3 ----
                    label3.setText("Y: ");
                    label3.setHorizontalAlignment(SwingConstants.TRAILING);
                    label3.setLabelFor(yEntry);
                    panel6.add(label3, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));

                    //---- yEntry ----
                    yEntry.setModel(new SpinnerNumberModel(0, -128, 127, 1));
                    yEntry.addChangeListener(new ChangeListener() {
                        @Override
                        public void stateChanged(ChangeEvent e) {
                            valueChanged(e);
                        }
                    });
                    panel6.add(yEntry, new GridBagConstraints(3, 1, 1, 1, 2.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
                }
                panel5.add(panel6, BorderLayout.CENTER);
            }
            panel3.add(panel5, BorderLayout.PAGE_END);
        }
        add(panel3, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 0), 0, 0));
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JPanel panel2;
    private JLabel fontPreview;
    private JPanel panel4;
    private JTextField previewText;
    private JLabel label1;
    private JPanel panel3;
    private JScrollPane scrollPane1;
    private JList list1;
    private JPanel panel5;
    private JLabel glyphPreview;
    private JPanel panel6;
    private JLabel label5;
    private JSpinner charEntry;
    private JLabel label4;
    private JSpinner widthEntry;
    private JLabel label2;
    private JSpinner xEntry;
    private JLabel label3;
    private JSpinner yEntry;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
