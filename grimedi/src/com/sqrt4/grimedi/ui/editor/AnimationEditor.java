/*
 * Created by JFormDesigner on Fri Mar 22 15:04:56 GMT 2013
 */

package com.sqrt4.grimedi.ui.editor;

import java.awt.event.*;

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
import com.sqrt.liblab.threed.Vector3f;
import com.sqrt4.grimedi.ui.component.FrameCallback;
import com.sqrt4.grimedi.ui.component.ModelRenderer;
import com.sqrt4.grimedi.util.AnimatedGifCreator;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author James Lawrence
 */
public class AnimationEditor extends EditorPanel<Animation> {
    private LabCollection _container;
    private boolean _animating;
    private Runnable animator = new Runnable() {
        public void run() {
            long last = System.currentTimeMillis();
            float frame = 0f;
            while(_animating) {
                int fps = (int) fpsSelect.getValue();
                int frameTime = 1000/fps;
                long time = System.currentTimeMillis();
                long delta = time - last;
                last = time;
                frame += ((float) delta / (float) frameTime);
                while(frame >= data.numFrames)
                    frame -= data.numFrames;
                setFrame(frame);
                try {
                    Thread.sleep(10);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };
    private Thread animateThread;

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
            modelSelectorItemStateChanged(null);
        }
        renderer.setModel(renderer.getModel()); // Forces the renderer to reset the view...
        stopAction.actionPerformed(null);

        frameSlider.setMaximum(data.numFrames - 1);
        frameSlider.setValue(0);
    }

    public void setFrame(float frame) {
        // Todo: move most of this into the model...
        if(frameSlider.getValue() != (int) frame)
            frameSlider.setValue((int) frame);
        // Reset model...
        renderer.getModel().reset();

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
            Vector3f pos = last.pos;
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

    private void changeFrame(ChangeEvent e) {
        setFrame(frameSlider.getValue());
    }

    private void modelSelectorItemStateChanged(ItemEvent e) {
        DataSource selected = (DataSource) modelSelector.getSelectedItem();
        try {
            selected.position(0);
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
        renderer = new ModelRenderer();
        panel3 = new JPanel();
        label1 = new JLabel();
        modelSelector = new JComboBox();
        panel4 = new JPanel();
        label2 = new JLabel();
        frameSlider = new JSlider();
        label3 = new JLabel();
        fpsSelect = new JSpinner();
        panel1 = new JPanel();
        playButton = new JButton();
        button1 = new JButton();
        button2 = new JButton();
        playAction = new PlayAction();
        stopAction = new StopAction();
        exportGif = new ExportGIFAction();

        //======== this ========
        setLayout(new BorderLayout());
        add(renderer, BorderLayout.CENTER);

        //======== panel3 ========
        {
            panel3.setLayout(new GridBagLayout());
            ((GridBagLayout)panel3.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
            ((GridBagLayout)panel3.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
            ((GridBagLayout)panel3.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0, 1.0E-4};
            ((GridBagLayout)panel3.getLayout()).rowWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};

            //---- label1 ----
            label1.setText("Target model: ");
            label1.setHorizontalAlignment(SwingConstants.TRAILING);
            panel3.add(label1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));

            //---- modelSelector ----
            modelSelector.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    modelSelectorItemStateChanged(e);
                }
            });
            panel3.add(modelSelector, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));

            //======== panel4 ========
            {
                panel4.setBorder(new TitledBorder("Anim control"));
                panel4.setLayout(new GridBagLayout());
                ((GridBagLayout)panel4.getLayout()).columnWidths = new int[] {0, 0, 0};
                ((GridBagLayout)panel4.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
                ((GridBagLayout)panel4.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
                ((GridBagLayout)panel4.getLayout()).rowWeights = new double[] {1.0, 0.0, 0.0, 1.0E-4};

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

                //---- fpsSelect ----
                fpsSelect.setModel(new SpinnerNumberModel(15, 1, null, 1));
                panel4.add(fpsSelect, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
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
        add(panel3, BorderLayout.PAGE_END);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private ModelRenderer renderer;
    private JPanel panel3;
    private JLabel label1;
    private JComboBox modelSelector;
    private JPanel panel4;
    private JLabel label2;
    private JSlider frameSlider;
    private JLabel label3;
    private JSpinner fpsSelect;
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
            _animating = true;
            animateThread = new Thread(animator);
            animateThread.setPriority(1);
            animateThread.setDaemon(true);
            animateThread.start();
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
            _animating = false;
            setEnabled(false);
            try {
                if(animateThread != null)
                    animateThread.join();
                animateThread = null;
            } catch (InterruptedException e1) {
                /**/
            }
            playAction.setEnabled(true);
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
            stopAction.actionPerformed(null);
            final AtomicBoolean cancel = new AtomicBoolean(false);
            window.runAsyncWithPopup("Rendering animation (frame 1/" + data.numFrames + ")", new Runnable() {
                public void run() {
                    try {
                        final Queue<BufferedImage> images = new LinkedList<>();
                        File temp = File.createTempFile("anim", ".gif");
                        ImageOutputStream ios = ImageIO.createImageOutputStream(temp);
                        AnimatedGifCreator agc = new AnimatedGifCreator(ios, BufferedImage.TYPE_INT_RGB, 14.99992f, true, 0);
                        frameSlider.setValue(0);
                        FrameCallback screenshotCallback = new FrameCallback() {
                            public void preDisplay(GL2 gl2) {
                            }

                            public void postDisplay(GL2 gl2) {
                                int frame = frameSlider.getValue();

                                synchronized (images) {
                                    gl2.glReadBuffer(GL.GL_BACK);
                                    int w = renderer.getViewportWidth(), h = renderer.getViewportHeight();
                                    ByteBuffer glBB = ByteBuffer.allocateDirect(4 * w * h);
                                    int[] buf = new int[w*h];
                                    gl2.glReadPixels(0, 0, w, h, GL.GL_RGBA, GL.GL_BYTE, glBB);
                                    BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

                                    // build RGB buffer
                                    int off = (h-1) * w;
                                    for(int y = 0; y < h; y++) {
                                        for(int x = 0; x < w; x++) {
                                            int r = 2 * glBB.get();
                                            int g = 2 * glBB.get();
                                            int b = 2 * glBB.get();
                                            int a = glBB.get();

                                            buf[off + x] = (r << 16) | (g << 8) | b;
                                        }
                                        off -= w;
                                    }

                                    bi.setRGB(0, 0, w, h, buf, 0, w);

                                    images.add(bi);
                                    images.notify();
                                }
                                frameSlider.setValue(frameSlider.getValue() + 1);
                                window.setBusyMessage("Rendering animation (frame " + frameSlider.getValue() + "/" + data.numFrames + ")");

                                if (frame >= data.numFrames - 1 || cancel.get())
                                    renderer.setCallback(null);
                            }
                        };
                        renderer.setCallback(screenshotCallback);
                        int processed = 0;
                        while (processed < data.numFrames && !cancel.get()) {
                            // Wait for notify...
                            BufferedImage image = null;
                            synchronized (images) {
                                if (images.isEmpty()) {
                                    try {
                                        images.wait(10);
                                    } catch (InterruptedException ignore) {
                                    }
                                }
                                if (images.isEmpty())
                                    continue;
                                image = images.poll();
                            }
                            if(renderer.getCallback() != screenshotCallback)
                                window.setBusyMessage("Generating GIF (frame " + processed + " / " + data.numFrames + ")");
                            agc.addFrame(image);
                            processed++;

                            if(processed >= data.numFrames && !cancel.get()) {
                                agc.finish();
                                ios.close();
                                if (Desktop.isDesktopSupported())
                                    Desktop.getDesktop().open(temp);
                            }
                        }

                        if(cancel.get()) {
                            agc.finish();
                            ios.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, true, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    cancel.set(true);
                }
            });
        }
    }
}