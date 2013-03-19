/*
 * Created by JFormDesigner on Mon Mar 18 20:01:59 GMT 2013
 */

package com.sqrt4.grimedi.ui.editor;

import java.awt.event.*;

import com.sqrt.liblab.EntryDataProvider;
import com.sqrt.liblab.LabFile;
import com.sqrt.liblab.codec.CodecMapper;
import com.sqrt.liblab.codec.EntryCodec;
import com.sqrt.liblab.model.ColorMap;
import com.sqrt.liblab.model.GrimBitmap;
import com.sqrt.liblab.model.Material;
import com.sqrt.liblab.model.Texture;
import com.sqrt4.grimedi.util.CachedPredicate;
import com.sqrt4.grimedi.util.FilterableComboBoxModel;
import com.sqrt4.grimedi.util.Predicate;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;

/**
 * @author James Lawrence
 */
public class MaterialView extends EditorPanel<Material> {
    private LabFile _container;
    private boolean playAnimation;
    private Thread animationThread;
    private FilterableComboBoxModel<ColorMap> colorMapModel;
    private Predicate<ColorMap> colorMapNameFilter;

    public MaterialView() {
        initComponents();
    }

    private void updatePreview() {
        preview.setIcon(null);
        if (imageList.getSelectedValue() == null || colorMapSelector.getSelectedItem() == null || colorMapSelector.getSelectedItem() instanceof String)
            return;
        preview.setIcon(new ImageIcon(((Texture) imageList.getSelectedValue()).render((ColorMap) colorMapSelector.getSelectedItem())));
    }

    private void imageSelected(ListSelectionEvent e) {
        updatePreview();
    }

    private void colorMapSelected(ItemEvent e) {
        ListCellRenderer rend = imageList.getCellRenderer();
        if (!(rend instanceof OurCellRenderer))
            return;
        ((OurCellRenderer) rend).cache.clear();
        updatePreview();
    }

    private void createUIComponents() {
        colorMapSelector = new JComboBox();
        colorMapSelector.setEditable(true);
        ComboBoxEditor editor = colorMapSelector.getEditor();
        if(editor instanceof BasicComboBoxEditor) {
            BasicComboBoxEditor bcb = (BasicComboBoxEditor) editor;
            final JTextField jtf = (JTextField) bcb.getEditorComponent();
            jtf.addCaretListener(new CaretListener() {
                private String lastFilter;
                public void caretUpdate(CaretEvent e) {
                    if(colorMapSelector.getSelectedItem() != null) {
                        ColorMap cur = (ColorMap) colorMapModel.getSelectedItem();
                        if(cur.getName().equals(jtf.getText()))
                            return;
                    }
                    if(!colorMapSelector.isPopupVisible())
                        colorMapSelector.setPopupVisible(true);
                    final String filter = jtf.getText();
                    if(filter.equals(lastFilter))
                        return;
                    colorMapModel.removeFilter(colorMapNameFilter);
                    colorMapNameFilter = null;
                    if(!filter.isEmpty()) {
                        colorMapModel.addFilter(colorMapNameFilter = new CachedPredicate<ColorMap>(new Predicate<ColorMap>() {
                            public boolean accept(ColorMap colorMap) {
                                return colorMap.getName().toLowerCase().contains(filter);
                            }
                        }));
                    }
                    lastFilter = filter;
                    colorMapModel.applyFilters();
                }
            });
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        createUIComponents();

        panel3 = new JSplitPane();
        panel4 = new JPanel();
        panel1 = new JScrollPane();
        preview = new JLabel();
        panel2 = new JPanel();
        playButton = new JButton();
        scrollPane1 = new JScrollPane();
        imageList = new JList();
        playAction = new PlayAction();

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
                colorMapSelector.setPrototypeDisplayValue("Color Map");
                colorMapSelector.setEditable(true);
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

                //---- playButton ----
                playButton.setAction(playAction);
                panel2.add(playButton, BorderLayout.NORTH);

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
        if (data.container != _container) {
            _container = data.container;
            java.util.List<ColorMap> colorMaps = new ArrayList<ColorMap>();
            for (EntryDataProvider prov : data.container.entries) {
                EntryCodec<?> codec = CodecMapper.codecForProvider(prov);
                if (codec == null || codec.getEntryClass() != ColorMap.class)
                    continue;
                try {
                    prov.seek(0);
                    ColorMap cm = ((EntryCodec<ColorMap>) codec).read(prov);
                    if (cm == null)
                        continue;
                    colorMaps.add(cm);
                } catch (IOException ignore) {
                }
            }
            Collections.sort(colorMaps, new Comparator<ColorMap>() {
                public int compare(ColorMap o1, ColorMap o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });
            colorMapSelector.setModel(colorMapModel = new FilterableComboBoxModel<ColorMap>(colorMaps));
            colorMapSelector.setSelectedIndex(0);
        }
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
                BufferedImage bi = t.render((ColorMap) colorMapSelector.getSelectedItem());
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
    private JComboBox colorMapSelector;
    private JPanel panel2;
    private JButton playButton;
    private JScrollPane scrollPane1;
    private JList imageList;
    private PlayAction playAction;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    private class PlayAction extends AbstractAction {
        private PlayAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "Play");
            putValue(SHORT_DESCRIPTION, "Play animation");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            playAction.setEnabled(false);
            stopAction.setEnabled(true);
            playButton.setAction(stopAction);
            imageList.setEnabled(false);
            if (animationThread != null)
                return;
            animationThread = new Thread() {
                public void run() {
                    playAnimation = true;
                    while (MaterialView.this.isVisible() && playAnimation) {
                        imageList.setSelectedIndex((imageList.getSelectedIndex() + 1) % data.textures.size());
                        try {
                            Thread.sleep(125);
                        } catch (Exception e) {
                            /**/
                        }
                    }
                }
            };
            animationThread.setPriority(1);
            animationThread.setDaemon(true);
            animationThread.start();
        }
    }

    private StopAction stopAction = new StopAction();

    private class StopAction extends AbstractAction {

        private StopAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "Stop");
            putValue(SHORT_DESCRIPTION, "Stop animating");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            if (animationThread == null)
                return;
            try {
                playAnimation = false;
                animationThread.join();
            } catch (InterruptedException ie) {
                /**/
            }
            animationThread = null;
            playAction.setEnabled(true);
            stopAction.setEnabled(false);
            playButton.setAction(playAction);
            imageList.setEnabled(true);
        }
    }
}
