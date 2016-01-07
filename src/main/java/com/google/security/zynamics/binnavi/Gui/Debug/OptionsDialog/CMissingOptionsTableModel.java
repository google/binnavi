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
package com.google.security.zynamics.binnavi.Gui.Debug.OptionsDialog;

import javax.swing.table.AbstractTableModel;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerOptions;

/**
 * Table model used to display the available debugger options when a debugger is started for the
 * first time.
 */
public final class CMissingOptionsTableModel extends AbstractTableModel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -7248890252459352865L;

  /**
   * Titles of the columns shown in the table.
   */
  private final String[] m_columns = new String[] {"Option", "Status"};

  /**
   * Data shown in the table.
   */
  private final String[][] m_entries;

  /**
   * Creates a new table model object.
   *
   * @param options The debugger options object that provides the table data.
   */
  public CMissingOptionsTableModel(final DebuggerOptions options) {
    Preconditions.checkNotNull(options, "IE01465: Options argument can not be null");

    m_entries = new String[16][2];

    int index = 0;

    index = setRow(index, true, "Step Into");
    index = setRow(index, true, "Step Over");
    index = setRow(index, true, "Step to next block");
    index = setRow(index, true, "Trace mode");
    index = setRow(index, true, "Modify registers");
    index = setRow(index, options.canDetach(), "Detach from target");
    index = setRow(index, options.canMemmap(), "Show map of allocated memory");
    index = setRow(index, options.canValidMemory(), "Automatically find memory ranges");
    index = setRow(index, options.canMultithread(), "Multithreading support");
    index = setRow(index, options.canTerminate(), "Terminate target");
    index = setRow(index, options.canHalt(), "Halt target");
    index = setRow(index, options.mustHaltBeforeCommunicating(), "Halt before communicating");
    index = setRow(index, options.canSoftwareBreakpoint(), "Use software breakpoints");
    index = setRow(index, options.isStackAvailable(), "Stack view available");
    index = setRow(index, options.getBreakpointCounter(), "Maximum number of active breakpoints");
    setRow(index, options.getPageSize(), "Page size");
  }

  /**
   * Sets the content of a table row.
   *
   * @param index Index of the row.
   * @param supported True, if the option in the indexed row is supported. False, otherwise.
   * @param value Name of the option shown in the index tabled.
   *
   * @return The next available index.
   */
  private int setRow(final int index, final boolean supported, final String value) {
    m_entries[index][0] = value;
    m_entries[index][1] = supported ? "Supported" : "Not supported";

    return index + 1;
  }

  /**
   * Sets the content of a table row.
   *
   * @param index Index of the row.
   * @param value Amount of the supported option.
   * @param description Name of the option shown in the index tabled.
   *
   * @return The next available index.
   */
  private int setRow(final int index, final int value, final String description) {
    m_entries[index][0] = description;
    m_entries[index][1] = String.valueOf(value);

    return index + 1;
  }

  @Override
  public int getColumnCount() {
    return m_columns.length;
  }

  @Override
  public String getColumnName(final int index) {
    return m_columns[index];
  }

  @Override
  public int getRowCount() {
    return m_entries.length;
  }

  @Override
  public Object getValueAt(final int row, final int col) {
    return m_entries[row][col];
  }
}
