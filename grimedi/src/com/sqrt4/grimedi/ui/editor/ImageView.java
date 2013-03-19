/*
 * Created by JFormDesigner on Mon Mar 18 20:01:59 GMT 2013
 */

package com.sqrt4.grimedi.ui.editor;

import java.awt.event.*;
import javax.swing.event.*;

import com.sqrt.liblab.LabFile;
import com.sqrt.liblab.model.GrimBitmap;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

/**
 * @author James Lawrence
 */
public class ImageView extends EditorPanel<GrimBitmap> {
    private BufferedImage previewImage;
    private boolean playAnimation;
    private Thread animationThread;

    public ImageView() {
        initComponents();
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
        panel3 = new JSplitPane();
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

            //======== panel1 ========
            {

                //---- preview ----
                preview.setHorizontalAlignment(SwingConstants.CENTER);
                panel1.setViewportView(preview);
            }
            panel3.setTopComponent(panel1);

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
        BufferedImage rep = data.images.get(0);
        previewImage = new BufferedImage(rep.getWidth(), rep.getHeight(), BufferedImage.TYPE_INT_ARGB);
        imageList.setModel(new ListModel() {
            public int getSize() {
                return data.images.size();
            }

            public Object getElementAt(int index) {
                return data.images.get(index);
            }

            public void addListDataListener(ListDataListener l) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public void removeListDataListener(ListDataListener l) {
                //To change body of implemented methods use File | Settings | File Templates.
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
    private JSplitPane panel3;
    private JScrollPane panel1;
    private JLabel preview;
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
}
