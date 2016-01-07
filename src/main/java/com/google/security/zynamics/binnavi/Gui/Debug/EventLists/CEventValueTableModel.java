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
package com.google.security.zynamics.binnavi.Gui.Debug.EventLists;

import javax.swing.table.AbstractTableModel;

import com.google.security.zynamics.binnavi.debug.models.trace.TraceRegister;
import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceEvent;


/**
 * Table model for displaying the register values of events.
 */
public final class CEventValueTableModel extends AbstractTableModel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 4788903975097482416L;

  /**
   * Names of the columns of this model.
   */
  private static final String[] COLUMN_NAMES = {"Register", "Value", "Memory"};

  /**
   * Index of the column where the register names are shown.
   */
  private static final int NAME_COLUMN = 0;

  /**
   * Index of the column where the register values are shown.
   */
  private static final int VALUE_COLUMN = 1;

  /**
   * Index of the column where the memory values are shown.
   */
  public static final int MEMORY_COLUMN = 2;

  /**
   * Event that provides the data or null.
   */
  private ITraceEvent event = null;

  /**
   * Turns a byte array into a printable string.
   *
   * @param memory The byte array.
   *
   * @return The printable string.
   */
  private String getMemoryString(final byte[] memory) {
    final StringBuffer stringBuffer = new StringBuffer();

    for (int i = 0; i < memory.length; i++) {
      stringBuffer.append(String.format("%02X", memory[i]));

      if (i != memory.length - 1) {
        stringBuffer.append(' ');
      }
    }

    return stringBuffer.toString();
  }

  @Override
  public int getColumnCount() {
    return COLUMN_NAMES.length;
  }

  @Override
  public String getColumnName(final int column) {
    return COLUMN_NAMES[column];
  }

  /**
   * Returns the event that is shown in the table. This value can be null.
   *
   * @return The event shown in the table or null.
   */
  public ITraceEvent getEvent() {
    return event;
  }

  @Override
  public int getRowCount() {
    if (event == null) {
      return 0;
    }
    return event.getRegisterValues().size();
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    final TraceRegister value = event.getRegisterValues().get(rowIndex);

    switch (columnIndex) {
      case NAME_COLUMN:
        return value.getName();
      case VALUE_COLUMN:
        return value.getValue().toHexString();
      case MEMORY_COLUMN:
        return getMemoryString(value.getMemory());
      default:
        throw new IllegalStateException("IE00609: Invalid column name");
    }
  }

  /**
   * Sets the event whose values are displayed in the table.
   *
   * @param event The event or null if no event values should be shown.
   */
  public void setEvent(final ITraceEvent event) {
    this.event = event;
    fireTableDataChanged();
  }
}
