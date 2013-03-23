/*
 * Created by JFormDesigner on Sat Mar 16 10:40:39 GMT 2013
 */

package com.sqrt4.grimedi.ui.editor;

import java.beans.*;
import javax.swing.border.*;
import javax.swing.event.*;
import com.sqrt.liblab.entry.graphics.FontGlyph;
import com.sqrt.liblab.entry.graphics.GrimFont;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.event.ListDataListener;

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
        panel1 = new JSplitPane();
        panel2 = new JPanel();
        panel7 = new JScrollPane();
        fontPreview = new JLabel();
        panel4 = new JPanel();
        previewText = new JTextField();
        label1 = new JLabel();
        panel3 = new JSplitPane();
        scrollPane1 = new JScrollPane();
        list1 = new JList();
        panel5 = new JPanel();
        glyphPreview = new JLabel();
        panel6 = new JPanel();
        label5 = new JLabel();
        charEntry = new JSpinner();
        label2 = new JLabel();
        xEntry = new JSpinner();
        label3 = new JLabel();
        yEntry = new JSpinner();
        label4 = new JLabel();
        widthEntry = new JSpinner();

        //======== this ========
        setLayout(new BorderLayout());

        //======== panel1 ========
        {
            panel1.setOrientation(JSplitPane.VERTICAL_SPLIT);
            panel1.setResizeWeight(0.3);

            //======== panel2 ========
            {
                panel2.setBorder(new TitledBorder("Font Preview"));
                panel2.setLayout(new BorderLayout());

                //======== panel7 ========
                {

                    //---- fontPreview ----
                    fontPreview.setHorizontalAlignment(SwingConstants.CENTER);
                    fontPreview.setBackground(Color.white);
                    panel7.setViewportView(fontPreview);
                }
                panel2.add(panel7, BorderLayout.CENTER);

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
            panel1.setTopComponent(panel2);

            //======== panel3 ========
            {
                panel3.setBorder(new TitledBorder("Font Glyphs"));
                panel3.setOrientation(JSplitPane.VERTICAL_SPLIT);
                panel3.setResizeWeight(0.5);

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
                panel3.setTopComponent(scrollPane1);

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
                        ((GridBagLayout)panel6.getLayout()).columnWidths = new int[] {0, 0, 0};
                        ((GridBagLayout)panel6.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0};
                        ((GridBagLayout)panel6.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
                        ((GridBagLayout)panel6.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

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
                            new Insets(0, 0, 5, 0), 0, 0));

                        //---- label2 ----
                        label2.setText("X: ");
                        label2.setHorizontalAlignment(SwingConstants.TRAILING);
                        label2.setLabelFor(xEntry);
                        panel6.add(label2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 5), 0, 0));

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
                            new Insets(0, 0, 5, 0), 0, 0));

                        //---- label3 ----
                        label3.setText("Y: ");
                        label3.setHorizontalAlignment(SwingConstants.TRAILING);
                        label3.setLabelFor(yEntry);
                        panel6.add(label3, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 5), 0, 0));

                        //---- yEntry ----
                        yEntry.setModel(new SpinnerNumberModel(0, -128, 127, 1));
                        yEntry.addChangeListener(new ChangeListener() {
                            @Override
                            public void stateChanged(ChangeEvent e) {
                                valueChanged(e);
                            }
                        });
                        panel6.add(yEntry, new GridBagConstraints(1, 2, 1, 1, 2.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 0), 0, 0));

                        //---- label4 ----
                        label4.setText("Width: ");
                        label4.setHorizontalAlignment(SwingConstants.TRAILING);
                        label4.setLabelFor(widthEntry);
                        panel6.add(label4, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
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
                        panel6.add(widthEntry, new GridBagConstraints(1, 3, 1, 1, 2.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 0), 0, 0));
                    }
                    panel5.add(panel6, BorderLayout.CENTER);
                }
                panel3.setBottomComponent(panel5);
            }
            panel1.setBottomComponent(panel3);
        }
        add(panel1, BorderLayout.CENTER);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JSplitPane panel1;
    private JPanel panel2;
    private JScrollPane panel7;
    private JLabel fontPreview;
    private JPanel panel4;
    private JTextField previewText;
    private JLabel label1;
    private JSplitPane panel3;
    private JScrollPane scrollPane1;
    private JList list1;
    private JPanel panel5;
    private JLabel glyphPreview;
    private JPanel panel6;
    private JLabel label5;
    private JSpinner charEntry;
    private JLabel label2;
    private JSpinner xEntry;
    private JLabel label3;
    private JSpinner yEntry;
    private JLabel label4;
    private JSpinner widthEntry;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
