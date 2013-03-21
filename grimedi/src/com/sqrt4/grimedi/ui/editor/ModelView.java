/*
 * Created by JFormDesigner on Mon Mar 18 20:01:59 GMT 2013
 */

package com.sqrt4.grimedi.ui.editor;

import java.awt.event.*;
import javax.media.opengl.*;
import javax.media.opengl.awt.*;
import javax.swing.border.*;

import com.sqrt.liblab.model.*;

import javax.media.opengl.glu.GLU;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.sqrt4.grimedi.ui.component.*;
import jogamp.graph.math.MathFloat;

/**
 * @author James Lawrence
 */
public class ModelView extends EditorPanel<GrimModel> {
    private List<Texture> textures = new LinkedList<Texture>();
    private boolean _regenerateTextures;
    float rot = 0f;
    private boolean _drawTextures = false, _drawWireframe = true;
    private Vector3f target;
    private float camDistance = 1f, theta, phi;
    private boolean mouseUpdate;
    private Lock mouseLock = new ReentrantLock();
    private int oldX, oldY, rX, rY;
    private GLU glu = new GLU();

    public ModelView() {
        initComponents();
        _drawTextures = toggleTextures.isSelected();
        _drawWireframe = toggleWireframe.isSelected();
    }

    private void colorMapSelected(ChangeEvent e) {
        _regenerateTextures = true;
    }

    private void createUIComponents() {
        colorMapSelector = new ColorMapSelector();
        GLCapabilities cap = new GLCapabilities(GLProfile.getGL2ES1());
        cap.setDepthBits(24);
        preview = new GLJPanel(cap);
        Thread t = new Thread() {
            public void run() {
                final int targetFps = 25; // Should be fairly decent...
                final int frameTime = 1000 / targetFps;
                final int minDelta = 20;
                while (true) {
                    try {
                        long start = System.currentTimeMillis();
                        if (preview.isVisible()) {
                            preview.display();
                        }
                        long end = System.currentTimeMillis();
                        int delta = (int) (end - start); // milliseconds taken
                        int sleep = Math.min(frameTime - delta, minDelta); // milliseconds to sleep
                        Thread.sleep(sleep);
                    } catch (Exception e) {
                    /**/
                    }
                }
            }
        };
        t.setDaemon(true);
        t.setPriority(1);
        t.start();
        preview.addGLEventListener(new GLEventListener() {
            private IntBuffer viewport = IntBuffer.allocate(4);
            private FloatBuffer modelview = FloatBuffer.allocate(16);
            private FloatBuffer projview = FloatBuffer.allocate(16);
            private FloatBuffer zBuf = FloatBuffer.allocate(1);
            private FloatBuffer result = FloatBuffer.allocate(4);

            public void init(GLAutoDrawable glAutoDrawable) {
                // Todo: disable debugging when it all works :)
                glAutoDrawable.setGL(new DebugGL2(glAutoDrawable.getGL().getGL2()));
                rot = 0;
                textures.clear();
                _regenerateTextures = true;
                GL2 gl = glAutoDrawable.getGL().getGL2();
                gl.glEnable(GL.GL_DEPTH_TEST);
                gl.glEnable(GL2.GL_LIGHTING);
                gl.glEnable(GL2.GL_LIGHT0);
                //gl.glEnable(GL2.GL_CULL_FACE);
                gl.glDepthFunc(GL2.GL_LEQUAL);
                gl.glColorMaterial(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE);
                gl.glEnable(GL2.GL_COLOR_MATERIAL);
            }

            public void dispose(GLAutoDrawable glAutoDrawable) {
            }

            private FloatBuffer _camPosBuf = FloatBuffer.allocate(4)
                    ,
                    _spotDirBuf = FloatBuffer.allocate(4);

            public void display(GLAutoDrawable glAutoDrawable) {
                Vector3f rot = new Vector3f(camDistance * MathFloat.cos(theta),
                        camDistance * MathFloat.cos(phi) * MathFloat.sin(theta),
                        camDistance * MathFloat.sin(phi) * MathFloat.sin(theta));
                //rot = new Vector3f(0, 1, 0);
                Vector3f cam = target.add(rot);
                GL2 gl2 = glAutoDrawable.getGL().getGL2();
                gl2.glLoadIdentity();
                if (mouseUpdate && mouseLock.tryLock()) {
                    Vector3f orig = toWorld(gl2, oldX, oldY);
                    Vector3f n = toWorld(gl2, oldX+rX, oldY+rY);
                    Vector3f delta = n.sub(orig);
                    theta += delta.x;
                    phi += delta.y;
                    // Todo: clamp..
                    rX = rY = 0;
                    mouseUpdate = false;
                    mouseLock.unlock();
                }
                _camPosBuf.position(0);
                _spotDirBuf.position(0);
                //gl2.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, _camPosBuf.put(camPos.x).put(camPos.y).put(camPos.z).put(0f));
                //gl2.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPOT_DIRECTION, _spotDirBuf.put(camDir.x).put(camDir.y).put(camDir.z).put(0f));
                glu.gluLookAt(cam.x, cam.y, cam.z, target.x, target.y, target.z, 0, 0, 1);
                gl2.glPushMatrix();
                gl2.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
                for (ModelNode node : data.hierarchy)
                    renderNode(gl2, node);
                gl2.glPopMatrix();
                if (_drawTextures)
                    _regenerateTextures = false;
            }

            private void renderNode(GL2 gl2, ModelNode node) {
                if (node.mesh == null)
                    return;
                ModelNode selected = null;
                if (boneTree.getSelectionPath() != null)
                    selected = (ModelNode) boneTree.getSelectionPath().getLastPathComponent();
                boolean isSelected = selected == node;

                // Render
                gl2.glPushMatrix();
                for (ModelNode n = node; n != null; n = n.parent) {
                    gl2.glTranslatef(n.pos.x, n.pos.y, n.pos.z);
                    gl2.glRotatef((float) Math.toRadians(n.roll), 0.0f, 0.0f, 1.0f);
                    gl2.glRotatef((float) Math.toRadians(n.yaw), 0.0f, 1.0f, 0.0f);
                    gl2.glRotatef((float) Math.toRadians(n.pitch), 1.0f, 0.0f, 0.0f);
                }

                gl2.glTranslatef(node.pivot.x, node.pivot.y, node.pivot.z);
                gl2.glAlphaFunc(GL.GL_GREATER, 0.5f);
                gl2.glEnable(GL2.GL_ALPHA_TEST);
                for (MeshFace face : node.mesh.faces) {
                    Texture tex = null;
                    int texId;
                    Vector2f texDims = null;

                    // Load the texture if needed...
                    if (_drawTextures && face.texture != null) {
                        tex = face.texture;
                        texDims = new Vector2f(tex.width, tex.height);
                        texId = _regenerateTextures ? genTexture(gl2, tex) : getTexture(tex);
                        gl2.glEnable(GL2.GL_TEXTURE_2D);
                        gl2.glBindTexture(GL.GL_TEXTURE_2D, texId);
                    }

                    // Draw the shape (if we're not wireframing, draw solid, if we're texturing then draw under the wireframe...)
                    if (!_drawWireframe || _drawTextures) {
                        gl2.glColor3f(1, 1, 1);
                        switch (face.vertices.size()) {
                            case 3:
                                gl2.glBegin(GL.GL_TRIANGLES);
                                break;
                            case 4:
                                gl2.glBegin(GL2.GL_QUADS);
                                break;
                            default:
                                gl2.glBegin(GL2.GL_POLYGON);
                                break;
                        }
                        for (int i = 0; i < face.vertices.size(); i++) {
                            Vector3f vertex = face.vertices.get(i);
                            Vector3f normal = face.normals.get(i);
                            if (tex != null) {
                                Vector2f uv = face.uv.get(i);
                                uv = new Vector2f(uv.x, -uv.y).div(texDims);
                                // Model stores the tex coords backwards...
                                gl2.glTexCoord2f(uv.x, 1f - uv.y);
                            }
                            gl2.glNormal3f(normal.x, normal.y, normal.z);
                            gl2.glVertex3f(vertex.x, vertex.y, vertex.z);
                        }

                        gl2.glEnd();
                        gl2.glDisable(GL2.GL_TEXTURE_2D);
                    }

                    // Draw wireframe if need be
                    if (_drawWireframe || isSelected) {
                        if (isSelected)
                            gl2.glColor3f(1, 0, 0);
                        else
                            gl2.glColor3f(1, 1, 1);
                        gl2.glBegin(GL2.GL_LINE_STRIP);
                        List<Vector3f> vertices = face.vertices;
                        for (int i = 0; i < vertices.size(); i++) {
                            Vector3f v = vertices.get(i);
                            Vector3f normal = face.normals.get(i);
                            gl2.glNormal3f(normal.x, normal.y, normal.z);
                            gl2.glVertex3f(v.x, v.y, v.z);
                        }
                        Vector3f v = face.vertices.get(0);
                        Vector3f normal = face.normals.get(0);
                        gl2.glNormal3f(normal.x, normal.y, normal.z);
                        gl2.glVertex3f(v.x, v.y, v.z);
                        gl2.glEnd();
                    }
                }
                gl2.glDisable(GL2.GL_ALPHA_TEST);
                gl2.glPopMatrix();
            }

            public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width, int height) {
                GL2 gl2 = glAutoDrawable.getGL().getGL2();
                gl2.glMatrixMode(GL2.GL_PROJECTION);
                gl2.glLoadIdentity();

                GLU glu = new GLU();
                float aspect = (float) width / (float) height;
                glu.gluPerspective(45, aspect, 0.1f, 20f);
                gl2.glMatrixMode(GL2.GL_MODELVIEW);
                gl2.glLoadIdentity();
            }

            private Vector3f toWorld(GL2 gl2, int x, int y) {
                viewport.clear();
                modelview.clear();
                projview.clear();
                zBuf.clear();
                result.clear();
                gl2.glGetIntegerv(GL2.GL_VIEWPORT, viewport);
                gl2.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, modelview);
                gl2.glGetFloatv(GL2.GL_PROJECTION_MATRIX, projview);
                float winX = viewport.get(3) - x;
                float winY = y;
                gl2.glReadPixels((int) winX, (int) winY, 1, 1, GL2.GL_DEPTH_COMPONENT, GL2.GL_FLOAT, zBuf);
                float winZ = zBuf.get(0);
                glu.gluUnProject(winX, winY, winZ, modelview, projview, viewport, result);
                float px = result.get(0);
                float py = result.get(1);
                float pz = result.get(2);
                return new Vector3f(px, py, pz);
            }
        });

        MouseAdapter mad = new MouseAdapter() {
            private boolean rotate;

            public void mousePressed(MouseEvent e) {
                mouseLock.lock();
                oldX = e.getX();
                oldY = e.getY();
                rotate = e.getButton() == 1;
                mouseLock.unlock();
            }

            public void mouseDragged(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
                mouseLock.lock();
                rX = x - oldX;
                rY = y - oldY;
                oldX = x;
                oldY = y;
                mouseUpdate = true;
                mouseLock.unlock();
            }

            public void mouseWheelMoved(MouseWheelEvent e) {
                camDistance += e.getPreciseWheelRotation() * 0.1f;
            }
        };

        preview.addMouseListener(mad);
        preview.addMouseMotionListener(mad);
        preview.addMouseWheelListener(mad);
    }

    private int getTexture(Texture tex) {
        if (textures.contains(tex))
            return textures.indexOf(tex) + 1;
        return -1;
    }

    private int genTexture(GL2 gl, Texture tex) {
        int id = getTexture(tex);
        if (id != -1) {
            gl.glDeleteTextures(1, IntBuffer.wrap(new int[]{id}));
        } else {
            id = textures.size() + 1;
            textures.add(tex);
        }
        gl.glGenTextures(1, IntBuffer.wrap(new int[]{id}));
        gl.glBindTexture(GL.GL_TEXTURE_2D, id);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
        gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA8, tex.width, tex.height, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, toRgba(tex));
        return id;
    }

    private Buffer toRgba(Texture tex) {
        final int size = tex.width * tex.height;
        ColorMap map = (ColorMap) colorMapSelector.getSelected();
        IntBuffer pix = IntBuffer.allocate(size);
        for (int i = 0; i < size; i++) {
            int idx = tex.indices[i] & 0xff;
            pix.put(tex.hasAlpha && idx == 0 ? 0 : map.colors[idx] | (0xff << 24));
        }
        pix.flip();
        return pix;
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        createUIComponents();

        panel3 = new JPanel();
        panel1 = new JSplitPane();
        panel2 = new JPanel();
        panel4 = new JPanel();
        label1 = new JLabel();
        toggleTextures = new JCheckBox();
        toggleWireframe = new JCheckBox();
        panel5 = new JPanel();
        splitPane1 = new JSplitPane();
        scrollPane1 = new JScrollPane();
        boneTree = new JTree();
        panel6 = new JPanel();
        toggleTextureAction = new ToggleTextures();
        toggleWireframeAction = new ToggleWireframe();

        //======== this ========
        setLayout(new BorderLayout());

        //======== panel3 ========
        {
            panel3.setLayout(new BorderLayout());

            //======== panel1 ========
            {
                panel1.setResizeWeight(0.8);

                //======== panel2 ========
                {
                    panel2.setLayout(new BorderLayout());
                    panel2.add(preview, BorderLayout.CENTER);

                    //======== panel4 ========
                    {
                        panel4.setBorder(new TitledBorder("Render options"));
                        panel4.setLayout(new GridBagLayout());
                        ((GridBagLayout) panel4.getLayout()).columnWidths = new int[]{0, 0, 0, 0, 0};
                        ((GridBagLayout) panel4.getLayout()).rowHeights = new int[]{0, 0};
                        ((GridBagLayout) panel4.getLayout()).columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0E-4};
                        ((GridBagLayout) panel4.getLayout()).rowWeights = new double[]{1.0, 1.0E-4};

                        //---- label1 ----
                        label1.setText("Color map:");
                        label1.setLabelFor(colorMapSelector);
                        panel4.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                                new Insets(0, 0, 0, 0), 0, 0));

                        //---- colorMapSelector ----
                        colorMapSelector.addChangeListener(new ChangeListener() {
                            @Override
                            public void stateChanged(ChangeEvent e) {
                                colorMapSelected(e);
                            }
                        });
                        panel4.add(colorMapSelector, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
                                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                                new Insets(0, 0, 0, 0), 0, 0));

                        //---- toggleTextures ----
                        toggleTextures.setAction(toggleTextureAction);
                        toggleTextures.setSelected(true);
                        panel4.add(toggleTextures, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                                new Insets(0, 0, 0, 0), 0, 0));

                        //---- toggleWireframe ----
                        toggleWireframe.setAction(toggleWireframeAction);
                        panel4.add(toggleWireframe, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
                                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                                new Insets(0, 0, 0, 0), 0, 0));
                    }
                    panel2.add(panel4, BorderLayout.NORTH);
                }
                panel1.setLeftComponent(panel2);

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
                            scrollPane1.setViewportView(boneTree);
                        }
                        splitPane1.setTopComponent(scrollPane1);

                        //======== panel6 ========
                        {
                            panel6.setBorder(new TitledBorder("Bone Properties"));
                            panel6.setLayout(new BorderLayout());
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
        theta = phi = 0;
        camDistance = 1;
        colorMapSelector.setLabFile(data.container);
        Bounds3d bounds = data.getBounds();
        target = bounds == null ? Vector3f.zero : bounds.center();
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

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JPanel panel3;
    private JSplitPane panel1;
    private JPanel panel2;
    private GLJPanel preview;
    private JPanel panel4;
    private JLabel label1;
    private ColorMapSelector colorMapSelector;
    private JCheckBox toggleTextures;
    private JCheckBox toggleWireframe;
    private JPanel panel5;
    private JSplitPane splitPane1;
    private JScrollPane scrollPane1;
    private JTree boneTree;
    private JPanel panel6;
    private ToggleTextures toggleTextureAction;
    private ToggleWireframe toggleWireframeAction;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    private class ToggleTextures extends AbstractAction {
        private ToggleTextures() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "Draw textures");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            _drawTextures = toggleTextures.isSelected();
        }
    }

    private class ToggleWireframe extends AbstractAction {
        private ToggleWireframe() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "Draw wireframe");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            _drawWireframe = toggleWireframe.isSelected();
        }
    }
}
