/*
 * Created by JFormDesigner on Sun Mar 24 18:57:33 GMT 2013
 */

package com.sqrt4.grimedi.ui.editor;

import com.sqrt.liblab.entry.audio.Audio;
import com.sqrt.liblab.entry.audio.Jump;
import com.sqrt.liblab.entry.audio.Region;

import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;

/**
 * @author James Lawrence
 */
public class AudioEditor extends EditorPanel<Audio> {
    private boolean _playing;
    private Region _playRegion;
    private Region selected;
    private Jump selectedJump;
    private Map<Jump, Boolean> activeJumps = new HashMap<Jump, Boolean>();
    private SourceDataLine dataLine;
    private int _currentOffset, _syncOffset;
    private Runnable _play = new Runnable() {
        public void run() {
            dataLine.start();
            int len = dataLine.getBufferSize(); // how much we write per update loop...
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
                        _syncOffset = (int) ((long) _currentOffset - dataLine.getLongFramePosition());
                        _currentOffset += read;
                        updateTimeDisplay();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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

    ImageIcon icon = new ImageIcon(getClass().getResource("/speaker.png"));

    public ImageIcon getIcon() {
        return icon;
    }

    private void updateTimeDisplay() {
        timeSlider.setValue(_currentOffset+_syncOffset);
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
        name = new JLabel();
        label4 = new JLabel();
        startLabel = new JLabel();
        label5 = new JLabel();
        durationLabel = new JLabel();
        label6 = new JLabel();
        scrollPane2 = new JScrollPane();
        commentArea = new JTextArea();
        panel4 = new JPanel();
        button4 = new JButton();
        soundPropertyPanel = new JPanel();
        label7 = new JLabel();
        sampleRate = new JLabel();
        label8 = new JLabel();
        numChannels = new JLabel();
        label9 = new JLabel();
        bitsPerSample = new JLabel();
        label10 = new JLabel();
        bitrate = new JLabel();
        playAllAction = new PlayAllAction();
        stopAction = new StopAction();
        playSelectedRegion = new PlaySelectedRegion();
        toggleJumpAction = new ToggleJumpAction();
        action1 = new UpdateCommentsButton();

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
        add(panel1, BorderLayout.PAGE_END);

        //======== jumpPropertyPanel ========
        {
            jumpPropertyPanel.setLayout(new GridBagLayout());
            ((GridBagLayout) jumpPropertyPanel.getLayout()).columnWidths = new int[]{0, 0, 0};
            ((GridBagLayout) jumpPropertyPanel.getLayout()).rowHeights = new int[]{0, 0, 0, 0, 0};
            ((GridBagLayout) jumpPropertyPanel.getLayout()).columnWeights = new double[]{0.0, 0.0, 1.0E-4};
            ((GridBagLayout) jumpPropertyPanel.getLayout()).rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0E-4};

            //---- jumpEnabled ----
            jumpEnabled.setAction(toggleJumpAction);
            jumpPropertyPanel.add(jumpEnabled, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
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
            jumpPropertyPanel.add(targetSelector, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
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
            ((GridBagLayout) regionPropertyPanel.getLayout()).columnWidths = new int[]{0, 0, 0};
            ((GridBagLayout) regionPropertyPanel.getLayout()).rowHeights = new int[]{0, 0, 0, 0, 0, 0};
            ((GridBagLayout) regionPropertyPanel.getLayout()).columnWeights = new double[]{0.0, 0.0, 1.0E-4};
            ((GridBagLayout) regionPropertyPanel.getLayout()).rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

            //---- name ----
            name.setText("text");
            name.setFont(new Font("Tahoma", Font.BOLD, 11));
            name.setHorizontalAlignment(SwingConstants.CENTER);
            regionPropertyPanel.add(name, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

            //---- label4 ----
            label4.setText("Start: ");
            label4.setHorizontalAlignment(SwingConstants.TRAILING);
            regionPropertyPanel.add(label4, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

            //---- startLabel ----
            startLabel.setText("text");
            regionPropertyPanel.add(startLabel, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

            //---- label5 ----
            label5.setText("Duration: ");
            label5.setHorizontalAlignment(SwingConstants.TRAILING);
            regionPropertyPanel.add(label5, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

            //---- durationLabel ----
            durationLabel.setText("text");
            regionPropertyPanel.add(durationLabel, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

            //---- label6 ----
            label6.setText("Comments: ");
            label6.setHorizontalAlignment(SwingConstants.TRAILING);
            regionPropertyPanel.add(label6, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                    GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
                    new Insets(0, 0, 5, 5), 0, 0));

            //======== scrollPane2 ========
            {
                scrollPane2.setViewportView(commentArea);
            }
            regionPropertyPanel.add(scrollPane2, new GridBagConstraints(1, 3, 1, 1, 1.0, 1.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

            //======== panel4 ========
            {
                panel4.setLayout(new FlowLayout());

                //---- button4 ----
                button4.setAction(action1);
                panel4.add(button4);
            }
            regionPropertyPanel.add(panel4, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
        }

        //======== soundPropertyPanel ========
        {
            soundPropertyPanel.setLayout(new GridBagLayout());
            ((GridBagLayout) soundPropertyPanel.getLayout()).columnWidths = new int[]{0, 0, 0};
            ((GridBagLayout) soundPropertyPanel.getLayout()).rowHeights = new int[]{0, 0, 0, 0, 0};
            ((GridBagLayout) soundPropertyPanel.getLayout()).columnWeights = new double[]{0.0, 0.0, 1.0E-4};
            ((GridBagLayout) soundPropertyPanel.getLayout()).rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0E-4};

            //---- label7 ----
            label7.setText("Sample Rate: ");
            label7.setHorizontalAlignment(SwingConstants.TRAILING);
            soundPropertyPanel.add(label7, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

            //---- sampleRate ----
            sampleRate.setText("text");
            soundPropertyPanel.add(sampleRate, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

            //---- label8 ----
            label8.setText("Channels: ");
            label8.setHorizontalAlignment(SwingConstants.TRAILING);
            soundPropertyPanel.add(label8, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

            //---- numChannels ----
            numChannels.setText("text");
            soundPropertyPanel.add(numChannels, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

            //---- label9 ----
            label9.setText("Bits per sample: ");
            label9.setHorizontalAlignment(SwingConstants.TRAILING);
            soundPropertyPanel.add(label9, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

            //---- bitsPerSample ----
            bitsPerSample.setText("text");
            soundPropertyPanel.add(bitsPerSample, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

            //---- label10 ----
            label10.setText("Birate: ");
            label10.setHorizontalAlignment(SwingConstants.TRAILING);
            soundPropertyPanel.add(label10, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 5), 0, 0));

            //---- bitrate ----
            bitrate.setText("text");
            soundPropertyPanel.add(bitrate, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
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
    private JLabel name;
    private JLabel label4;
    private JLabel startLabel;
    private JLabel label5;
    private JLabel durationLabel;
    private JLabel label6;
    private JScrollPane scrollPane2;
    private JTextArea commentArea;
    private JPanel panel4;
    private JButton button4;
    private JPanel soundPropertyPanel;
    private JLabel label7;
    private JLabel sampleRate;
    private JLabel label8;
    private JLabel numChannels;
    private JLabel label9;
    private JLabel bitsPerSample;
    private JLabel label10;
    private JLabel bitrate;
    private PlayAllAction playAllAction;
    private StopAction stopAction;
    private PlaySelectedRegion playSelectedRegion;
    private ToggleJumpAction toggleJumpAction;
    private UpdateCommentsButton action1;
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
        ImageIcon regionIcon = new ImageIcon(getClass().getResource("/speaker.png"));
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
}