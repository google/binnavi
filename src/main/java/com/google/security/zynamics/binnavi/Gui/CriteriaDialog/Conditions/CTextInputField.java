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
package com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFormattedTextField;

/**
 * Input field where text criteria strings can be entered.
 */
public final class CTextInputField extends JFormattedTextField {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 2889698700058023189L;

  /**
   * The criterium to update on user input.
   */
  private final CAbstractCriterium m_criterium;

  /**
   * Updates the visible tree on user input.
   */
  private final InternalTextListener m_textFieldListener = new InternalTextListener();

  /**
   * Creates a new input field object.
   *
   * @param criterium The criterium to update on user input.
   * @param formatter The formatter to be used by the text field.
   */
  public CTextInputField(final CAbstractCriterium criterium, final AbstractFormatter formatter) {
    super(formatter);

    m_criterium = criterium;

    addKeyListener(m_textFieldListener);
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    removeKeyListener(m_textFieldListener);
  }

  /**
   * Updates the visible tree on user input.
   */
  private class InternalTextListener implements KeyListener {
    @Override
    public void keyPressed(final KeyEvent event) {
      m_criterium.notifyListeners();
    }

    @Override
    public void keyReleased(final KeyEvent event) {
      m_criterium.notifyListeners();
    }

    @Override
    public void keyTyped(final KeyEvent event) {
      m_criterium.notifyListeners();
    }
  }
}
