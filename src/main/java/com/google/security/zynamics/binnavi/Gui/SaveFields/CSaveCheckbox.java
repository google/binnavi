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
package com.google.security.zynamics.binnavi.Gui.SaveFields;

import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JCheckBox;

/**
 * Check box that shows different backgrounds on different modification states.
 */
public class CSaveCheckbox extends JCheckBox {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -5389859186329717013L;

  /**
   * Shows a modified background in case input to the box changed.
   */
  private final CSaveFieldBackground m_background = new CSaveFieldBackground();

  /**
   * Flag that indicates whether input to the box changed.
   */
  private boolean m_modified = false;

  /**
   * Creates a new box object.
   * 
   * @param text Text to show in the check box.
   */
  public CSaveCheckbox(final String text) {
    super(text);
  }

  @Override
  public final void paintComponent(final Graphics graphics) {
    setOpaque(!m_modified);

    if (m_modified) {
      m_background.paint((Graphics2D) graphics, getWidth(), getHeight());
    }

    super.paintComponent(graphics);
  }

  /**
   * Changes the modification state of the box.
   * 
   * @param modified The new modification state.
   */
  public final void setModified(final boolean modified) {
    m_modified = modified;
  }
}
