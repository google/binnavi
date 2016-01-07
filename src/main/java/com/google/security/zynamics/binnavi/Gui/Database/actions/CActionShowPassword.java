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
package com.google.security.zynamics.binnavi.Gui.Database.actions;



import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;

import com.google.security.zynamics.binnavi.Gui.SaveFields.CSavePasswordField;


/**
 * Toggles between hidden password and plaint-text password in the password field.
 */
public class CActionShowPassword extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -6228598226751722608L;

  /**
   * The database password is shown here.
   */
  private final CSavePasswordField m_passwordField;

  /**
   * Character used to hide the password.
   */
  private final char m_originalCharacter;

  /**
   * Creates a new action object.
   * 
   * @param passwordField The password field where the password is shown.
   */
  public CActionShowPassword(final CSavePasswordField passwordField) {
    super("Show Password");

    m_passwordField = passwordField;

    m_originalCharacter = m_passwordField.getEchoChar();
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    final boolean isSelected = ((JCheckBox) event.getSource()).isSelected();

    m_passwordField.setEchoChar(isSelected ? (char) 0 : m_originalCharacter);
  }
}
