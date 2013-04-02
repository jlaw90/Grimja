/*
 * Created by JFormDesigner on Fri Mar 29 11:34:15 GMT 2013
 */

package com.sqrt4.grimedi.ui.editor;

import com.jogamp.opengl.util.awt.Screenshot;
import com.sqrt.liblab.entry.video.AudioTrack;
import com.sqrt.liblab.entry.video.Video;
import com.sqrt4.grimedi.ui.component.FrameCallback;
import com.sqrt4.grimedi.util.AnimatedGifEncoder;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageOutputStream;
import javax.media.opengl.GL2;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author James Lawrence
 */
public class VideoViewer extends EditorPanel<Video> {
    private BufferedImage surface;
    private boolean playing;

    private Runnable play = new Runnable() {
        public void run() {
            final int framePeriod = (int) (1000f / data.fps);
            int last_frame = -1;
            boolean valid = true;
            final WritableRaster raster = surface.getRaster();
            try {
                // Attempt to sync to the audio stream...
                if (data.audio.stream != null) {
                    AudioTrack audio = data.audio;
                    AudioFormat af = new AudioFormat(audio.sampleRate, audio.bits, audio.channels, true, true);
                    SourceDataLine sdl = (SourceDataLine) AudioSystem.getLine(new DataLine.Info(SourceDataLine.class, af));
                    sdl.open();
                    sdl.start();
                    byte[] buf = new byte[sdl.getBufferSize()];
                    audio.stream.seek(0);
                    int pos = 0;

                    final int bytesPerSample = (audio.bits / 8) * audio.channels;
                    final int bytesPerSec = bytesPerSample * audio.sampleRate;
                    final int bytesPerMs = bytesPerSec / 1000;

                    // VIMA is 50 frames ahead according to residual...
                    final int frameOff = 500;

                    while (valid) {
                        int ms = pos / bytesPerMs;
                        ms -= frameOff;
                        if (ms < 0)
                            ms = 0;
                        int frame = ms / framePeriod;
                        if (frame != last_frame && frame != last_frame + 1)
                            frame = last_frame + 1; // Interpolate, we can't drop frames!
                        last_frame = frame;

                        data.stream.setFrame(frame);
                        valid = playing && data.stream.readFrame(raster, data.width, data.height);
                        if (!valid)
                            break;
                        viewer.repaint();
                        int read = audio.stream.read(buf, 0, Math.min(buf.length, sdl.available()));
                        pos += read;
                        if (read == -1)
                            break;
                        sdl.write(buf, 0, read);
                    }
                    if (playing)
                        sdl.drain();
                    else
                        sdl.flush();

                    sdl.stop();
                    sdl.close();
                } else {
                    // Otherwise just try and keep the FPS
                    long start = System.currentTimeMillis();
                    while (valid) {
                        long elapsed = System.currentTimeMillis() - start;
                        int frame = (int) (elapsed / framePeriod);
                        if (frame != last_frame && frame != last_frame + 1)
                            frame = last_frame + 1; // Interpolate, can't drop...
                        last_frame = frame;

                        data.stream.setFrame(frame);
                        valid = playing && data.stream.readFrame(raster, data.width, data.height);
                        viewer.repaint();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            stopAction.setEnabled(false);
            playAction.setEnabled(true);
        }
    };

    public VideoViewer() {
        initComponents();
    }

    ImageIcon icon = new ImageIcon(getClass().getResource("/movie.png"));
    public ImageIcon getIcon() {
        return icon;
    }

    public void onNewData() {
        surface = new BufferedImage(data.width, data.height, data.format);
        viewer.setIcon(new ImageIcon(surface));
        playing = false;
        try {
            data.stream.setFrame(0);
            data.stream.readFrame(surface.getRaster(), data.width, data.height);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void onHide() {
        playing = false;
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        viewer = new JLabel();
        panel1 = new JPanel();
        button1 = new JButton();
        button2 = new JButton();
        button3 = new JButton();
        playAction = new PlayAction();
        stopAction = new StopAction();
        exportGifAction = new ExportGifAction();

        //======== this ========
        setLayout(new BorderLayout());

        //---- viewer ----
        viewer.setBackground(Color.black);
        viewer.setOpaque(true);
        viewer.setHorizontalAlignment(SwingConstants.CENTER);
        add(viewer, BorderLayout.CENTER);

        //======== panel1 ========
        {
            panel1.setLayout(new FlowLayout());

            //---- button1 ----
            button1.setAction(playAction);
            panel1.add(button1);

            //---- button2 ----
            button2.setAction(stopAction);
            panel1.add(button2);

            //---- button3 ----
            button3.setAction(exportGifAction);
            panel1.add(button3);
        }
        add(panel1, BorderLayout.SOUTH);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JLabel viewer;
    private JPanel panel1;
    private JButton button1;
    private JButton button2;
    private JButton button3;
    private PlayAction playAction;
    private StopAction stopAction;
    private ExportGifAction exportGifAction;
// JFormDesigner - End of variables declaration  //GEN-END:variables

    private class PlayAction extends AbstractAction {
        private PlayAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "Play");
            putValue(SHORT_DESCRIPTION, "Play the video");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            playAction.setEnabled(false);
            stopAction.setEnabled(true);
            playing = true;
            new Thread(play).start();
        }
    }

    private class StopAction extends AbstractAction {
        private StopAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "Stop");
            putValue(SHORT_DESCRIPTION, "hammer time");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            playing = false;
        }
    }

    private class ExportGifAction extends AbstractAction {
        private ExportGifAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "Export GIF");
            putValue(SHORT_DESCRIPTION, "Export the frames of this video as a GIF animation");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            window.runAsyncWithPopup("Please wait...", "Decoding video...", new Runnable() {
                public void run() {
                    window.setBusyMessage("Generating GIF (frame 1/" + data.numFrames + ")");
                    try {
                        AnimatedGifEncoder age = new AnimatedGifEncoder();
                        age.setSize(data.width, data.height);
                        age.setRepeat(0);
                        age.setFrameRate(14.99992f);
                        age.setQuality(10);
                        File temp = File.createTempFile("anim", ".gif");
                        FileOutputStream fos = new FileOutputStream(temp);
                        age.start(fos);
                        WritableRaster raster = surface.getRaster();
                        for (int i1 = 0; i1 < data.numFrames; i1++) {
                            data.stream.setFrame(i1);
                            boolean cont = data.stream.readFrame(raster, data.width, data.height);
                            viewer.repaint();
                            age.addFrame(surface);
                            window.setBusyMessage("Generating GIF (frame " + (i1 + 1) + "/" + data.numFrames + ")");
                            if(!cont)
                                break;
                        }
                        age.finish();
                        fos.close();
                        if (Desktop.isDesktopSupported())
                            Desktop.getDesktop().open(temp);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}