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

import javax.swing.table.AbstractTableModel;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.debug.debugger.BackEndDebuggerProvider;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointManager;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointStatus;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointType;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.interfaces.Condition;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.Pair;

/**
 * The table model used to display information about breakpoints in the breakpoint table.
 */
public final class CBreakpointTableModel extends AbstractTableModel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 1559835308928530054L;

  /**
   * Index of the column that shows the breakpoint status.
   */
  public static final int COLUMN_STATUS = 0;

  /**
   * Index of the column that shows the debugger where the breakpoint is set.
   */
  public static final int COLUMN_DEBUGGER = 1;

  /**
   * Index of the column where the not relocated address of the breakpoint is shown.
   */
  public static final int COLUMN_UNRELOCATED_ADDRESS = 2;

  /**
   * Index of the column where the relocated address of the breakpoint is shown.
   */
  public static final int COLUMN_RELOCATED_ADDRESS = 3;

  /**
   * Index of the column where the module name of the breakpoint is shown.
   */
  public static final int COLUMN_MODULE_NAME = 4;

  /**
   * Index of the column where the breakpoint description is shown.
   */
  public static final int COLUMN_CONDITION = 5;

  /**
   * Index of the column where the breakpoint description is shown.
   */
  private static final int COLUMN_DESCRIPTION = 6;

  /**
   * Names of the columns of the breakpoint table.
   */
  private final String[] columnNames = {"Status", "Debugger", "Unrelocated Address",
      "Relocated Address", "Module", "Condition", "Description"};

  /**
   * Provides all the debuggers that manage breakpoints which are displayed in the table.
   */
  private final BackEndDebuggerProvider m_debuggerProvider;

  /**
   * Creates a new breakpoint table model.
   * 
   * @param debuggerProvider Provides all the debuggers that manage breakpoints which are displayed
   *        in the table.
   */
  public CBreakpointTableModel(final BackEndDebuggerProvider debuggerProvider) {
    m_debuggerProvider =
        Preconditions.checkNotNull(debuggerProvider, "IE01339: Debugger provider can't be null");
  }

  /**
   * Calculates the total number of breakpoints shown in the table.
   * 
   * @return The total number of breakpoints shown in the table.
   */
  private int calculateBreakpoints() {
    int breakpoints = 0;

    for (final IDebugger debugger : m_debuggerProvider.getDebuggers()) {
      breakpoints += debugger.getBreakpointManager().getNumberOfBreakpoints(BreakpointType.REGULAR);
    }

    return breakpoints;
  }

  /**
   * Determines the address of the breakpoint shown in a given row.
   * 
   * @param row The row where the breakpoint is shown.
   * 
   * @return The address of the breakpoint shown in the given row.
   */
  private IAddress getBreakpointAddress(final int row) {
    final Pair<IDebugger, Integer> breakpoint =
        CBreakpointTableHelpers.findBreakpoint(m_debuggerProvider, row);

    final BreakpointManager manager = breakpoint.first().getBreakpointManager();
    final int breakpointIndex = breakpoint.second();

    return manager.getBreakpoint(BreakpointType.REGULAR, breakpointIndex).getAddress().getAddress()
        .getAddress();
  }

  /**
   * Returns the breakpoint condition for the breakpoint in the given row.
   * 
   * @param row Row index of the breakpoint.
   * 
   * @return Formula string of the breakpoint condition.
   */
  private String getBreakpointCondition(final int row) {
    final Pair<IDebugger, Integer> breakpoint =
        CBreakpointTableHelpers.findBreakpoint(m_debuggerProvider, row);

    final BreakpointManager manager = breakpoint.first().getBreakpointManager();
    final int breakpointIndex = breakpoint.second();

    final Condition condition =
        manager.getBreakpoint(BreakpointType.REGULAR, breakpointIndex).getCondition();

    return condition == null ? "" : condition.toString();
  }

  /**
   * Determines the description of the breakpoint shown in a given row.
   * 
   * @param row The row where the breakpoint is shown.
   * 
   * @return The description of the breakpoint shown in the given row.
   */
  private String getBreakpointDescription(final int row) {
    final Pair<IDebugger, Integer> breakpoint =
        CBreakpointTableHelpers.findBreakpoint(m_debuggerProvider, row);

    final BreakpointManager manager = breakpoint.first().getBreakpointManager();
    final int breakpointIndex = breakpoint.second();

    return manager.getBreakpoint(BreakpointType.REGULAR, breakpointIndex).getDescription();
  }

  /**
   * Determines the name of the module where the breakpoint resides.
   * 
   * @param row The row where the breakpoint is shown.
   * 
   * @return The name of the module where the breakpoint resides.
   */
  private String getBreakpointModuleName(final int row) {
    final Pair<IDebugger, Integer> breakpoint =
        CBreakpointTableHelpers.findBreakpoint(m_debuggerProvider, row);

    final BreakpointManager manager = breakpoint.first().getBreakpointManager();
    final int breakpointIndex = breakpoint.second();

    return (manager.getBreakpoint(BreakpointType.REGULAR, breakpointIndex).getAddress())
        .getModule().getConfiguration().getName();
  }

  /**
   * Determines the breakpoint status of the breakpoint shown in a given row.
   * 
   * @param row The row where the breakpoint is shown.
   * 
   * @return The breakpoint status of the breakpoint shown in the given row.
   */
  private BreakpointStatus getBreakpointStatus(final int row) {
    final Pair<IDebugger, Integer> breakpoint =
        CBreakpointTableHelpers.findBreakpoint(m_debuggerProvider, row);

    final BreakpointManager manager = breakpoint.first().getBreakpointManager();
    final int breakpointIndex = breakpoint.second();

    return manager.getBreakpointStatus(BreakpointType.REGULAR, breakpointIndex);
  }

  /**
   * Determines the debugger of the breakpoint shown in a given row.
   * 
   * @param row The row where the breakpoint is shown.
   * 
   * @return The debugger of the breakpoint shown in the given row.
   */
  private String getDebuggerString(final int row) {
    final Pair<IDebugger, Integer> breakpoint =
        CBreakpointTableHelpers.findBreakpoint(m_debuggerProvider, row);

    for (final IDebugger debugger : m_debuggerProvider) {
      if (debugger.getBreakpointManager() == breakpoint.first().getBreakpointManager()) {
        return debugger.getPrintableString();
      }
    }

    return null;
  }

  /**
   * Determines the relocated address of the breakpoint.
   * 
   * @param row The row where the relocated breakpoint is shown.
   * 
   * @return the relocated address of the breakpoint
   */
  private IAddress getRelocatedBreakpointAddress(final int row) {
    final Pair<IDebugger, Integer> breakpoint =
        CBreakpointTableHelpers.findBreakpoint(m_debuggerProvider, row);

    final BreakpointManager manager = breakpoint.first().getBreakpointManager();
    final int breakpointIndex = breakpoint.second();

    return breakpoint
        .first()
        .fileToMemory(
            (manager.getBreakpoint(BreakpointType.REGULAR, breakpointIndex).getAddress())
                .getModule(),
            manager.getBreakpoint(BreakpointType.REGULAR, breakpointIndex).getAddress()
                .getAddress()).getAddress();
  }

  /**
   * Updates a breakpoint condition.
   * 
   * @param manager Manager that holds the breakpoint to be updated.
   * @param breakpointIndex Index of the breakpoint to be updated.
   * @param formula Condition formula.
   */
  private void updateCondition(final BreakpointManager manager, final int breakpointIndex,
      final String formula) {
    manager.setBreakpointCondition(breakpointIndex, formula);
  }

  @Override
  public int getColumnCount() {
    return columnNames.length;
  }

  @Override
  public String getColumnName(final int col) {
    return columnNames[col];
  }

  @Override
  public int getRowCount() {
    return calculateBreakpoints();
  }

  @Override
  public Object getValueAt(final int row, final int column) {
    switch (column) {
      case COLUMN_STATUS:
        return getBreakpointStatus(row);
      case COLUMN_DEBUGGER:
        return getDebuggerString(row);
      case COLUMN_UNRELOCATED_ADDRESS:
        return getBreakpointAddress(row);
      case COLUMN_RELOCATED_ADDRESS:
        return getRelocatedBreakpointAddress(row);
      case COLUMN_MODULE_NAME:
        return getBreakpointModuleName(row);
      case COLUMN_CONDITION:
        return getBreakpointCondition(row);
      case COLUMN_DESCRIPTION:
        return getBreakpointDescription(row);
      default:
        throw new IllegalArgumentException("IE01340: Unknown column");
    }
  }

  @Override
  public boolean isCellEditable(final int row, final int column) {
    return (column == COLUMN_CONDITION) || (column == COLUMN_DESCRIPTION);
  }

  @Override
  public void setValueAt(final Object value, final int row, final int col) {
    final Pair<IDebugger, Integer> breakpoint =
        CBreakpointTableHelpers.findBreakpoint(m_debuggerProvider, row);

    final BreakpointManager manager = breakpoint.first().getBreakpointManager();
    final int breakpointIndex = breakpoint.second();

    switch (col) {
      case COLUMN_CONDITION:
        updateCondition(manager, breakpointIndex, value.toString());
        break;
      case COLUMN_DESCRIPTION:
        manager.getBreakpoint(BreakpointType.REGULAR, breakpointIndex).setDescription(
            value.toString());
        break;
    }

    fireTableCellUpdated(row, col);
  }
}
