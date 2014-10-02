

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
 * Created by JFormDesigner on Sun Mar 24 18:57:33 GMT 2013
 */

package com.sqrt4.grimedi.ui.editor;

import com.sqrt.liblab.entry.audio.Audio;
import com.sqrt.liblab.entry.audio.Jump;
import com.sqrt.liblab.entry.audio.Region;
import com.sqrt4.grimedi.Main;
import com.sqrt4.grimedi.ui.MainWindow;

import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author James Lawrence
 */
public class AudioEditor extends EditorPanel<Audio> {
    private boolean _playing;
    private Region _playRegion;
    private Region selected;
    private Jump selectedJump;
    private Map<Jump, Boolean> activeJumps = new HashMap<>();
    private SourceDataLine dataLine;
    private int _currentOffset;
    private Runnable _play = new Runnable() {
        public void run() {
            dataLine.start();
            int len = Math.min(dataLine.getBufferSize(), 1024); // how much we write per update loop...
            byte[] buf = new byte[len];
            regionLoop:
            while (_playRegion != null && _playing) {
                // Handle jumps...
                for (Jump j : _playRegion.jumps) {
                    if (activeJumps.containsKey(j) && activeJumps.get(j)) {
                        _playRegion = j.target;
                        continue regionLoop;
                    }
                }

                regionTree.repaint();
                int off = 0;
                _currentOffset = _playRegion.offset;
                try {
                    data.stream.seek(_playRegion.offset);
                    while (_playing && off < _playRegion.length) {
                        int read = data.stream.read(buf, 0, Math.min(len, _playRegion.length - off));
                        off += read;
                        dataLine.write(buf, 0, read);
                        _currentOffset += read;
                        updateTimeDisplay();
                    }
                } catch (Exception e) {
                    MainWindow.getInstance().handleException(e);
                }
                int idx = data.regions.indexOf(_playRegion);
                if (idx == data.regions.size() - 1)
                    _playRegion = null;
                else
                    _playRegion = data.regions.get(idx + 1);
            }
            if (!_playing) { // User stopped...
                dataLine.flush();
            } else {
                _playing = false;
                dataLine.drain();
            }
            _playRegion = null;
            dataLine.stop();
            playAllAction.setEnabled(true);
            playSelectedRegion.setEnabled(selected != null);
            stopAction.setEnabled(false);
            currentTime.setText("");
            totalTime.setText("");
            timeSlider.setValue(0);
        }
    };

    public AudioEditor() {
        initComponents();
        targetSelector.setRenderer(new RegionListCellRenderer());
    }

    ImageIcon icon = new ImageIcon(getClass().getResource("/sound.png"));

    public ImageIcon getIcon() {
        return icon;
    }

    private void updateTimeDisplay() {
        timeSlider.setValue(_currentOffset);
        currentTime.setText(msToString(data.bytesToTime(_currentOffset)));
    }

    private void regionTreeValueChanged(TreeSelectionEvent e) {
        boolean regionSelection = false, jumpSelection = false, soundSelected = false;
        Object sel = null;

        if (regionTree.getSelectionPath() != null) {
            sel = regionTree.getSelectionPath().getLastPathComponent();
            if (sel instanceof Region)
                regionSelection = true;
            else if (sel instanceof Jump)
                jumpSelection = true;
            else if (sel instanceof Audio)
                soundSelected = true;
        }
        selected = regionSelection ? (Region) sel : null;
        playSelectedRegion.setEnabled(regionSelection && !_playing);
        selectedJump = jumpSelection ? (Jump) sel : null;
        propertyContainer.removeAll();
        if (jumpSelection) {
            propertyContainer.add(jumpPropertyPanel);
            hookSpinner.setValue(selectedJump.hookId);
            fadeSpinner.setValue(selectedJump.fadeDelay);
            jumpEnabled.setSelected(activeJumps.containsKey(selectedJump) && activeJumps.get(selectedJump));
            targetSelector.setSelectedItem(selectedJump.target);
        }
        if (regionSelection) {
            propertyContainer.add(regionPropertyPanel);
            startLabel.setText(msToString(data.bytesToTime(selected.offset)));
            durationLabel.setText(msToString(data.bytesToTime(selected.length)));
            commentArea.setText(selected.comments == null ? "" : selected.comments);
        }
        if (soundSelected) {
            propertyContainer.add(soundPropertyPanel);
            bitsPerSample.setText(String.valueOf(data.bits));
            bitrate.setText(String.valueOf((data.bits * data.channels * data.sampleRate) / 1000) + "kBps");
            numChannels.setText(String.valueOf(data.channels));
            sampleRate.setText(String.valueOf(data.sampleRate));
            name.setText(data.getName());
        }

        propertyContainer.revalidate();
        propertyContainer.repaint();
    }

    private String msToString(int ms) {
        int secs = (ms / 1000);
        int mins = secs / 60;
        secs = secs % 60;
        return String.format("%02d:%02d", mins, secs);
    }

    private String regionToString(Region r, boolean altFormat) {
        String timeRange = msToString(data.bytesToTime(r.offset));
        if (r.comments != null) {
            String name = r.comments;
            int lidx = name.indexOf('\n');
            if (lidx != -1)
                name = name.substring(0, lidx);
            if (altFormat)
                return timeRange + " (" + name + ")";
            return name + " - " + timeRange;
        }
        return timeRange;
    }

    private void createUIComponents() {
        regionTree = new JTree() {
            public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                if (value instanceof Region) {
                    // Region to string...
                    return regionToString((Region) value, false);
                } else if (value instanceof Jump) {
                    Jump j = (Jump) value;
                    return regionToString(j.target, true);
                    // Jump to string...
                } else {
                    if (value == null)
                        return "(null)";
                    return value.toString();
                }
            }
        };
        regionTree.setCellRenderer(new RegionTreeCellRenderer());
    }

    private void hookIdChanged(ChangeEvent e) {
        if (selectedJump != null)
            selectedJump.hookId = (Integer) hookSpinner.getValue();
    }

    private void fadeChanged(ChangeEvent e) {
        if (selectedJump != null)
            selectedJump.fadeDelay = (Integer) hookSpinner.getValue();
    }

    private void targetSelected(ItemEvent e) {
        if (selectedJump != null) {
            Region selected = (Region) targetSelector.getSelectedItem();
            if (selected != null)
                selectedJump.target = selected;
            regionTree.repaint();
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        createUIComponents();

        panel3 = new JSplitPane();
        scrollPane1 = new JScrollPane();
        propertyContainer = new JPanel();
        panel1 = new JPanel();
        panel5 = new JPanel();
        timeSlider = new JSlider();
        panel6 = new JPanel();
        totalTime = new JLabel();
        currentTime = new JLabel();
        panel2 = new JPanel();
        button1 = new JButton();
        button3 = new JButton();
        button2 = new JButton();
        jumpPropertyPanel = new JPanel();
        jumpEnabled = new JCheckBox();
        label1 = new JLabel();
        targetSelector = new JComboBox();
        label2 = new JLabel();
        hookSpinner = new JSpinner();
        label3 = new JLabel();
        fadeSpinner = new JSpinner();
        regionPropertyPanel = new JPanel();
        label4 = new JLabel();
        startLabel = new JLabel();
        label5 = new JLabel();
        durationLabel = new JLabel();
        label6 = new JLabel();
        scrollPane2 = new JScrollPane();
        commentArea = new JTextArea();
        panel4 = new JPanel();
        button4 = new JButton();
        panel7 = new JPanel();
        button5 = new JButton();
        soundPropertyPanel = new JPanel();
        name = new JLabel();
        label7 = new JLabel();
        sampleRate = new JLabel();
        label8 = new JLabel();
        numChannels = new JLabel();
        label9 = new JLabel();
        bitsPerSample = new JLabel();
        label10 = new JLabel();
        bitrate = new JLabel();
        panel8 = new JPanel();
        button6 = new JButton();
        button7 = new JButton();
        playAllAction = new PlayAllAction();
        stopAction = new StopAction();
        playSelectedRegion = new PlaySelectedRegion();
        toggleJumpAction = new ToggleJumpAction();
        action1 = new UpdateCommentsButton();
        exportRegionAction = new ExportRegionAction();
        exportWaveAction = new ExportWaveAction();
        exportDirectoryAction = new ExportDirectoryAction();

        //======== this ========
        setLayout(new BorderLayout());

        //======== panel3 ========
        {
            panel3.setResizeWeight(0.8);

            //======== scrollPane1 ========
            {

                //---- regionTree ----
                regionTree.setLargeModel(true);
                regionTree.addTreeSelectionListener(new TreeSelectionListener() {
                    @Override
                    public void valueChanged(TreeSelectionEvent e) {
                        regionTreeValueChanged(e);
                    }
                });
                scrollPane1.setViewportView(regionTree);
            }
            panel3.setLeftComponent(scrollPane1);

            //======== propertyContainer ========
            {
                propertyContainer.setBorder(new TitledBorder("Properties"));
                propertyContainer.setLayout(new BorderLayout());
            }
            panel3.setRightComponent(propertyContainer);
        }
        add(panel3, BorderLayout.CENTER);

        //======== panel1 ========
        {
            panel1.setLayout(new BorderLayout());

            //======== panel5 ========
            {
                panel5.setBorder(new TitledBorder("Time"));
                panel5.setLayout(new BorderLayout());

                //---- timeSlider ----
                timeSlider.setEnabled(false);
                timeSlider.setValue(0);
                panel5.add(timeSlider, BorderLayout.CENTER);

                //======== panel6 ========
                {
                    panel6.setLayout(new BorderLayout());
                    panel6.add(totalTime, BorderLayout.EAST);
                    panel6.add(currentTime, BorderLayout.WEST);
                }
                panel5.add(panel6, BorderLayout.SOUTH);
            }
            panel1.add(panel5, BorderLayout.CENTER);

            //======== panel2 ========
            {
                panel2.setLayout(new FlowLayout());

                //---- button1 ----
                button1.setAction(playAllAction);
                button1.setActionCommand("Play");
                panel2.add(button1);

                //---- button3 ----
                button3.setAction(playSelectedRegion);
                panel2.add(button3);

                //---- button2 ----
                button2.setAction(stopAction);
                panel2.add(button2);
            }
            panel1.add(panel2, BorderLayout.SOUTH);
        }
        add(panel1, BorderLayout.SOUTH);

        //======== jumpPropertyPanel ========
        {
            jumpPropertyPanel.setLayout(new GridBagLayout());
            ((GridBagLayout)jumpPropertyPanel.getLayout()).columnWidths = new int[] {0, 0, 0};
            ((GridBagLayout)jumpPropertyPanel.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0};
            ((GridBagLayout)jumpPropertyPanel.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
            ((GridBagLayout)jumpPropertyPanel.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0E-4};

            //---- jumpEnabled ----
            jumpEnabled.setAction(toggleJumpAction);
            jumpPropertyPanel.add(jumpEnabled, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
                new Insets(0, 0, 5, 0), 0, 0));

            //---- label1 ----
            label1.setText("Target: ");
            label1.setHorizontalAlignment(SwingConstants.TRAILING);
            jumpPropertyPanel.add(label1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 5), 0, 0));

            //---- targetSelector ----
            targetSelector.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    targetSelected(e);
                }
            });
            jumpPropertyPanel.add(targetSelector, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 0), 0, 0));

            //---- label2 ----
            label2.setText("Hook ID: ");
            label2.setHorizontalAlignment(SwingConstants.TRAILING);
            jumpPropertyPanel.add(label2, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 5), 0, 0));

            //---- hookSpinner ----
            hookSpinner.setModel(new SpinnerNumberModel(0, 0, null, 1));
            hookSpinner.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    hookIdChanged(e);
                }
            });
            jumpPropertyPanel.add(hookSpinner, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 0), 0, 0));

            //---- label3 ----
            label3.setText("Fade delay: ");
            label3.setHorizontalAlignment(SwingConstants.TRAILING);
            jumpPropertyPanel.add(label3, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 5), 0, 0));

            //---- fadeSpinner ----
            fadeSpinner.setModel(new SpinnerNumberModel(0, 0, null, 1));
            fadeSpinner.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    fadeChanged(e);
                }
            });
            jumpPropertyPanel.add(fadeSpinner, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        }

        //======== regionPropertyPanel ========
        {
            regionPropertyPanel.setLayout(new GridBagLayout());
            ((GridBagLayout)regionPropertyPanel.getLayout()).columnWidths = new int[] {0, 0, 0};
            ((GridBagLayout)regionPropertyPanel.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0};
            ((GridBagLayout)regionPropertyPanel.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
            ((GridBagLayout)regionPropertyPanel.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

            //---- label4 ----
            label4.setText("Start: ");
            label4.setHorizontalAlignment(SwingConstants.TRAILING);
            regionPropertyPanel.add(label4, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 5), 0, 0));

            //---- startLabel ----
            startLabel.setText("text");
            regionPropertyPanel.add(startLabel, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 0), 0, 0));

            //---- label5 ----
            label5.setText("Duration: ");
            label5.setHorizontalAlignment(SwingConstants.TRAILING);
            regionPropertyPanel.add(label5, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 5), 0, 0));

            //---- durationLabel ----
            durationLabel.setText("text");
            regionPropertyPanel.add(durationLabel, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 0), 0, 0));

            //---- label6 ----
            label6.setText("Comments: ");
            label6.setHorizontalAlignment(SwingConstants.TRAILING);
            regionPropertyPanel.add(label6, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 5, 5), 0, 0));

            //======== scrollPane2 ========
            {
                scrollPane2.setViewportView(commentArea);
            }
            regionPropertyPanel.add(scrollPane2, new GridBagConstraints(1, 2, 1, 1, 1.0, 1.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 0), 0, 0));

            //======== panel4 ========
            {
                panel4.setLayout(new FlowLayout());

                //---- button4 ----
                button4.setAction(action1);
                panel4.add(button4);
            }
            regionPropertyPanel.add(panel4, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 0), 0, 0));

            //======== panel7 ========
            {
                panel7.setLayout(new FlowLayout());

                //---- button5 ----
                button5.setAction(exportRegionAction);
                panel7.add(button5);
            }
            regionPropertyPanel.add(panel7, new GridBagConstraints(0, 4, 2, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        }

        //======== soundPropertyPanel ========
        {
            soundPropertyPanel.setLayout(new GridBagLayout());
            ((GridBagLayout)soundPropertyPanel.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
            ((GridBagLayout)soundPropertyPanel.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0};
            ((GridBagLayout)soundPropertyPanel.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
            ((GridBagLayout)soundPropertyPanel.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

            //---- name ----
            name.setText("text");
            name.setFont(new Font("Tahoma", Font.BOLD, 11));
            name.setHorizontalAlignment(SwingConstants.CENTER);
            soundPropertyPanel.add(name, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 5), 0, 0));

            //---- label7 ----
            label7.setText("Sample Rate: ");
            label7.setHorizontalAlignment(SwingConstants.TRAILING);
            soundPropertyPanel.add(label7, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 5), 0, 0));

            //---- sampleRate ----
            sampleRate.setText("text");
            soundPropertyPanel.add(sampleRate, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 5), 0, 0));

            //---- label8 ----
            label8.setText("Channels: ");
            label8.setHorizontalAlignment(SwingConstants.TRAILING);
            soundPropertyPanel.add(label8, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 5), 0, 0));

            //---- numChannels ----
            numChannels.setText("text");
            soundPropertyPanel.add(numChannels, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 5), 0, 0));

            //---- label9 ----
            label9.setText("Bits per sample: ");
            label9.setHorizontalAlignment(SwingConstants.TRAILING);
            soundPropertyPanel.add(label9, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 5), 0, 0));

            //---- bitsPerSample ----
            bitsPerSample.setText("text");
            soundPropertyPanel.add(bitsPerSample, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 5), 0, 0));

            //---- label10 ----
            label10.setText("Birate: ");
            label10.setHorizontalAlignment(SwingConstants.TRAILING);
            soundPropertyPanel.add(label10, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 5), 0, 0));

            //---- bitrate ----
            bitrate.setText("text");
            soundPropertyPanel.add(bitrate, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 5), 0, 0));

            //======== panel8 ========
            {
                panel8.setLayout(new FlowLayout());

                //---- button6 ----
                button6.setAction(exportWaveAction);
                panel8.add(button6);

                //---- button7 ----
                button7.setAction(exportDirectoryAction);
                panel8.add(button7);
            }
            soundPropertyPanel.add(panel8, new GridBagConstraints(0, 5, 2, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 5), 0, 0));
        }
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    public void onNewData() {
        regionTree.setModel(new TreeModel() {
            public Object getRoot() {
                return data;
            }

            public Object getChild(Object parent, int index) {
                if (parent == data)
                    return data.regions.get(index);
                else if (parent instanceof Region)
                    return ((Region) parent).jumps.get(index);
                return null;
            }

            public int getChildCount(Object parent) {
                if (parent == data)
                    return data.regions.size();
                else if (parent instanceof Region)
                    return ((Region) parent).jumps.size();
                return 0;
            }

            public boolean isLeaf(Object node) {
                return getChildCount(node) == 0;
            }

            public void valueForPathChanged(TreePath path, Object newValue) {
            }

            public int getIndexOfChild(Object parent, Object child) {
                if (parent == data)
                    return data.regions.indexOf(child);
                else if (parent instanceof Region)
                    return ((Region) parent).jumps.indexOf(child);
                return -1;
            }

            public void addTreeModelListener(TreeModelListener l) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public void removeTreeModelListener(TreeModelListener l) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });
        regionTree.setSelectionRow(0);
        for (int i = 0; i < regionTree.getRowCount(); i++)
            regionTree.expandRow(i);

        AudioFormat af = new AudioFormat(data.sampleRate, data.bits, data.channels, true, true);
        try {
            if (dataLine != null)
                dataLine.close();
            dataLine = (SourceDataLine) AudioSystem.getLine(new DataLine.Info(SourceDataLine.class, af, 49152)); // 2 second buffer to give us time to decode
            dataLine.open();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }

        targetSelector.setModel(new ComboBoxModel() {
            private Region selected;

            public void setSelectedItem(Object anItem) {
                if (!(anItem instanceof Region))
                    return;
                selected = (Region) anItem;
            }

            public Object getSelectedItem() {
                return data.regions.contains(selected) ? selected : null;
            }

            public int getSize() {
                return data.regions.size();
            }

            public Object getElementAt(int index) {
                return data.regions.get(index);
            }

            public void addListDataListener(ListDataListener l) {
            }

            public void removeListDataListener(ListDataListener l) {
            }
        });
    }

    public void onHide() {
        stop();
    }

    public void stop() {

        stopAction.setEnabled(false);
        _playing = false;
    }

    public void play(Region start) {
        int totalLength;
        totalLength = 0;
        for (Region r : data.regions)
            totalLength += r.length;


        timeSlider.setMaximum(totalLength);
        totalTime.setText(msToString(data.bytesToTime(totalLength)));

        _playing = true;
        playAllAction.setEnabled(false);
        playSelectedRegion.setEnabled(false);
        stopAction.setEnabled(true);
        _playRegion = start;
        new Thread(_play).start();
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JSplitPane panel3;
    private JScrollPane scrollPane1;
    private JTree regionTree;
    private JPanel propertyContainer;
    private JPanel panel1;
    private JPanel panel5;
    private JSlider timeSlider;
    private JPanel panel6;
    private JLabel totalTime;
    private JLabel currentTime;
    private JPanel panel2;
    private JButton button1;
    private JButton button3;
    private JButton button2;
    private JPanel jumpPropertyPanel;
    private JCheckBox jumpEnabled;
    private JLabel label1;
    private JComboBox targetSelector;
    private JLabel label2;
    private JSpinner hookSpinner;
    private JLabel label3;
    private JSpinner fadeSpinner;
    private JPanel regionPropertyPanel;
    private JLabel label4;
    private JLabel startLabel;
    private JLabel label5;
    private JLabel durationLabel;
    private JLabel label6;
    private JScrollPane scrollPane2;
    private JTextArea commentArea;
    private JPanel panel4;
    private JButton button4;
    private JPanel panel7;
    private JButton button5;
    private JPanel soundPropertyPanel;
    private JLabel name;
    private JLabel label7;
    private JLabel sampleRate;
    private JLabel label8;
    private JLabel numChannels;
    private JLabel label9;
    private JLabel bitsPerSample;
    private JLabel label10;
    private JLabel bitrate;
    private JPanel panel8;
    private JButton button6;
    private JButton button7;
    private PlayAllAction playAllAction;
    private StopAction stopAction;
    private PlaySelectedRegion playSelectedRegion;
    private ToggleJumpAction toggleJumpAction;
    private UpdateCommentsButton action1;
    private ExportRegionAction exportRegionAction;
    private ExportWaveAction exportWaveAction;
    private ExportDirectoryAction exportDirectoryAction;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    private class PlayAllAction extends AbstractAction {
        private PlayAllAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "Play");
            putValue(SHORT_DESCRIPTION, "Play all the regions");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            play(data.regions.get(0));
        }
    }

    private class StopAction extends AbstractAction {
        private StopAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "Stop");
            putValue(SHORT_DESCRIPTION, "Stop playing");
            setEnabled(false);
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            stop();
        }
    }

    private class PlaySelectedRegion extends AbstractAction {
        private PlaySelectedRegion() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "Play from selected");
            putValue(SHORT_DESCRIPTION, "Play from the selected region");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            play(selected);
        }
    }

    private class ToggleJumpAction extends AbstractAction {
        private ToggleJumpAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "Active");
            putValue(SHORT_DESCRIPTION, "Enables this jump when playing");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            if (selectedJump != null) {
                boolean enabled = jumpEnabled.isSelected();
                if (!enabled)
                    activeJumps.remove(selectedJump);
                else
                    activeJumps.put(selectedJump, true);
                regionTree.repaint();
            }
        }
    }

    private class UpdateCommentsButton extends AbstractAction {
        private UpdateCommentsButton() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "Update");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            if (selected == null)
                return;
            String comments = commentArea.getText();
            selected.comments = comments.isEmpty() ? null : comments;
            regionTree.repaint();
        }
    }

    private class RegionListCellRenderer implements ListCellRenderer<Region> {
        public Component getListCellRendererComponent(JList<? extends Region> list, Region value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = new JLabel(regionToString(value, true));
            if (isSelected) {
                label.setOpaque(true);
                label.setForeground(list.getSelectionForeground());
                label.setBackground(list.getSelectionBackground());
            } else {
                label.setForeground(list.getForeground());
                label.setBackground(list.getBackground());
            }
            return label;
        }
    }

    private class RegionTreeCellRenderer extends DefaultTreeCellRenderer {
        ImageIcon jumpIcon = new ImageIcon(getClass().getResource("/jump.png"));
        ImageIcon regionIcon = new ImageIcon(getClass().getResource("/musicnote.png"));
        ImageIcon playIcon = new ImageIcon(getClass().getResource("/play-green.png"));

        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            Component c = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            if (value instanceof Region) {
                if (_playRegion == value)
                    setIcon(playIcon);
                else
                    setIcon(regionIcon);
            } else if (value instanceof Jump) {
                Jump j = (Jump) value;
                Icon i = jumpIcon;
                if (!activeJumps.containsKey(j) || !activeJumps.get(j)) {
                    LookAndFeel laf = UIManager.getLookAndFeel();
                    i = laf.getDisabledIcon(tree, i);
                }
                setIcon(i);
            }
            return c;
        }
    }

    private class ExportRegionAction extends AbstractAction {
        private ExportRegionAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "Export region as WAVE");
            putValue(SHORT_DESCRIPTION, "exports this region as a wave file");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            final AtomicBoolean cancel = new AtomicBoolean(false);
            final JFileChooser jfc = window.createFileDialog();
            jfc.setFileFilter(new FileNameExtensionFilter("Wave file", "wav"));
            String name = data.getName();
            int idx = name.indexOf('.');
            if (idx != -1)
                name = name.substring(0, idx);
            name += ".wav";
            jfc.setSelectedFile(new File(name));
            if (jfc.showSaveDialog(window) != JFileChooser.APPROVE_OPTION)
                return;
            File f = jfc.getSelectedFile();
            String fn = f.getName();
            if(!fn.toLowerCase().endsWith(".wav"))
                fn += ".wav";
            final File dest = new File(f.getParentFile(), fn);
            window.runAsyncWithPopup("Exporting region...", new Runnable() {
                public void run() {
                    try {
                        if (stopAction.isEnabled())
                            stopAction.actionPerformed(null);
                        WaveOutputStream wos = new WaveOutputStream(dest, data.channels, data.sampleRate, data.bits);
                        Region selected = AudioEditor.this.selected;
                        byte[] buf = new byte[data.sampleRate];
                        int off = 0;
                        while (off < selected.length) {
                            final float ratio = ((float) off / (float) selected.length);
                            window.setBusyMessage(String.format("Exporting region (%.2f%%)", ratio * 100f));
                            data.stream.seek(selected.offset+off);
                            int read = data.stream.read(buf, 0, Math.min(buf.length, selected.length - off));
                            off += read;
                            wos.write(buf, 0, read);
                            if(cancel.get())
                                break;
                        }
                        wos.finish();
                        wos.close();
                        if(cancel.get())
                            dest.delete();
                    } catch (IOException e1) {
                        MainWindow.getInstance().handleException(e1);
                    }
                }
            }, true, new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    cancel.set(true);
                }
            });
        }
    }

    private class ExportWaveAction extends AbstractAction {
        private ExportWaveAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "Export as WAVE");
            putValue(SHORT_DESCRIPTION, "export the entire song as a WAVE file");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            final JFileChooser jfc = window.createFileDialog();
            final AtomicBoolean cancel = new AtomicBoolean(false);
            jfc.setFileFilter(new FileNameExtensionFilter("Wave file", "wav"));
            String name = data.getName();
            int idx = name.indexOf('.');
            if (idx != -1)
                name = name.substring(0, idx);
            name += ".wav";
            jfc.setSelectedFile(new File(name));
            if (jfc.showSaveDialog(window) != JFileChooser.APPROVE_OPTION)
                return;

            File f = jfc.getSelectedFile();
            String fn = f.getName();
            if(!fn.toLowerCase().endsWith(".wav"))
                fn += ".wav";
            final File dest = new File(f.getParentFile(), fn);

            window.runAsyncWithPopup("Exporting song...", new Runnable() {
                public void run() {
                    try {
                        if (stopAction.isEnabled())
                            stopAction.actionPerformed(null);
                        WaveOutputStream wos = new WaveOutputStream(dest, data.channels, data.sampleRate, data.bits);

                        byte[] buf = new byte[5000];
                        int len = 0;
                        for (Region region : data.regions)
                            len += region.length;
                        int globOff = 0;
                        mainLoop:
                        for (Region region : data.regions) {
                            int off = 0;
                            while (off < region.length) {
                                final float ratio = ((float) globOff / (float) len);
                                window.setBusyMessage(String.format("Exporting song (%.2f%%)", ratio * 100f));
                                data.stream.seek(region.offset+off);
                                int read = data.stream.read(buf, 0, Math.min(buf.length, region.length - off));
                                off += read;
                                globOff += read;
                                wos.write(buf, 0, read);
                                if(cancel.get())
                                    break mainLoop;
                            }
                        }
                        wos.finish();
                        wos.close();
                        if(cancel.get())
                            dest.delete();
                    } catch (IOException e1) {
                        MainWindow.getInstance().handleException(e1);
                    }
                }
            }, true, new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    cancel.set(true);
                }
            });
        }
    }

    private class ExportDirectoryAction extends AbstractAction {
        private ExportDirectoryAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "Export separated regions");
            putValue(SHORT_DESCRIPTION, "export all the regions to their own wave file in the specified directory");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            final JFileChooser jfc = window.createFileDialog();
            jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (jfc.showSaveDialog(window) == JFileChooser.APPROVE_OPTION) {
                window.runAsyncWithPopup("Exporting song...", new Runnable() {
                    public void run() {
                        try {
                            if (stopAction.isEnabled())
                                stopAction.actionPerformed(null);
                            File dir = jfc.getSelectedFile();
                            if (!dir.exists())
                                dir.mkdirs();

                            byte[] buf = new byte[data.sampleRate];
                            int len = 0;
                            for (Region region : data.regions)
                                len += region.length;
                            int globOff = 0;
                            for (Region region : data.regions) {
                                String regionName = "region " + data.regions.indexOf(region);
                                if (region.comments != null && !region.comments.isEmpty()) {
                                    String comPart = region.comments;
                                    int idx = comPart.indexOf('\n');
                                    if (idx != -1)
                                        comPart = comPart.substring(0, idx);
                                    regionName += " (" + comPart + ")";
                                }
                                regionName += ".wav";

                                WaveOutputStream wos = new WaveOutputStream(new File(dir, regionName), data.channels, data.sampleRate, data.bits);
                                int off = 0;
                                while (off < region.length) {
                                    final float ratio = ((float) globOff / (float) len);
                                    window.setBusyMessage(String.format("Exporting song (%.2f%%)", ratio * 100f));
                                    data.stream.seek(region.offset+off);
                                    int read = data.stream.read(buf, 0, Math.min(buf.length, region.length - off));
                                    off += read;
                                    globOff += read;
                                    wos.write(buf, 0, read);
                                }
                                wos.finish();
                                wos.close();
                            }
                        } catch (IOException e1) {
                            MainWindow.getInstance().handleException(e1);
                        }
                    }
                });
            }
        }
    }
}

class WaveOutputStream {
    private final int channels, sampleRate, bits, bytes;
    private RandomAccessFile dest;

    private int writtenBytes;

    public WaveOutputStream(File f, int channels, int sampleRate, int bits) throws IOException {
        dest = new RandomAccessFile(f, "rw");
        this.channels = channels;
        this.sampleRate = sampleRate;
        this.bits = bits;
        this.bytes = bits / 8;
        dest.writeInt(('R' << 24) | ('I' << 16) | ('F' << 8) | 'F');
        dest.skipBytes(4);

        dest.writeInt(('W' << 24) | ('A' << 16) | ('V' << 8) | 'E');
        // fmt chunk...
        dest.writeInt(('f' << 24) | ('m' << 16) | ('t' << 8) | ' ');
        dest.writeInt(reverse32(16)); // 16 bytes...
        dest.writeShort(reverse16(1)); // pcm
        dest.writeShort(reverse16(channels));
        dest.writeInt(reverse32(sampleRate));
        dest.writeInt(reverse32(sampleRate * channels * (bits / 8)));
        dest.writeShort(reverse16(channels * (bits / 8)));
        dest.writeShort(reverse16(bits));

        dest.writeInt(('d' << 24) | ('a' << 16) | ('t' << 8) | 'a');
        dest.skipBytes(4);
    }

    public void write(byte[] data, int off, final int len) throws IOException {
        final int end = off + len;
        switch (bits) {
            case 16:
                for (; off < end; off += bytes) {
                    byte b = data[off];
                    byte b1 = data[off + 1];
                    dest.write(b1);
                    dest.write(b);
                }
                break;
            case 32:
                for (; off < end; off += bytes) {
                    byte b = data[off];
                    byte b1 = data[off + 1];
                    byte b2 = data[off + 2];
                    byte b3 = data[off + 3];
                    dest.write(b3);
                    dest.write(b2);
                    dest.write(b1);
                    dest.write(b);
                }
            default:
                throw new IOException("Unhandled bitlength: " + bits);
        }
        writtenBytes += len;
    }

    public void finish() throws IOException {
        long pos = dest.getFilePointer();
        dest.seek(4);
        dest.writeInt(writtenBytes + 36);
        dest.seek(40);
        dest.writeInt(reverse32(writtenBytes));
        dest.seek(pos);
    }

    public void close() throws IOException {
        finish();
        dest.close();
    }

    private ByteBuffer buf = ByteBuffer.allocate(4);

    private int reverse16(int i) {
        buf.order(ByteOrder.BIG_ENDIAN);
        buf.putShort(0, (short) i);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        return buf.getShort(0) & 0xffff;
    }

    private int reverse32(int i) {
        buf.order(ByteOrder.BIG_ENDIAN);
        buf.putInt(0, i);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        return buf.getInt(0);
    }
}