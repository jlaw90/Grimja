/*
 * Created by JFormDesigner on Mon Mar 18 20:01:59 GMT 2013
 */

package com.sqrt4.grimedi.ui.editor;

import java.awt.event.*;
import javax.imageio.ImageIO;
import javax.swing.event.*;

import com.sqrt.liblab.entry.graphics.GrimBitmap;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

/**
 * @author James Lawrence
 */
public class ImageView extends EditorPanel<GrimBitmap> {
    private boolean playAnimation;
    private Thread animationThread;

    public ImageView() {
        initComponents();
    }

    ImageIcon icon = new ImageIcon(getClass().getResource("/picture.png"));
    public ImageIcon getIcon() {
        return icon;
    }

    private void updatePreview() {
        if(imageList.getSelectedValue() == null) {
            preview.setIcon(null);
            return;
        }
        preview.setIcon(new ImageIcon((BufferedImage) imageList.getSelectedValue()));
    }

    private void imageSelected(ListSelectionEvent e) {
        updatePreview();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        panel4 = new JPanel();
        panel1 = new JScrollPane();
        preview = new JLabel();
        panel5 = new JPanel();
        button1 = new JButton();
        panel2 = new JPanel();
        scrollPane1 = new JScrollPane();
        imageList = new JList();
        panel6 = new JPanel();
        playButton = new JButton();
        button2 = new JButton();
        playAction = new PlayAction();
        exportAnimationAction = new ExportAnimationAction();
        exportPngAction = new ExportPNGAction();

        //======== this ========
        setLayout(new BorderLayout());

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

            //======== panel5 ========
            {
                panel5.setLayout(new FlowLayout());

                //---- button1 ----
                button1.setAction(exportPngAction);
                panel5.add(button1);
            }
            panel4.add(panel5, BorderLayout.SOUTH);
        }
        add(panel4, BorderLayout.CENTER);

        //======== panel2 ========
        {
            panel2.setLayout(new BorderLayout());

            //======== scrollPane1 ========
            {
                scrollPane1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

                //---- imageList ----
                imageList.setVisibleRowCount(0);
                imageList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
                imageList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                imageList.addListSelectionListener(new ListSelectionListener() {
                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        imageSelected(e);
                    }
                });
                scrollPane1.setViewportView(imageList);
            }
            panel2.add(scrollPane1, BorderLayout.CENTER);

            //======== panel6 ========
            {
                panel6.setLayout(new FlowLayout());

                //---- playButton ----
                playButton.setAction(playAction);
                panel6.add(playButton);

                //---- button2 ----
                button2.setAction(exportAnimationAction);
                panel6.add(button2);
            }
            panel2.add(panel6, BorderLayout.SOUTH);
        }
        add(panel2, BorderLayout.SOUTH);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    public void onNewData() {
        playAction.setEnabled(data.images.size() > 1);
        imageList.setModel(new ListModel() {
            public int getSize() {
                return data.images.size();
            }

            public Object getElementAt(int index) {
                return data.images.get(index);
            }

            public void addListDataListener(ListDataListener l) {
            }

            public void removeListDataListener(ListDataListener l) {
            }
        });
        imageList.setCellRenderer(new ListCellRenderer() {
            private Map<BufferedImage, JLabel> cache = new HashMap<BufferedImage, JLabel>();

            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                BufferedImage bi = (BufferedImage) value;
                JLabel label;
                if (cache.containsKey(bi))
                    label = cache.get(bi);
                else {
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
        });
        imageList.setSelectedIndex(0);
        updatePreview();
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JPanel panel4;
    private JScrollPane panel1;
    private JLabel preview;
    private JPanel panel5;
    private JButton button1;
    private JPanel panel2;
    private JScrollPane scrollPane1;
    private JList imageList;
    private JPanel panel6;
    private JButton playButton;
    private JButton button2;
    private PlayAction playAction;
    private ExportAnimationAction exportAnimationAction;
    private ExportPNGAction exportPngAction;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    private class PlayAction extends AbstractAction {
        private PlayAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "Animate");
            putValue(SHORT_DESCRIPTION, "Play animation");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            playAction.setEnabled(false);
            stopAction.setEnabled(true);
            playButton.setAction(stopAction);
            imageList.setEnabled(false);
            if(animationThread != null)
                return;
            animationThread = new Thread() {
                public void run() {
                    playAnimation = true;
                    while(ImageView.this.isVisible() && playAnimation) {
                        imageList.setSelectedIndex((imageList.getSelectedIndex() + 1) % data.images.size());
                        try {
                            Thread.sleep(125);
                        } catch(Exception e) {
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
            if(animationThread == null)
                return;
            try {
                playAnimation = false;
                animationThread.join();
            } catch(InterruptedException ie) {
                /**/
            }
            animationThread = null;
            playAction.setEnabled(true);
            stopAction.setEnabled(false);
            playButton.setAction(playAction);
            imageList.setEnabled(true);
        }
    }

    private class ExportAnimationAction extends AbstractAction {
        private ExportAnimationAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "Export Animated GIF");
            putValue(SHORT_DESCRIPTION, "exports the frames of this bitmap as a GIF");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            // TODO add your code here
        }
    }

    private class ExportPNGAction extends AbstractAction {
        private ExportPNGAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "Export PNG");
            putValue(SHORT_DESCRIPTION, "exports this image as a PNG file");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            final JFileChooser jfc = window.createFileDialog();
            String name = data.getName();
            int idx = name.lastIndexOf('.');
            if(idx != -1)
                name = name.substring(0, idx);
            if(data.images.size() != 1)
                name += "." + (imageList.getSelectedIndex() + 1);
            name += ".png";
            jfc.setSelectedFile(new File(name));
            if(jfc.showSaveDialog(window) == JFileChooser.APPROVE_OPTION) {
                window.runAsyncWithPopup("Exporting image...", new Runnable() {
                    public void run() {
                        File f = jfc.getSelectedFile();
                        try {
                            ImageIO.write((BufferedImage) imageList.getSelectedValue(), "png", f);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
            }
        }
    }
}