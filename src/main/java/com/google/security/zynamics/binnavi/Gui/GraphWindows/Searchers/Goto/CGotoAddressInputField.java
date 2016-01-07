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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Searchers.Goto;

import com.google.security.zynamics.binnavi.Help.IHelpInformation;
import com.google.security.zynamics.binnavi.Help.IHelpProvider;
import com.google.security.zynamics.zylib.gui.CHexFormatter;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JFormattedTextField;



/**
 * Input component of the address input field.
 */
public final class CGotoAddressInputField extends JFormattedTextField implements IHelpProvider {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 8982813299304500378L;

  /**
   * Background color of the text field when the specified address does not exist in the graph.
   */
  private static final Color BACKGROUND_COLOR_FAIL = new Color(255, 128, 128);

  /**
   * Background color of the text field when the specified address exists in the graph.
   */
  private static final Color BACKGROUND_COLOR_SUCCESS = Color.WHITE;

  /**
   * Flag that says whether the last search was successful or not.
   */
  private boolean m_lastSearchSuccessful = true;

  /**
   * Creates a new input field object.
   */
  public CGotoAddressInputField() {
    super(new CHexFormatter(16));
  }

  @Override
  public IHelpInformation getHelpInformation() {
    return new CGotoAddressHelp();
  }

  @Override
  public void paintComponent(final Graphics graphics) {
    setOpaque(false);

    graphics.setColor(m_lastSearchSuccessful ? BACKGROUND_COLOR_SUCCESS : BACKGROUND_COLOR_FAIL);
    graphics.fillRect(2, 2, getWidth() - 4, getHeight() - 4);

    super.paintComponent(graphics);
  }

  /**
   * Sets the success flag.
   *
   * @param success True, to say the last search was successful. False, otherwise.
   */
  public void setSuccessful(final boolean success) {
    m_lastSearchSuccessful = success;
  }
}
