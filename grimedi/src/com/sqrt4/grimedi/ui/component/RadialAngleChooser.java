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

package com.sqrt4.grimedi.ui.component;

import com.sqrt.liblab.threed.Angle;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by James on 01/10/2014.
 */
public class RadialAngleChooser extends JPanel implements MouseListener, MouseWheelListener, MouseMotionListener {
    private final List<ChangeListener> listeners = new LinkedList<>();

    // angle, between -180 and 180
    private float angle;

    public RadialAngleChooser() {
        addMouseListener(this);
        addMouseWheelListener(this);
        addMouseMotionListener(this);
    }

    public void setAngle(float angle) {
        this.angle = Angle.normalize(angle, -180f);
        repaint();
        if(listeners.isEmpty())
            return;


        ChangeEvent ce = new ChangeEvent(this);
        synchronized (listeners) {
            for (ChangeListener cl : listeners) {
                cl.stateChanged(ce);
            }
        }
    }

    public float getAngle() {
        return angle;
    }

    public void addChangeListener(ChangeListener cl) {
        synchronized (listeners) {
            listeners.add(cl);
        }
    }

    public void removeChangeListener(ChangeListener cl) {
        synchronized (listeners) {
            listeners.remove(cl);
        }
    }

    public void paint(Graphics g) {
        super.paint(g);

        Color fg = isEnabled()? getForeground(): getBackground().darker();
        g.setColor(fg);

        // Draw circle
        final int border = 4;
        Dimension d = getSize();
        int diam = Math.min(d.width, d.height) - border;
        int dX = d.width - diam;
        int dY = d.height - diam;
        int x = dX == 0? 0: dX/2;
        int y = dY == 0? 0: dY/2;
        g.drawOval(x, y, diam, diam);

        // Draw indicator
        int r = diam/2;
        int cX = d.width/2;
        int cY = d.height/2;
        double rad = Math.toRadians(angle);
        int eX = cX + (int) (Math.sin(rad) * r);
        int eY = cY - (int) (Math.cos(rad) * r);

        g.drawLine(cX, cY, eX, eY);
    }

    private void setAngleFromMouse(MouseEvent e) {
        if(!isEnabled())
            return;
        int x = e.getX();
        int y = e.getY();
        int cX = getWidth()/2;
        int cY = getHeight()/2;
        double deg = Math.toDegrees(Math.atan2(y - cY, x - cX)) + 90;
        setAngle((float) deg);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        setAngleFromMouse(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        setAngleFromMouse(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getScrollType() != MouseWheelEvent.WHEEL_UNIT_SCROLL)
            return;
        int i = e.getUnitsToScroll();
        setAngle(angle + i);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        setAngleFromMouse(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }
}