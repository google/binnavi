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
package com.google.security.zynamics.binnavi.Gui.Debug.RemoteBrowser.ProcessBrowser;

import javax.swing.table.AbstractTableModel;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.debug.models.processlist.ProcessList;

/**
 * Table model of the table that shows the running processes of a target system.
 */
public final class CProcessListModel extends AbstractTableModel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 8585670118441964360L;

  /**
   * Titles of the columns of this model.
   */
  private static final String COLUMN_NAMES[] = {"PID", "Name"};

  /**
   * Index of the columns where process IDs are shown.
   */
  private static final int PID_COLUMN = 0;

  /**
   * Index of the columns where process names are shown.
   */
  private static final int NAME_COLUMN = 1;

  /**
   * List of processes shown by the model.
   */
  private final ProcessList m_processes;

  /**
   * Creates a new model object.
   *
   * @param processList List of processes shown by the model.
   */
  public CProcessListModel(final ProcessList processList) {
    Preconditions.checkNotNull(processList, "IE01500: Processes argument can not be null");

    m_processes = processList;
  }

  @Override
  public int getColumnCount() {
    return 2;
  }

  @Override
  public String getColumnName(final int col) {
    return COLUMN_NAMES[col];
  }

  @Override
  public int getRowCount() {
    return m_processes.size();
  }

  @Override
  public Object getValueAt(final int row, final int col) {
    switch (col) {
      case PID_COLUMN:
        return m_processes.get(row).getPID();
      case NAME_COLUMN:
        return String.valueOf(m_processes.get(row).getName());
      default:
        throw new IllegalStateException("IE01136: Unknown column");
    }
  }
}
