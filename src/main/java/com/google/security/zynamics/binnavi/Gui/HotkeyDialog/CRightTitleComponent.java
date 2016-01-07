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
package com.google.security.zynamics.binnavi.Gui.HotkeyDialog;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JComponent;

import com.google.common.base.Preconditions;

/**
 * Component that is shown in the cells that display the right half of hotkey section titles.
 */
public final class CRightTitleComponent extends JComponent {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -6268634847281874601L;

  /**
   * Background color of hotkey section title rows.
   */
  public static final Color DEFAULT_BACKGROUND_COLOR = new Color(231, 237, 227);

  /**
   * The string to display.
   */
  private final String m_string;

  /**
   * Creates a new rendering component.
   *
   * @param string The string to display.
   */
  public CRightTitleComponent(final String string) {
    Preconditions.checkNotNull(string, "IE01819: String argument can not be null");

    m_string = string;
  }

  @Override
  public void paintComponent(final Graphics graphics) {
    super.paintComponents(graphics);

    graphics.setColor(DEFAULT_BACKGROUND_COLOR);
    graphics.fillRect(0, 0, getWidth(), getHeight());

    graphics.setColor(Color.BLACK);
    graphics.drawString(m_string, 0, 12);
  }
}
