/*
 * Copyright (C) 2022  James Lawrence.
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

package com.sqrt4.grimedi.ui.editor;

import com.sqrt.liblab.io.DataSource;

import javax.swing.*;
import java.io.IOException;

public class TextView {
  private JPanel panel1;
  private JTextArea textArea1;
  private String content;

  public void setData(DataSource source) {
    byte[] data = new byte[(int) source.length()];
    try {
      source.position(0);
      source.get(data);
    } catch (IOException e) {
      e.printStackTrace();
    }
    this.content = new String(data);
    // We work with multiple data types, which one is this
    textArea1.setText(this.content);
  }

  public JComponent getContentPane() {
    return this.panel1;
  }
}
