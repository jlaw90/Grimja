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

package com.sqrt4.grimedi;

import com.sqrt4.grimedi.ui.MainWindow;

import javax.swing.*;

public class Main {
    public static final String VERSION = "0.1";

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            JFrame.setDefaultLookAndFeelDecorated(true);
            JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        } catch(Exception ignore) {
        }
        System.setProperty("sun.java2d.opengl","True");
        MainWindow mw = new MainWindow();
        mw.setVisible(true);
    }
}