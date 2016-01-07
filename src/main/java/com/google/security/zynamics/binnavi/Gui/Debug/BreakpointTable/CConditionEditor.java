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
package com.google.security.zynamics.binnavi.Gui.Debug.BreakpointTable;

import java.awt.Color;
import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableCellEditor;

import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointConditionParser;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.InvalidFormulaException;


/**
 * Cell editor that makes sure that all breakpoint conditions are valid.
 */
public class CConditionEditor extends AbstractCellEditor implements TableCellEditor {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -8534932082343485672L;

  /**
   * Text field used for editing formulas.
   */
  private final JTextField inputField = new JTextField();

  /**
   * Keeps track of changes in the input field.
   */
  private final DocumentListener m_documentListener = new InternalDocumentListener();

  /**
   * Creates a new editor object.
   */
  public CConditionEditor() {
    inputField.getDocument().addDocumentListener(m_documentListener);
  }

  @Override
  public Object getCellEditorValue() {
    return inputField.getText();
  }

  @Override
  public Component getTableCellEditorComponent(final JTable table, final Object value,
      final boolean isSelected, final int row, final int column) {
    inputField.setText((String) value);

    return inputField;
  }

  @Override
  public boolean stopCellEditing() {
    try {
      BreakpointConditionParser.evaluate(inputField.getText());

      fireEditingStopped();
    } catch (final InvalidFormulaException e) {
      // Keep the cell open in case of invalid formulas
    }

    return true;
  }

  /**
   * Keeps track of changes in the input field.
   */
  private class InternalDocumentListener implements DocumentListener {
    /**
     * Updates the color of the input field depending on whether the entered formula string is valid
     * or not.
     */
    private void updateColor() {
      try {
        BreakpointConditionParser.evaluate(inputField.getText());

        inputField.setBackground(Color.WHITE);
      } catch (final InvalidFormulaException e) {
        inputField.setBackground(Color.RED);
      }
    }

    @Override
    public void changedUpdate(final DocumentEvent event) {
      updateColor();
    }

    @Override
    public void insertUpdate(final DocumentEvent event) {
      updateColor();
    }

    @Override
    public void removeUpdate(final DocumentEvent event) {
      updateColor();
    }
  }
}
