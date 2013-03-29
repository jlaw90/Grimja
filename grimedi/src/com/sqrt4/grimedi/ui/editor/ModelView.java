/*
 * Created by JFormDesigner on Mon Mar 18 20:01:59 GMT 2013
 */

package com.sqrt4.grimedi.ui.editor;

import com.sqrt.liblab.entry.model.GrimModel;
import com.sqrt.liblab.entry.model.ModelNode;
import com.sqrt.liblab.threed.Angle;
import com.sqrt4.grimedi.ui.component.AngleEditor;
import com.sqrt4.grimedi.ui.component.ModelRenderer;
import com.sqrt4.grimedi.ui.component.Vector3Editor;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author James Lawrence
 */
public class ModelView extends EditorPanel<GrimModel> {
    private boolean bonesUpdating;
    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JPanel panel3;
    private JSplitPane panel1;
    private ModelRenderer renderer;
    private JPanel panel5;
    private JSplitPane splitPane1;
    private JScrollPane scrollPane1;
    private JTree boneTree;
    private JPanel panel6;
    private JLabel label5;
    private Vector3Editor bonePos;
    private JLabel label6;
    private Vector3Editor bonePivot;
    private JLabel label2;
    private AngleEditor boneYaw;
    private JLabel label3;
    private AngleEditor bonePitch;
    private JLabel label4;
    private AngleEditor boneRoll;
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
            boneYaw.setAngle(selected.yaw);
            bonePitch.setAngle(selected.pitch);
            boneRoll.setAngle(selected.roll);
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
        ;

    }

    private void boneChanged(ChangeEvent e) {
        boneChanged();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        panel3 = new JPanel();
        panel1 = new JSplitPane();
        renderer = new ModelRenderer();
        panel5 = new JPanel();
        splitPane1 = new JSplitPane();
        scrollPane1 = new JScrollPane();
        boneTree = new JTree();
        panel6 = new JPanel();
        label5 = new JLabel();
        bonePos = new Vector3Editor();
        label6 = new JLabel();
        bonePivot = new Vector3Editor();
        label2 = new JLabel();
        boneYaw = new AngleEditor();
        label3 = new JLabel();
        bonePitch = new AngleEditor();
        label4 = new JLabel();
        boneRoll = new AngleEditor();

        //======== this ========
        setLayout(new BorderLayout());

        //======== panel3 ========
        {
            panel3.setLayout(new BorderLayout());

            //======== panel1 ========
            {
                panel1.setResizeWeight(0.8);
                panel1.setLeftComponent(renderer);

                //======== panel5 ========
                {
                    panel5.setLayout(new BorderLayout());

                    //======== splitPane1 ========
                    {
                        splitPane1.setOrientation(JSplitPane.VERTICAL_SPLIT);
                        splitPane1.setResizeWeight(1.0);

                        //======== scrollPane1 ========
                        {

                            //---- boneTree ----
                            boneTree.setBorder(new TitledBorder("Bones"));
                            boneTree.addTreeSelectionListener(new TreeSelectionListener() {
                                @Override
                                public void valueChanged(TreeSelectionEvent e) {
                                    boneSelected(e);
                                }
                            });
                            scrollPane1.setViewportView(boneTree);
                        }
                        splitPane1.setTopComponent(scrollPane1);

                        //======== panel6 ========
                        {
                            panel6.setBorder(new TitledBorder("Bone Properties"));
                            panel6.setLayout(new GridBagLayout());
                            ((GridBagLayout) panel6.getLayout()).columnWidths = new int[]{0, 0, 0};
                            ((GridBagLayout) panel6.getLayout()).rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
                            ((GridBagLayout) panel6.getLayout()).columnWeights = new double[]{1.0, 0.0, 1.0E-4};
                            ((GridBagLayout) panel6.getLayout()).rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

                            //---- label5 ----
                            label5.setText("Position: ");
                            label5.setHorizontalAlignment(SwingConstants.TRAILING);
                            panel6.add(label5, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                                    new Insets(0, 0, 0, 0), 0, 0));

                            //---- bonePos ----
                            bonePos.setEnabled(false);
                            bonePos.addChangeListener(new ChangeListener() {
                                @Override
                                public void stateChanged(ChangeEvent e) {
                                    boneChanged(e);
                                }
                            });
                            panel6.add(bonePos, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                                    new Insets(0, 0, 0, 0), 0, 0));

                            //---- label6 ----
                            label6.setText("Pivot: ");
                            label6.setHorizontalAlignment(SwingConstants.TRAILING);
                            panel6.add(label6, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                                    new Insets(0, 0, 0, 0), 0, 0));

                            //---- bonePivot ----
                            bonePivot.setEnabled(false);
                            bonePivot.addChangeListener(new ChangeListener() {
                                @Override
                                public void stateChanged(ChangeEvent e) {
                                    boneChanged(e);
                                }
                            });
                            panel6.add(bonePivot, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                                    new Insets(0, 0, 0, 0), 0, 0));

                            //---- label2 ----
                            label2.setText("Yaw: ");
                            label2.setLabelFor(boneYaw);
                            label2.setHorizontalAlignment(SwingConstants.TRAILING);
                            panel6.add(label2, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                                    new Insets(0, 0, 0, 0), 0, 0));

                            //---- boneYaw ----
                            boneYaw.setEnabled(false);
                            boneYaw.addPropertyChangeListener(new PropertyChangeListener() {
                                @Override
                                public void propertyChange(PropertyChangeEvent e) {
                                    boneChanged(e);
                                }
                            });
                            panel6.add(boneYaw, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0,
                                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                                    new Insets(0, 0, 0, 0), 0, 0));

                            //---- label3 ----
                            label3.setText("Pitch: ");
                            label3.setLabelFor(bonePitch);
                            label3.setHorizontalAlignment(SwingConstants.TRAILING);
                            panel6.add(label3, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                                    new Insets(0, 0, 0, 0), 0, 0));

                            //---- bonePitch ----
                            bonePitch.setEnabled(false);
                            bonePitch.addPropertyChangeListener(new PropertyChangeListener() {
                                @Override
                                public void propertyChange(PropertyChangeEvent e) {
                                    boneChanged(e);
                                }
                            });
                            panel6.add(bonePitch, new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0,
                                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                                    new Insets(0, 0, 0, 0), 0, 0));

                            //---- label4 ----
                            label4.setText("Roll: ");
                            label4.setHorizontalAlignment(SwingConstants.TRAILING);
                            label4.setLabelFor(boneRoll);
                            panel6.add(label4, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
                                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                                    new Insets(0, 0, 0, 0), 0, 0));

                            //---- boneRoll ----
                            boneRoll.setEnabled(false);
                            boneRoll.addPropertyChangeListener(new PropertyChangeListener() {
                                @Override
                                public void propertyChange(PropertyChangeEvent e) {
                                    boneChanged(e);
                                }
                            });
                            panel6.add(boneRoll, new GridBagConstraints(1, 4, 1, 1, 1.0, 0.0,
                                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                                    new Insets(0, 0, 0, 0), 0, 0));
                        }
                        splitPane1.setBottomComponent(panel6);
                    }
                    panel5.add(splitPane1, BorderLayout.CENTER);
                }
                panel1.setRightComponent(panel5);
            }
            panel3.add(panel1, BorderLayout.CENTER);
        }
        add(panel3, BorderLayout.CENTER);
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
}
