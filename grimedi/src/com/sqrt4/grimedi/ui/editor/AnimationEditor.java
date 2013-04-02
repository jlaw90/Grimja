/*
 * Created by JFormDesigner on Fri Mar 22 15:04:56 GMT 2013
 */

package com.sqrt4.grimedi.ui.editor;

import com.jogamp.opengl.util.awt.Screenshot;
import com.sqrt.liblab.LabCollection;
import com.sqrt.liblab.codec.CodecMapper;
import com.sqrt.liblab.codec.EntryCodec;
import com.sqrt.liblab.entry.model.GrimModel;
import com.sqrt.liblab.entry.model.ModelNode;
import com.sqrt.liblab.entry.model.anim.Animation;
import com.sqrt.liblab.entry.model.anim.AnimationNode;
import com.sqrt.liblab.entry.model.anim.KeyFrame;
import com.sqrt.liblab.io.DataSource;
import com.sqrt.liblab.threed.Angle;
import com.sqrt.liblab.threed.Vector3;
import com.sqrt4.grimedi.ui.component.FrameCallback;
import com.sqrt4.grimedi.ui.component.ModelRenderer;
import com.sqrt4.grimedi.util.AnimatedGifEncoder;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageOutputStream;
import javax.media.opengl.GL2;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;

/**
 * @author James Lawrence
 */
public class AnimationEditor extends EditorPanel<Animation> {
    private LabCollection _container;
    private boolean _exporting;
    private Timer animTimer = new Timer(1000 / 15, new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            if (!isVisible())
                stopAction.actionPerformed(null);
            else
                frameSlider.setValue((frameSlider.getValue() + 1) % data.numFrames);
        }
    });

    public AnimationEditor() {
        initComponents();
    }

    ImageIcon icon = new ImageIcon(getClass().getResource("/run.png"));

    public ImageIcon getIcon() {
        return icon;
    }

    public void onNewData() {
        if (_container != data.container.container) {
            Vector<DataSource> allModels = new Vector<DataSource>();
            allModels.addAll(data.container.container.findByType(GrimModel.class));
            _container = data.container.container;
            modelSelector.setModel(new DefaultComboBoxModel(allModels));
            modelSelectorPopupMenuWillBecomeInvisible(null);
        }
        renderer.setModel(renderer.getModel()); // Forces the renderer to reset the view...
        stopAction.actionPerformed(null);

        frameSlider.setMaximum(data.numFrames - 1);
        frameSlider.setValue(0);
    }

    private void changeFrame(ChangeEvent e) {
        int frame = frameSlider.getValue();

        // Reset model...
        for (ModelNode node : renderer.getModel().hierarchy) {
            node.animRoll = node.animPitch = node.animYaw = Angle.zero;
            node.animPos = Vector3.zero;
        }

        if (data.nodes.isEmpty())
            return;

        if (frame > data.numFrames)
            frame = data.numFrames;

        for (AnimationNode node : data.nodes) {
            if (node == null || node.entries.isEmpty())
                continue;
            ModelNode mn = renderer.getModel().findNode(node.meshName);
            if (mn == null)
                return;

            boolean useDelta = (data.flags & 256) == 0;

            // Do a binary search for the nearest previous frame
            // Loop invariant: entries_[low].frame_ <= frame < entries_[high].frame_
            int low = 0, high = node.entries.size();
            while (high > low + 1) {
                int mid = (low + high) / 2;
                if (node.entries.get(mid).frame <= frame)
                    low = mid;
                else
                    high = mid;
            }
            KeyFrame last = node.entries.get(low);

            float dt = frame - last.frame;
            Vector3 pos = last.pos;
            Angle pitch = last.pitch;
            Angle yaw = last.yaw;
            Angle roll = last.roll;

            if (useDelta) {
                pos = pos.add(last.dpos.mult(dt));
                pitch = pitch.add(last.dpitch.mult(dt));
                yaw = yaw.add(last.dyaw.mult(dt));
                roll = roll.add(last.droll.mult(dt));
            }

            mn.animPos = pos.sub(mn.pos);
            mn.animPitch = pitch.sub(mn.pitch).normalize(-180);
            mn.animYaw = yaw.sub(mn.yaw).normalize(-180);
            mn.animRoll = roll.sub(mn.roll).normalize(-180);
        }
        renderer.refreshModelCache();
    }

    private void modelSelectorPopupMenuWillBecomeInvisible(PopupMenuEvent e) {
        DataSource selected = (DataSource) modelSelector.getSelectedItem();
        try {
            selected.seek(0);
            EntryCodec<GrimModel> codec = (EntryCodec<GrimModel>) CodecMapper.codecForProvider(selected);
            GrimModel model = codec.read(selected);
            if (model == renderer.getModel())
                return;
            renderer.setModel(model);
            frameSlider.setValue(0);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        panel2 = new JSplitPane();
        renderer = new ModelRenderer();
        panel3 = new JPanel();
        label1 = new JLabel();
        modelSelector = new JComboBox();
        panel4 = new JPanel();
        label2 = new JLabel();
        frameSlider = new JSlider();
        label3 = new JLabel();
        spinner1 = new JSpinner();
        panel1 = new JPanel();
        playButton = new JButton();
        button1 = new JButton();
        button2 = new JButton();
        playAction = new PlayAction();
        stopAction = new StopAction();
        exportGif = new ExportGIFAction();

        //======== this ========
        setLayout(new BorderLayout());

        //======== panel2 ========
        {
            panel2.setOrientation(JSplitPane.VERTICAL_SPLIT);
            panel2.setResizeWeight(0.7);
            panel2.setTopComponent(renderer);

            //======== panel3 ========
            {
                panel3.setLayout(new GridBagLayout());
                ((GridBagLayout) panel3.getLayout()).columnWidths = new int[]{0, 0, 0, 0};
                ((GridBagLayout) panel3.getLayout()).rowHeights = new int[]{0, 0, 0, 0};
                ((GridBagLayout) panel3.getLayout()).columnWeights = new double[]{0.0, 1.0, 1.0, 1.0E-4};
                ((GridBagLayout) panel3.getLayout()).rowWeights = new double[]{0.0, 1.0, 0.0, 1.0E-4};

                //---- label1 ----
                label1.setText("Target model: ");
                label1.setHorizontalAlignment(SwingConstants.TRAILING);
                panel3.add(label1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));

                //---- modelSelector ----
                modelSelector.addPopupMenuListener(new PopupMenuListener() {
                    @Override
                    public void popupMenuCanceled(PopupMenuEvent e) {
                    }

                    @Override
                    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                        modelSelectorPopupMenuWillBecomeInvisible(e);
                    }

                    @Override
                    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                    }
                });
                panel3.add(modelSelector, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));

                //======== panel4 ========
                {
                    panel4.setBorder(new TitledBorder("Anim control"));
                    panel4.setLayout(new GridBagLayout());
                    ((GridBagLayout) panel4.getLayout()).columnWidths = new int[]{0, 0, 0};
                    ((GridBagLayout) panel4.getLayout()).rowHeights = new int[]{0, 0, 0, 0};
                    ((GridBagLayout) panel4.getLayout()).columnWeights = new double[]{0.0, 1.0, 1.0E-4};
                    ((GridBagLayout) panel4.getLayout()).rowWeights = new double[]{1.0, 0.0, 0.0, 1.0E-4};

                    //---- label2 ----
                    label2.setText("Frame: ");
                    label2.setHorizontalAlignment(SwingConstants.TRAILING);
                    panel4.add(label2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 0, 0), 0, 0));

                    //---- frameSlider ----
                    frameSlider.setSnapToTicks(true);
                    frameSlider.setMajorTickSpacing(1);
                    frameSlider.setMaximum(50);
                    frameSlider.setPaintTicks(true);
                    frameSlider.setValue(0);
                    frameSlider.addChangeListener(new ChangeListener() {
                        @Override
                        public void stateChanged(ChangeEvent e) {
                            changeFrame(e);
                        }
                    });
                    panel4.add(frameSlider, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 0, 0), 0, 0));

                    //---- label3 ----
                    label3.setText("FPS: ");
                    label3.setHorizontalAlignment(SwingConstants.TRAILING);
                    panel4.add(label3, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 0, 0), 0, 0));
                    panel4.add(spinner1, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 0, 0), 0, 0));

                    //======== panel1 ========
                    {
                        panel1.setLayout(new FlowLayout());

                        //---- playButton ----
                        playButton.setAction(playAction);
                        panel1.add(playButton);

                        //---- button1 ----
                        button1.setText("text");
                        button1.setAction(stopAction);
                        button1.setEnabled(false);
                        panel1.add(button1);

                        //---- button2 ----
                        button2.setAction(exportGif);
                        panel1.add(button2);
                    }
                    panel4.add(panel1, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 0, 0), 0, 0));
                }
                panel3.add(panel4, new GridBagConstraints(1, 1, 2, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
            }
            panel2.setBottomComponent(panel3);
        }
        add(panel2, BorderLayout.CENTER);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JSplitPane panel2;
    private ModelRenderer renderer;
    private JPanel panel3;
    private JLabel label1;
    private JComboBox modelSelector;
    private JPanel panel4;
    private JLabel label2;
    private JSlider frameSlider;
    private JLabel label3;
    private JSpinner spinner1;
    private JPanel panel1;
    private JButton playButton;
    private JButton button1;
    private JButton button2;
    private PlayAction playAction;
    private StopAction stopAction;
    private ExportGIFAction exportGif;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    private class PlayAction extends AbstractAction {
        private PlayAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "Play");
            putValue(SHORT_DESCRIPTION, "play animation");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            animTimer.start();
            stopAction.setEnabled(true);
            setEnabled(false);
        }
    }

    private class StopAction extends AbstractAction {
        private StopAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "Stop");
            putValue(SHORT_DESCRIPTION, "stop animation");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            animTimer.stop();
            playAction.setEnabled(true);
            setEnabled(false);
        }
    }

    private class ExportGIFAction extends AbstractAction {
        private ExportGIFAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "Export GIF");
            putValue(SHORT_DESCRIPTION, "exports the current animation as a GIF (from the current viewpoint)");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            window.runAsyncWithPopup("Please wait...", "Rendering animation (frame 1/" + data.numFrames + ")", new Runnable() {
                public void run() {
                    try {
                        final Queue<BufferedImage> images = new LinkedList<BufferedImage>();
                        final AnimatedGifEncoder age = new AnimatedGifEncoder();
                        age.setQuality(10);
                        age.setFrameRate(15);
                        age.setRepeat(0);
                        File file = File.createTempFile("anim", ".gif");
                        age.start(file.getAbsolutePath());
                        frameSlider.setValue(0);
                        FrameCallback screenshotCallback = new FrameCallback() {
                            public void preDisplay(GL2 gl2) {
                            }

                            public void postDisplay(GL2 gl2) {
                                int frame = frameSlider.getValue();

                                synchronized (images) {
                                    images.add(Screenshot.readToBufferedImage(renderer.getViewportWidth(), renderer.getViewportHeight()));
                                    images.notify();
                                }
                                frameSlider.setValue(frameSlider.getValue() + 1);
                                window.setBusyMessage("Rendering animation (frame " + frameSlider.getValue() + "/" + data.numFrames + ")");

                                if (frame >= data.numFrames - 1)
                                    renderer.setCallback(null);
                            }
                        };
                        renderer.setCallback(screenshotCallback);
                        int processed = 0;
                        while (processed < data.numFrames) {
                            // Wait for notify...
                            BufferedImage image = null;
                            synchronized (images) {
                                if (images.isEmpty()) {
                                    try {
                                        images.wait();
                                    } catch (InterruptedException ignore) {
                                    }
                                }
                                if (images.isEmpty())
                                    continue;
                                image = images.poll();
                            }
                            if(renderer.getCallback() != screenshotCallback)
                                window.setBusyMessage("Generating GIF (frame " + processed + " / " + data.numFrames + ")");
                            age.addFrame(image);
                            processed++;
                            if(processed >= data.numFrames) {
                                age.finish();
                                if (Desktop.isDesktopSupported())
                                    Desktop.getDesktop().open(file);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}