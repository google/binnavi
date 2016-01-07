/*
Copyright 2011-2016 Google Inc. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.google.security.zynamics.binnavi.standardplugins.utils;


import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;

public final class GuiHelper {
  /**
   * Centers the child component relative to it's parent component
   * 
   * @param parent
   * @param child
   * @param bStayOnScreen
   */
  public static final void centerChildToParent(final Component parent, final Component child,
      final boolean bStayOnScreen) {
    int x = (parent.getX() + (parent.getWidth() / 2)) - (child.getWidth() / 2);
    int y = (parent.getY() + (parent.getHeight() / 2)) - (child.getHeight() / 2);
    if (bStayOnScreen) {
      final Toolkit tk = Toolkit.getDefaultToolkit();
      final Dimension ss = new Dimension(tk.getScreenSize());
      if ((x + child.getWidth()) > ss.getWidth()) {
        x = (int) (ss.getWidth() - child.getWidth());
      }
      if ((y + child.getHeight()) > ss.getHeight()) {
        y = (int) (ss.getHeight() - child.getHeight());
      }
      if (x < 0) {
        x = 0;
      }
      if (y < 0) {
        y = 0;
      }
    }
    child.setLocation(x, y);
  }

  public static String getMonospaceFont() {
    final GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
    final Font[] allfonts = env.getAllFonts();

    for (final Font font : allfonts) {
      if (font.getName().equals("Courier New")) {
        return "Courier New";
      }
    }

    return Font.MONOSPACED;
  }
}
