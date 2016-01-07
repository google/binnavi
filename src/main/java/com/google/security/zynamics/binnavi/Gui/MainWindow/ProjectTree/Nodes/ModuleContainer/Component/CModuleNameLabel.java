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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.ModuleContainer.Component;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;

import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;


/**
 * Custom component that is used to draw name cells in modules tables.
 */
public final class CModuleNameLabel extends JLabel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -6722331517839308493L;

  /**
   * Font that is used to display the names of normal functions.
   */
  private static Font normalFont;

  /**
   * Font that is used to display the names of normal functions, which are currently loaded and
   * displayed in a window.
   */
  private static Font normalBoldFont;

  /**
   * Image shown for stared modules.
   */
  private static Image starImage;

  /**
   * Table where the rendering happens.
   */
  private final JTable m_table;

  /**
   * The module for which the label is created.
   */
  private final INaviModule m_module;

  /**
   * Creates a new module name label object.
   * 
   * @param table Table where the rendering happens.
   * @param module The module for which the label is created.
   * @param backgroundColor The color used for the background of the address label.
   */
  public CModuleNameLabel(final JTable table, final INaviModule module, final Color backgroundColor) {
    if (starImage == null) {
      try {
        starImage =
            new ImageIcon(CMain.class.getResource("data/star.png").toURI().toURL()).getImage();
      } catch (final MalformedURLException e) {
      } catch (final URISyntaxException e) {
      }
    }

    m_table = table;
    m_module = module;

    if (normalFont == null) {
      normalFont = new Font(this.getFont().getFontName(), Font.PLAIN, 12);
      normalBoldFont = new Font(this.getFont().getFontName(), Font.BOLD, 12);
    }

    setBackground(backgroundColor);

    setOpaque(true);
  }

  @Override
  public void paint(final Graphics graphics) {
    super.paint(graphics);

    if (m_module.isStared()) {
      graphics.drawImage(starImage, 0, 0, getHeight() - 2, getHeight() - 2, m_table);
    }

    final boolean isOpen = m_module.isLoaded();
    graphics.setColor(Color.BLACK);
    graphics.setFont(isOpen ? normalBoldFont : normalFont);
    graphics.drawString(m_module.getConfiguration().getName(), m_module.isStared() ? getHeight()
        : 0, 12);
  }
}
