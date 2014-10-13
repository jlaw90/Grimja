

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
 * Created by JFormDesigner on Mon Mar 18 20:01:59 GMT 2013
 */

package com.sqrt4.grimedi.ui.editor;

import com.sqrt.liblab.codec.CodecMapper;
import com.sqrt.liblab.codec.ModelCodec;
import com.sqrt.liblab.entry.model.GrimModel;
import com.sqrt.liblab.entry.model.ModelNode;
import com.sqrt.liblab.threed.Angle;
import com.sqrt4.grimedi.ui.MainWindow;
import com.sqrt4.grimedi.ui.component.AngleEditor;
import com.sqrt4.grimedi.ui.component.ModelRenderer;
import com.sqrt4.grimedi.ui.component.Vector3Editor;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

/**
 * @author James Lawrence
 */
public class ModelView extends EditorPanel<GrimModel> {
    private boolean bonesUpdating;
    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JSplitPane panel2;
    private ModelRenderer renderer;
    private JPanel splitPane1;
    private JScrollPane scrollPane1;
    private JTree boneTree;
    private JPanel panel6;
    private Vector3Editor bonePos;
    private Vector3Editor bonePivot;
    private JPanel panel3;
    private AngleEditor boneYaw;
    private AngleEditor bonePitch;
    private AngleEditor boneRoll;
    private JPanel panel1;
    private JButton button1;
    private ExportObjAction exportObjAction;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    public ModelView() {
        initComponents();
    }

    private void boneSelected(TreeSelectionEvent e) {
        bonesUpdating = true;
        if (boneTree.getSelectionPath() == null) {
            renderer.setSelectedNode(null);
        } else {
            ModelNode selected = (ModelNode) boneTree.getSelectionPath().getLastPathComponent();
            renderer.setSelectedNode(selected);
            boneYaw.setAngle(selected.yaw.degrees);
            bonePitch.setAngle(selected.pitch.degrees);
            boneRoll.setAngle(selected.roll.degrees);
            bonePos.setValue(selected.pos);
            bonePivot.setValue(selected.pivot);
        }

        boolean editable = renderer.getSelectedNode() != null;
        boneYaw.setEnabled(editable);
        boneRoll.setEnabled(editable);
        bonePitch.setEnabled(editable);
        bonePos.setEnabled(editable);
        bonePivot.setEnabled(editable);
        bonesUpdating = false;
    }

    private void boneChanged() {
        ModelNode selected = renderer.getSelectedNode();
        if (selected == null || bonesUpdating)
            return;
        selected.yaw = new Angle(boneYaw.getAngle());
        selected.roll = new Angle(boneRoll.getAngle());
        selected.pitch = new Angle(bonePitch.getAngle());
        selected.pos = bonePos.getValue();
        selected.pivot = bonePivot.getValue();
        renderer.refreshModelCache();
    }

    private void boneChanged(PropertyChangeEvent e) {
        boneChanged();
    }

    private void boneChanged(ChangeEvent e) {
        boneChanged();
    }

    ImageIcon icon = new ImageIcon(getClass().getResource("/3do.png"));
    public ImageIcon getIcon() {
        return icon;
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        panel2 = new JSplitPane();
        renderer = new ModelRenderer();
        splitPane1 = new JPanel();
        scrollPane1 = new JScrollPane();
        boneTree = new JTree();
        panel6 = new JPanel();
        bonePos = new Vector3Editor();
        bonePivot = new Vector3Editor();
        panel3 = new JPanel();
        boneYaw = new AngleEditor();
        bonePitch = new AngleEditor();
        boneRoll = new AngleEditor();
        panel1 = new JPanel();
        button1 = new JButton();
        exportObjAction = new ExportObjAction();

        //======== this ========
        setLayout(new BorderLayout());

        //======== panel2 ========
        {
            panel2.setResizeWeight(1.0);
            panel2.setOneTouchExpandable(true);
            panel2.setLeftComponent(renderer);

            //======== splitPane1 ========
            {
                splitPane1.setLayout(new BorderLayout());

                //======== scrollPane1 ========
                {

                    //---- boneTree ----
                    boneTree.addTreeSelectionListener(new TreeSelectionListener() {
                        @Override
                        public void valueChanged(TreeSelectionEvent e) {
                            boneSelected(e);
                        }
                    });
                    scrollPane1.setViewportView(boneTree);
                }
                splitPane1.add(scrollPane1, BorderLayout.CENTER);

                //======== panel6 ========
                {
                    panel6.setBorder(new TitledBorder("Bone Properties"));
                    panel6.setLayout(new BoxLayout(panel6, BoxLayout.Y_AXIS));

                    //---- bonePos ----
                    bonePos.setEnabled(false);
                    bonePos.setBorder(new TitledBorder("Position"));
                    bonePos.addChangeListener(new ChangeListener() {
                        @Override
                        public void stateChanged(ChangeEvent e) {
                            boneChanged(e);
                        }
                    });
                    panel6.add(bonePos);

                    //---- bonePivot ----
                    bonePivot.setEnabled(false);
                    bonePivot.setBorder(new TitledBorder("Pivot"));
                    bonePivot.addChangeListener(new ChangeListener() {
                        @Override
                        public void stateChanged(ChangeEvent e) {
                            boneChanged(e);
                        }
                    });
                    panel6.add(bonePivot);

                    //======== panel3 ========
                    {
                        panel3.setLayout(new BoxLayout(panel3, BoxLayout.X_AXIS));

                        //---- boneYaw ----
                        boneYaw.setEnabled(false);
                        boneYaw.setBorder(new TitledBorder("Yaw"));
                        boneYaw.addPropertyChangeListener(new PropertyChangeListener() {
                            @Override
                            public void propertyChange(PropertyChangeEvent e) {
                                boneChanged(e);
                            }
                        });
                        panel3.add(boneYaw);

                        //---- bonePitch ----
                        bonePitch.setEnabled(false);
                        bonePitch.setBorder(new TitledBorder("Pitch"));
                        bonePitch.addPropertyChangeListener(new PropertyChangeListener() {
                            @Override
                            public void propertyChange(PropertyChangeEvent e) {
                                boneChanged(e);
                            }
                        });
                        panel3.add(bonePitch);

                        //---- boneRoll ----
                        boneRoll.setEnabled(false);
                        boneRoll.setBorder(new TitledBorder("Roll"));
                        boneRoll.addPropertyChangeListener(new PropertyChangeListener() {
                            @Override
                            public void propertyChange(PropertyChangeEvent e) {
                                boneChanged(e);
                            }
                        });
                        panel3.add(boneRoll);
                    }
                    panel6.add(panel3);

                    //======== panel1 ========
                    {
                        panel1.setLayout(new BorderLayout());

                        //---- button1 ----
                        button1.setAction(exportObjAction);
                        panel1.add(button1, BorderLayout.CENTER);
                    }
                    panel6.add(panel1);
                }
                splitPane1.add(panel6, BorderLayout.SOUTH);
            }
            panel2.setRightComponent(splitPane1);
        }
        add(panel2, BorderLayout.CENTER);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    public void onNewData() {
        renderer.setModel(data);
        DefaultTreeCellRenderer r = (DefaultTreeCellRenderer) boneTree.getCellRenderer();
        r.setOpenIcon(r.getLeafIcon());
        r.setClosedIcon(r.getLeafIcon());
        boneTree.setModel(new TreeModel() {
            public Object getRoot() {
                return data.hierarchy.get(0);
            }

            public Object getChild(Object parent, int index) {
                ModelNode child = ((ModelNode) parent).child;
                for (int i = 0; i < index; i++)
                    child = child.sibling;
                return child;
            }

            public int getChildCount(Object parent) {
                ModelNode child = ((ModelNode) parent).child;
                int count = 0;
                while (child != null) {
                    count++;
                    child = child.sibling;
                }
                return count;
            }

            public boolean isLeaf(Object node) {
                return getChildCount(node) == 0;
            }

            public void valueForPathChanged(TreePath path, Object newValue) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public int getIndexOfChild(Object parent, Object child) {
                ModelNode c = ((ModelNode) parent).child;
                int idx = 0;
                while (c != null) {
                    if (c == child)
                        return idx;
                    idx++;
                    c = c.sibling;
                }
                return -1;
            }

            public void addTreeModelListener(TreeModelListener l) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public void removeTreeModelListener(TreeModelListener l) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });
        for (int i = 0; i < boneTree.getRowCount(); i++)
            boneTree.expandRow(i);
    }

    private class ExportObjAction extends AbstractAction {
        private ExportObjAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "Export OBJ");
            putValue(SHORT_DESCRIPTION, "Export wavefront obj file");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            JFileChooser jfc = MainWindow.getInstance().createFileDialog();
            jfc.setFileFilter(new FileNameExtensionFilter("Wavefront OBJ (*.obj)", "obj"));
            String name = data.getName();
            int idx = name.lastIndexOf('.');
            if(idx != -1)
                name = name.substring(0, idx);
            name += ".obj";
            jfc.setSelectedFile(new File(name));
            if(jfc.showSaveDialog(ModelView.this) != JFileChooser.APPROVE_OPTION)
                return;
            File f = jfc.getSelectedFile();
            name = f.getName();
            if(!name.toLowerCase().endsWith(".obj"))
                name += ".obj";
            f = new File(f.getParentFile(), name);
            try {
                ((ModelCodec) CodecMapper.codecForClass(GrimModel.class)).exportWavefront(f, renderer.getModel(), renderer.getColorMap());
            } catch (IOException e1) {
                MainWindow.getInstance().handleException(e1);
            }
        }
    }
}
