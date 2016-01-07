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

import com.google.security.zynamics.binnavi.Gui.CLimitedInputField;


/**
 * Text field that shows a different background depending on its modification state.
 */
public class CSaveField extends CLimitedInputField {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 3341188938150719952L;

  /**
   * Shows a modified background in case input to the field changed.
   */
  private final CSaveFieldBackground m_background = new CSaveFieldBackground();

  /**
   * Flag that indicates whether input to the field changed.
   */
  private boolean m_modified = false;

  /**
   * Default constructor.
   */
  public CSaveField() {
    super("");
  }

  /**
   * Creates a new save field.
   * 
   * @param text Default text of the save field.
   */
  public CSaveField(final String text) {
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
   * Sets the modification state of the field.
   * 
   * @param modified The modification state of the field.
   */
  public final void setModified(final boolean modified) {
    m_modified = modified;
  }
}
