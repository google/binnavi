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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.InstructionHighlighter;

import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * Table model of the table where the highlighting instructions are shown.
 */
public final class CResultsTableModel extends AbstractTableModel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -2539784355746290709L;

  /**
   * Index of the column where the result type is shown.
   */
  public static final int TYPE_COLUMN = 0;

  /**
   * Index of the column where the instruction address is shown.
   */
  public static final int ADDRESS_COLUMN = 1;

  /**
   * Index of the column where the instruction string is shown.
   */
  public static final int INSTRUCTION_COLUMN = 2;

  /**
   * Column names used in the model.
   */
  private static final String[] COLUMN_NAMES = {"Type", "Address", "Instruction"};

  /**
   * Model that provides the data to display.
   */
  private final CSpecialInstructionsModel m_model;

  /**
   * Updates the table model on changes to the data model.
   */
  private final ISpecialInstructionsModelListener m_listener =
      new ISpecialInstructionsModelListener() {
        @Override
        public void changedInstructions() {
          m_instructions.clear();
          m_instructions.addAll(m_model.getInstructions());

          fireTableDataChanged();
        }
      };

  /**
   * Cached list of instruction results shown in the table.
   */
  private final List<CSpecialInstruction> m_instructions;

  /**
   * Creates a new table model object.
   *
   * @param model Model that provides the data to display.
   */
  public CResultsTableModel(final CSpecialInstructionsModel model) {
    m_model = model;

    model.addListener(m_listener);

    m_instructions = model.getInstructions();
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    m_model.removeListener(m_listener);
  }

  @Override
  public Class<?> getColumnClass(final int columnIndex) {
    switch (columnIndex) {
      case TYPE_COLUMN:
        return ITypeDescription.class;
      case ADDRESS_COLUMN:
        return String.class;
      case INSTRUCTION_COLUMN:
        return String.class;
      default:
        throw new IllegalArgumentException(
            "IE02258: unexpected column index while resolving column class");
    }
  }

  @Override
  public int getColumnCount() {
    return COLUMN_NAMES.length;
  }

  @Override
  public String getColumnName(final int column) {
    return COLUMN_NAMES[column];
  }

  @Override
  public int getRowCount() {
    return m_instructions.size();
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    final CSpecialInstruction instruction = m_instructions.get(rowIndex);

    switch (columnIndex) {
      case TYPE_COLUMN:
        return instruction.getType();
      case ADDRESS_COLUMN:
        return instruction.getInstruction().getAddress().toHexString();
      case INSTRUCTION_COLUMN:
        return instruction.getInstruction().getInstructionString();
      default:
        throw new IllegalStateException("IE00668: Invalid column index given");
    }
  }
}
