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
 * Created by JFormDesigner on Fri Mar 22 14:35:59 GMT 2013
 */

package com.sqrt4.grimedi.ui.component;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.math.FloatUtil;
import com.sqrt.liblab.entry.model.*;
import com.sqrt.liblab.threed.Angle;
import com.sqrt.liblab.threed.Bounds3;
import com.sqrt.liblab.threed.Vector2f;
import com.sqrt.liblab.threed.Vector3f;
import com.sqrt4.grimedi.ui.MainWindow;
import com.sqrt4.grimedi.ui.layout.WrapLayout;

import javax.media.opengl.*;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.PrintStream;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author James Lawrence
 */
public class ModelRenderer extends JPanel implements GLEventListener {
    private OurAnimator animator;
    private GrimModel model;
    private java.util.List<Texture> textures = new LinkedList<Texture>();
    private boolean _regenerateTextures;
    public boolean drawTextures = true;
    public boolean drawWireframe;
    public boolean drawNormals;
    public boolean smoothShading = true;
    public boolean drawPlane = true;
    public boolean useCallList = true;
    public float planeWidth = 0.5f;
    public float planeExtent = 10f;
    private Vector3f target;
    private float camDistance = 1f, theta, phi;
    private boolean mouseUpdate, rebuildList = true, builtList;
    private int listId;
    private Lock mouseLock = new ReentrantLock();
    private int oldX, oldY, rX, rY;
    private GLU glu = new GLU();
    private ModelNode selected;
    private FrameCallback callback;
    private GLCanvas canvas;
    private int viewWidth, viewHeight;

    public ModelRenderer() {
        initComponents();
        toggleTextures.setSelected(drawTextures);
        toggleWireframe.setSelected(drawWireframe);
        toggleNormals.setSelected(drawNormals);
        toggleSmooth.setSelected(smoothShading);
        toggleGrid.setSelected(drawPlane);
    }

    public int getViewportWidth() {
        return viewWidth;
    }

    public int getViewportHeight() {
        return viewHeight;
    }

    public void setSelectedNode(ModelNode node) {
        selected = node;
        rebuildList = true;
    }

    public ModelNode getSelectedNode() {
        return selected;
    }

    public void setModel(final GrimModel model) {
        this.model = model;
        loadGL();
        selected = null;
        _regenerateTextures = true;
        rebuildList = true;
        phi = 0;
        theta = -4.71f;
        camDistance = 1;
        colorMapSelector.setLabFile(model.container);
        Bounds3 bounds = model.getBounds();
        target = bounds == null ? Vector3f.zero : bounds.center;
    }

    public void setCallback(FrameCallback fc) {
        this.callback = fc;
    }

    public FrameCallback getCallback() {
        return callback;
    }

    private IntBuffer viewport = Buffers.newDirectIntBuffer(4);
    private FloatBuffer modelview = Buffers.newDirectFloatBuffer(16);
    private FloatBuffer projview = Buffers.newDirectFloatBuffer(16);
    private FloatBuffer zBuf = Buffers.newDirectFloatBuffer(1);
    private FloatBuffer result = Buffers.newDirectFloatBuffer(4);

    public void init(GLAutoDrawable glAutoDrawable) {
        //glAutoDrawable.setGL(new DebugGL2(glAutoDrawable.getGL().getGL2()));

        GL2 gl = glAutoDrawable.getGL().getGL2();
        gl.glClearColor(0.5f, 0.5f, 0.5f, 1);
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glEnable(GL2.GL_LIGHT0);
        gl.glAlphaFunc(GL.GL_GREATER, 0.5f);
        gl.glEnable(GL2.GL_ALPHA_TEST);
        gl.glColorMaterial(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE);
        gl.glEnable(GL2.GL_COLOR_MATERIAL);

        for (int i = 0; i < textures.size(); i++)
            gl.glDeleteTextures(1, IntBuffer.wrap(new int[]{i + 1}));
        textures.clear();
        _regenerateTextures = true;
    }

    public GL getGL() {
        return canvas.getGL();
    }

    public void dispose(GLAutoDrawable glAutoDrawable) {
    }

    public void display(GLAutoDrawable glAutoDrawable) {
        GL2 gl2 = glAutoDrawable.getGL().getGL2();
        gl2.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        if (model == null || target == null)
            return;
        if (callback != null)
            callback.preDisplay(gl2);
        Vector3f rot = new Vector3f(camDistance * FloatUtil.cos(theta),
                camDistance * FloatUtil.cos(phi) * FloatUtil.sin(theta),
                camDistance * FloatUtil.sin(phi) * FloatUtil.sin(theta));
        Vector3f cam = target.add(rot);
        gl2.glLoadIdentity();
        if (mouseUpdate && mouseLock.tryLock()) {
            Vector3f orig = toWorld(gl2, oldX, oldY);
            Vector3f n = toWorld(gl2, oldX + rX, oldY + rY);
            Vector3f delta = n.sub(orig);
            theta += delta.x;
            phi += delta.y;
            // Todo: clamp..
            rX = rY = 0;
            mouseUpdate = false;
            mouseLock.unlock();
        }
        glu.gluLookAt(cam.x, cam.y, cam.z, target.x, target.y, target.z, 0, 0, 1);

        if (useCallList && rebuildList) {
            if (builtList)
                gl2.glDeleteLists(listId, 1);
            listId = gl2.glGenLists(1);
            gl2.glNewList(listId, GL2.GL_COMPILE);
            doRender(gl2);
            gl2.glEndList();
            builtList = true;
            rebuildList = false;
        }
        if (!useCallList && builtList) {
            rebuildList = true;
            builtList = false;
            gl2.glDeleteLists(listId, 1);
        }

        gl2.glCallList(listId);
        if (drawTextures)
            _regenerateTextures = false;
        if (callback != null)
            callback.postDisplay(gl2);
    }

    private void doRender(GL2 gl2) {
        // Draw floor plane...
        if (drawPlane) {
            gl2.glColor3f(0.7f, 0.7f, 0.7f);
            gl2.glBegin(GL2.GL_LINES);
            for (float x = -planeExtent; x <= planeExtent; x += planeWidth) {
                gl2.glVertex3f(x, -planeExtent, 0);
                gl2.glVertex3f(x, planeExtent, 0); // left-to-right
                gl2.glVertex3f(-planeExtent, x, 0);
                gl2.glVertex3f(planeExtent, x, 0); // top-to-bottom
            }
            gl2.glEnd();
        }

        renderNode(gl2, model.hierarchy.get(0));
    }

    private void renderNode(GL2 gl2, ModelNode node) {
        boolean isSelected = selected == node;
        // -- translateViewport
        Vector3f animPos = node.pos.add(node.animPos);
        Angle animPitch = node.pitch.add(node.animPitch);
        Angle animYaw = node.yaw.add(node.animYaw);
        Angle animRoll = node.roll.add(node.animRoll);
        // -- translateViewportStart
        gl2.glMatrixMode(GL2.GL_MODELVIEW);
        gl2.glPushMatrix();
        // --
        gl2.glTranslatef(animPos.x, animPos.y, animPos.z);
        gl2.glRotatef(animYaw.degrees, 0, 0, 1);
        gl2.glRotatef(animPitch.degrees, 1, 0, 0);
        gl2.glRotatef(animRoll.degrees, 0, 1, 0);
        // --
        if (node.hierarchyVisible) {
            gl2.glMatrixMode(GL2.GL_MODELVIEW);
            gl2.glPushMatrix();
            gl2.glTranslatef(node.pivot.x, node.pivot.y, node.pivot.z);

            // Draw mesh...
            if (node.mesh != null && node.meshVisible) {
                for (MeshFace face : node.mesh.faces) {
                    Texture tex = null;
                    int texId;

                    gl2.glEnable(GL2.GL_LIGHTING);
                    // Load the texture if needed...
                    if (drawTextures && face.texture != null) {
                        tex = face.texture;
                        texId = _regenerateTextures ? genTexture(gl2, tex) : getTexture(tex);
                        gl2.glEnable(GL2.GL_TEXTURE_2D);
                        gl2.glBindTexture(GL.GL_TEXTURE_2D, texId);
                    }

                    // Draw the shape (if we're not wireframing, draw solid, if we're texturing then draw under the wireframe...)
                    if (!drawWireframe || drawTextures) {
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
                            Vector3f normal = smoothShading ? face.normals.get(i) : face.normal;
                            if (tex != null) {
                                Vector2f uv = face.uv.get(i);
                                // Model stores the tex coords backwards...
                                gl2.glTexCoord2f(uv.x / tex.width, 1f + uv.y / tex.height);
                            }
                            gl2.glNormal3f(normal.x, normal.y, normal.z);
                            gl2.glVertex3f(vertex.x, vertex.y, vertex.z);
                        }

                        gl2.glEnd();
                        gl2.glDisable(GL2.GL_TEXTURE_2D);
                    }

                    // Draw wireframe if need be
                    if (drawWireframe || isSelected) {
                        if (isSelected)
                            gl2.glColor3f(1, 0, 0);
                        else
                            gl2.glColor3f(1, 1, 1);
                        gl2.glBegin(GL2.GL_LINE_STRIP);
                        List<Vector3f> vertices = face.vertices;
                        for (int i = 0; i < vertices.size(); i++) {
                            Vector3f v = vertices.get(i);
                            Vector3f normal = smoothShading ? face.normals.get(i) : face.normal;
                            gl2.glNormal3f(normal.x, normal.y, normal.z);
                            gl2.glVertex3f(v.x, v.y, v.z);
                        }
                        Vector3f v = face.vertices.get(0);
                        Vector3f normal = smoothShading ? face.normals.get(0) : face.normal;
                        gl2.glNormal3f(normal.x, normal.y, normal.z);
                        gl2.glVertex3f(v.x, v.y, v.z);
                        gl2.glEnd();
                    }

                    if (drawNormals) {
                        final float normalLength = 0.01f * camDistance;
                        if (isSelected)
                            gl2.glColor3f(0, 1, 0);
                        else
                            gl2.glColor3f(0, 0, 1);
                        gl2.glDisable(GL2.GL_LIGHTING);
                        gl2.glBegin(GL2.GL_LINES);
                        Vector3f normal = face.normal;
                        Vector3f center = face.getBounds(new Vector3f(0, 0, 0)).center;
                        gl2.glVertex3f(center.x, center.y, center.z);
                        gl2.glVertex3f(center.x + normal.x * normalLength, center.y + normal.y * normalLength, center.z + normal.z * normalLength);
                        List<Vector3f> vertices = face.vertices;
                        for (int i = 0; i < vertices.size(); i++) {
                            Vector3f v = vertices.get(i);
                            normal = face.normals.get(i);
                            gl2.glVertex3f(v.x, v.y, v.z);
                            gl2.glVertex3f(v.x + normal.x * normalLength, v.y + normal.y * normalLength, v.z + normal.z * normalLength);
                        }
                        gl2.glEnd();
                    }
                }
            }
            gl2.glMatrixMode(GL2.GL_MODELVIEW);
            gl2.glPopMatrix();

            if (node.child != null)
                renderNode(gl2, node.child);

        }
        gl2.glMatrixMode(GL2.GL_MODELVIEW);
        gl2.glPopMatrix();

        if (node.sibling != null)
            renderNode(gl2, node.sibling);
        gl2.glDisable(GL2.GL_LIGHTING);
    }

    public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width, int height) {
        GL2 gl2 = glAutoDrawable.getGL().getGL2();
        gl2.glMatrixMode(GL2.GL_PROJECTION);
        gl2.glLoadIdentity();

        GLU glu = new GLU();
        float aspect = (float) width / (float) height;
        glu.gluPerspective(45, aspect, 0.0001f, 20f);
        gl2.glMatrixMode(GL2.GL_MODELVIEW);
        gl2.glLoadIdentity();
        viewWidth = width;
        viewHeight = height;
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
        gl2.glReadPixels((int) winX, y, 1, 1, GL2.GL_DEPTH_COMPONENT, GL2.GL_FLOAT, zBuf);
        float winZ = zBuf.get(0);
        glu.gluUnProject(winX, y, winZ, modelview, projview, viewport, result);
        float px = result.get(0);
        float py = result.get(1);
        float pz = result.get(2);
        return new Vector3f(px, py, pz);
    }

    public void refreshModelCache() {
        rebuildList = true;
    }

    private void loadGL() {
        try {
            if (animator != null)
                return;
            animator = new OurAnimator();
            GLCapabilities caps = new GLCapabilities(GLProfile.getDefault());
            caps.setDepthBits(32);
            canvas = new GLCanvas(caps);
            canvas.addGLEventListener(this);
            panel2.add(canvas, BorderLayout.CENTER);
            animator.add(canvas);
            animator.start();

            MouseAdapter mad = new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    mouseLock.lock();
                    oldX = e.getX();
                    oldY = e.getY();
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

            canvas.addMouseListener(mad);
            canvas.addMouseMotionListener(mad);
            canvas.addMouseWheelListener(mad);
        } catch (Throwable t) {
            MainWindow.getInstance().handleException(t);
        }
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
        ColorMap map = colorMapSelector.getSelected();
        IntBuffer pix = IntBuffer.allocate(size);
        for (int i = 0; i < size; i++) {
            int idx = tex.indices[i] & 0xff;
            pix.put(tex.hasAlpha && idx == 0 ? 0 : map.colors[idx] | (0xff << 24));
        }
        pix.flip();
        return pix;
    }

    private void colorMapSelected(ItemEvent ignore) {
        _regenerateTextures = true;
        rebuildList = true;
    }

    public GrimModel getModel() {
        return model;
    }

    public ColorMap getColorMap() {
        return colorMapSelector.getSelected();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        panel2 = new JPanel();
        panel4 = new JPanel();
        panel1 = new JPanel();
        label1 = new JLabel();
        colorMapSelector = new ColorMapSelector();
        toggleTextures = new JCheckBox();
        toggleWireframe = new JCheckBox();
        toggleNormals = new JCheckBox();
        toggleSmooth = new JCheckBox();
        toggleGrid = new JCheckBox();
        normalToggleAction = new NormalToggleAction();
        toggleSmoothAction = new ToggleSmoothAction();
        toggleTextureAction = new ToggleTextures();
        toggleWireframeAction = new ToggleWireframe();
        toggleGridAction = new ToggleGridAction();

        //======== this ========
        setLayout(new BorderLayout());

        //======== panel2 ========
        {
            panel2.setLayout(new BorderLayout());
        }
        add(panel2, BorderLayout.CENTER);

        //======== panel4 ========
        {
            panel4.setLayout(null);
            panel4.setLayout(new WrapLayout());

            //======== panel1 ========
            {
                panel1.setLayout(new FlowLayout());

                //---- label1 ----
                label1.setText("Color Map:");
                panel1.add(label1);

                //---- colorMapSelector ----
                colorMapSelector.setMinimumSize(new Dimension(100, 20));
                colorMapSelector.setPreferredSize(new Dimension(100, 20));
                colorMapSelector.addItemListener(new ItemListener() {
                    @Override
                    public void itemStateChanged(ItemEvent e) {
                        colorMapSelected(e);
                    }
                });
                panel1.add(colorMapSelector);
            }
            panel4.add(panel1);
            panel1.setBounds(new Rectangle(new Point(45, 5), panel1.getPreferredSize()));

            //---- toggleTextures ----
            toggleTextures.setAction(toggleTextureAction);
            toggleTextures.setSelected(true);
            panel4.add(toggleTextures);
            toggleTextures.setBounds(new Rectangle(new Point(217, 8), toggleTextures.getPreferredSize()));

            //---- toggleWireframe ----
            toggleWireframe.setAction(toggleWireframeAction);
            panel4.add(toggleWireframe);
            toggleWireframe.setBounds(new Rectangle(new Point(291, 8), toggleWireframe.getPreferredSize()));

            //---- toggleNormals ----
            toggleNormals.setAction(normalToggleAction);
            panel4.add(toggleNormals);
            toggleNormals.setBounds(new Rectangle(new Point(371, 8), toggleNormals.getPreferredSize()));

            //---- toggleSmooth ----
            toggleSmooth.setAction(toggleSmoothAction);
            panel4.add(toggleSmooth);
            toggleSmooth.setBounds(new Rectangle(new Point(164, 40), toggleSmooth.getPreferredSize()));

            //---- toggleGrid ----
            toggleGrid.setAction(toggleGridAction);
            panel4.add(toggleGrid);
            toggleGrid.setBounds(new Rectangle(new Point(270, 40), toggleGrid.getPreferredSize()));
        }
        add(panel4, BorderLayout.NORTH);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JPanel panel2;
    private JPanel panel4;
    private JPanel panel1;
    private JLabel label1;
    private ColorMapSelector colorMapSelector;
    private JCheckBox toggleTextures;
    private JCheckBox toggleWireframe;
    private JCheckBox toggleNormals;
    private JCheckBox toggleSmooth;
    private JCheckBox toggleGrid;
    private NormalToggleAction normalToggleAction;
    private ToggleSmoothAction toggleSmoothAction;
    private ToggleTextures toggleTextureAction;
    private ToggleWireframe toggleWireframeAction;
    private ToggleGridAction toggleGridAction;
// JFormDesigner - End of variables declaration  //GEN-END:variables

    private class ToggleSmoothAction extends AbstractAction {
        private ToggleSmoothAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "Smooth shading");
            putValue(SHORT_DESCRIPTION, "toggle smooth shading");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            smoothShading = toggleSmooth.isSelected();
            rebuildList = true;
        }
    }

    private class NormalToggleAction extends AbstractAction {
        private NormalToggleAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "Normals");
            putValue(SHORT_DESCRIPTION, "draw normals");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            drawNormals = toggleNormals.isSelected();
            rebuildList = true;
        }
    }

    private class ToggleTextures extends AbstractAction {
        private ToggleTextures() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "Textured");
            putValue(SHORT_DESCRIPTION, "map face textures");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            drawTextures = toggleTextures.isSelected();
            colorMapSelector.setEnabled(drawTextures);
            rebuildList = true;
        }
    }

    private class ToggleWireframe extends AbstractAction {
        private ToggleWireframe() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "Wireframe");
            putValue(SHORT_DESCRIPTION, "draw wireframes");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            drawWireframe = toggleWireframe.isSelected();
            rebuildList = true;
        }
    }

    private class ToggleGridAction extends AbstractAction {
        private ToggleGridAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "Grid");
            putValue(SHORT_DESCRIPTION, "draw a grid");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            drawPlane = toggleGrid.isSelected();
            rebuildList = true;
        }
    }
}

class OurAnimator implements GLAnimatorControl {
    private List<GLAutoDrawable> drawables = Collections.synchronizedList(new LinkedList<GLAutoDrawable>());
    private boolean started, paused;
    private int frameDelta;

    private Runnable _runner = new Runnable() {
        public void run() {
            try {
                while (started) {
                    try {
                        long start = System.currentTimeMillis();
                        if (isAnimating()) {
                            for (int i = 0; isAnimating() && i < drawables.size(); i++) {
                                GLAutoDrawable drawable = drawables.get(i);
                                drawable.display();
                            }
                        }
                        long delta = System.currentTimeMillis() - start;
                        Thread.sleep(Math.max(frameDelta - delta, 5));
                    } catch (Throwable ignore) {
                        MainWindow.getInstance().handleException(ignore);
                    }
                }
            } finally {
                started = false;
                paused = false;
            }
        }
    };
    private Thread thread;

    public boolean isStarted() {
        return started;
    }

    public boolean isAnimating() {
        return started && !paused && !drawables.isEmpty();
    }

    public boolean isPaused() {
        return paused;
    }

    public Thread getThread() {
        return thread;
    }

    public boolean start() {
        if (thread != null)
            return false;
        started = true;
        paused = false;
        thread = new Thread(_runner);
        thread.setPriority(1);
        thread.setDaemon(true);
        thread.start();
        return true;
    }

    public boolean stop() {
        if (thread == null)
            return false;
        started = false;
        try {
            thread.join();
            thread = null;
            return true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean pause() {
        if (thread == null)
            return false;
        paused = true;
        return true;
    }

    public boolean resume() {
        if (!paused)
            return false;
        paused = false;
        return true;
    }

    public void add(GLAutoDrawable glAutoDrawable) {
        if (drawables.contains(glAutoDrawable))
            return;
        drawables.add(glAutoDrawable);
        glAutoDrawable.setAnimator(this);
    }

    public void remove(GLAutoDrawable glAutoDrawable) {
        drawables.remove(glAutoDrawable);
    }

    @Override
    public UncaughtExceptionHandler getUncaughtExceptionHandler() {
        return null;
    }

    @Override
    public void setUncaughtExceptionHandler(UncaughtExceptionHandler uncaughtExceptionHandler) {

    }

    public void setUpdateFPSFrames(int i, PrintStream printStream) {
        frameDelta = 1000 / i;
    }

    public void resetFPSCounter() {
    }

    public int getUpdateFPSFrames() {
        return 0;
    }

    public long getFPSStartTime() {
        return 0;
    }

    public long getLastFPSUpdateTime() {
        return 0;
    }

    public long getLastFPSPeriod() {
        return 0;
    }

    public float getLastFPS() {
        return 0;
    }

    public int getTotalFPSFrames() {
        return 0;
    }

    public long getTotalFPSDuration() {
        return 0;
    }

    public float getTotalFPS() {
        return 0;
    }
}