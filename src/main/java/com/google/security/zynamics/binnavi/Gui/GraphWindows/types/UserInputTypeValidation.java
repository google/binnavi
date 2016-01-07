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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.types;

import java.awt.Component;

import javax.swing.JComboBox;
import javax.swing.JTextField;

import com.google.security.zynamics.binnavi.disassembly.types.TypeManager;
import com.google.security.zynamics.zylib.gui.CMessageBox;

/**
 * Helper class that validates several types of user inputs when creating new types in the type
 * dialog panels.
 *
 * @author jannewger (Jan Newger)
 *
 */
public class UserInputTypeValidation {

  private UserInputTypeValidation() {
  }

  /**
   * Determines whether the given list of base types has a selected item.
   * 
   * @param parent The compoenent that is used as a parent to display error messages.
   * @param baseTypes The combo box that contains a list of base types.
   * @return True iff the combo box selected item is not null.
   */
  public static boolean validateComboBox(final Component parent, final JComboBox baseTypes) {
    if (validateComboBox(baseTypes)) {
      return true;
    } else {
      CMessageBox.showWarning(parent, "Please select a type from the list of base types.");
      return false;
    }
  }

  /**
   * Only validate that a base type is selected without showing an error message.
   */
  public static boolean validateComboBox(final JComboBox baseTypes) {
    return baseTypes.getSelectedItem() != null;
  }

  /**
   * Determines whether the given text field represents a valid type name and that the corresponding
   * does not already exist.
   *
   * @param parent The component that is used as a parent to display error messages.
   * @param typeManager The type manager that holds the type system.
   * @param name The text field that needs to be validated.
   * @return True iff the name does not already exist and is a valid type name.
   */
  public static boolean validateTypeName(
      final Component parent, final TypeManager typeManager, final JTextField name) {
    if (validateTypeName(typeManager, name)) {
      return true;
    } else {
      CMessageBox.showWarning(parent, String.format(
          "Unable to create empty or existing type."));
      return false;
    }
  }

  /**
   * Only validate the type name without showing an error message.
   */
  public static boolean validateTypeName(final TypeManager typeManager, final JTextField name) {
    return !name.getText().isEmpty() && !typeManager.isTypeExisting(name.getText());
  }

  /**
   * Determines whether the given text field contains a valid type size.
   *
   * @param parent The component that is used as a parent to display error messages.
   * @param size The text field that needs to be validated.
   * @return True iff the given text field contains a valid type size.
   */
  public static boolean validateTypeSize(final Component parent, final JTextField size) {
    if (validateTypeSize(size)) {
      return true;
    } else {
      CMessageBox.showWarning(parent, "Please enter a valid type size.");
      return false;
    }
  }

  /**
   * Only validate the type size without showing an error message.
   */
  public static boolean validateTypeSize(final JTextField size) {
    try {
      if (size.getText().isEmpty() || Integer.parseInt(size.getText()) < 0) {
        return false;
      }
    } catch (final NumberFormatException exception) {
      return false;
    }
    return true;
  }
}
