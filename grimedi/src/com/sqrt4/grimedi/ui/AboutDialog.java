

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
 * Created by JFormDesigner on Mon Sep 29 18:23:08 BST 2014
 */

package com.sqrt4.grimedi.ui;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import javax.swing.*;
import javax.swing.event.*;

/**
 * @author James Lawrence
 */
public class AboutDialog extends JDialog {
    public AboutDialog(Frame owner) {
        super(owner);
        initComponents();
    }

    public AboutDialog(Dialog owner) {
        super(owner);
        initComponents();
    }

    private void aboutHtmlHyperlinkUpdate(HyperlinkEvent e) {
        if(e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
            aboutHtml.setToolTipText(e.getURL().toExternalForm());
        } else if(e.getEventType() == HyperlinkEvent.EventType.EXITED) {
            aboutHtml.setToolTipText(null);
        } else if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            if(!Desktop.isDesktopSupported())
                JOptionPane.showMessageDialog(this, "Opening URL is not supported on this platform", "Error", JOptionPane.WARNING_MESSAGE);
            else {
                try {
                    Desktop.getDesktop().browse(e.getURL().toURI());
                } catch (IOException | URISyntaxException e1) {
                    MainWindow.getInstance().handleException(e1);
                }
            }
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        scrollPane1 = new JScrollPane();
        aboutHtml = new JEditorPane();

        //======== this ========
        setTitle("About GrimEdi");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setModal(true);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== scrollPane1 ========
        {

            //---- aboutHtml ----
            aboutHtml.setContentType("text/html");
            aboutHtml.setEditable(false);
            aboutHtml.setText("<p color=\"black\" align=\"center\">GrimEdi Copyright 2013 James Lawrence<br/>\n<br/>\nThe source code is available on GitHub, you can find it <a href=\"https://github.com/jlaw90/Grimja/\">here</a>.<br/>\nPlease feel free to make improvements/fixes and send a pull request - the more the merrier.<br/>\nIf you find any problems with this software, please create an issue in the <a href=\"https://github.com/jlaw90/Grimja/issues\">issue tracker</a><br/>\n<br/>\nGrimEdit is protected by the GPLv3 license, to understand what this means, please <a href=\"http://www.gnu.org/copyleft/gpl.html\">read it here</a><br/>\n<br/>\nAdditionally, GrimEdi uses the following components which also have their own licenses:<br/>\n<br/>\n<a href=\"http://jogamp.org/jogl/www/\">JOGL</a> for 3d rendering (<a href=\"http://jogamp.org/git/?p=jogl.git;a=blob;f=LICENSE.txt\">view license</a>)\n<br/>\nGlueGen as a dependency of JOGL (<a href=\"http://jogamp.org/git/?p=gluegen.git;a=blob;f=LICENSE.txt\">view license</a>)<br/>\nNetbeans Outline component for rendering the pretty tree table (<a href=\"https://netbeans.org/cddl-gplv2.html\">view license</a>)");
            aboutHtml.addHyperlinkListener(new HyperlinkListener() {
                @Override
                public void hyperlinkUpdate(HyperlinkEvent e) {
                    aboutHtmlHyperlinkUpdate(e);
                }
            });
            scrollPane1.setViewportView(aboutHtml);
        }
        contentPane.add(scrollPane1, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JScrollPane scrollPane1;
    private JEditorPane aboutHtml;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
