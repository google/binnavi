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
package com.google.security.zynamics.binnavi.Gui.Debug.ThreadInformationPanel;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ThreadListenerAdapter;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ThreadState;
import com.google.security.zynamics.binnavi.debug.models.processmanager.interfaces.ThreadListener;


/**
 * Table model that shows thread information.
 */
public class CThreadInformationTableModel extends AbstractTableModel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 6479629458211965718L;

  /**
   * Index of the column where thread IDs are shown.
   */
  private static final int TID_COLUMN = 0;

  /**
   * Index of the column where the thread states are shown.
   */
  private static final int STATE_COLUMN = 1;

  /**
   * Names of the table columns.
   */
  private static final String[] COLUMN_NAMES = new String[] {"Thread ID", "State"};

  /**
   * Threads currently shown in the table.
   */
  private final List<TargetProcessThread> m_threads = new ArrayList<TargetProcessThread>();

  /**
   * Updates the table on changes to the thread.
   */
  private final ThreadListener m_threadListener = new ThreadListenerAdapter() {
    @Override
    public void stateChanged(final TargetProcessThread thread) {
      fireTableDataChanged();
    }
  };

  /**
   * Adds a thread to show.
   *
   * @param thread The thread to show.
   */
  public void addThread(final TargetProcessThread thread) {
    synchronized (m_threads) {
      thread.addListener(m_threadListener);

      m_threads.add(thread);
    }

    fireTableDataChanged();
  }

  @Override
  public int getColumnCount() {
    return COLUMN_NAMES.length;
  }

  @Override
  public String getColumnName(final int col) {
    return COLUMN_NAMES[col];
  }

  @Override
  public int getRowCount() {
    synchronized (m_threads) {
      return m_threads.size();
    }
  }

  /**
   * Returns the threads shown in the table.
   *
   * @return The threads shown in the table.
   */
  public List<TargetProcessThread> getThreads() {
    synchronized (m_threads) {
      return new ArrayList<TargetProcessThread>(m_threads);
    }
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    try {
      synchronized (m_threads) {
        final TargetProcessThread thread = m_threads.get(rowIndex);

        switch (columnIndex) {
          case TID_COLUMN:
            return thread.getThreadId();
          case STATE_COLUMN:
            return thread.getState() == ThreadState.RUNNING ? "Running" : "Suspended";
          default:
            throw new IllegalStateException("IE00651: Unknown column");
        }
      }
    } catch (final IndexOutOfBoundsException exception) {
      // This can happen if threads are removed between getColumnCount and getValueAt.

      return "";
    }
  }

  /**
   * Removes a thread from the table.
   *
   * @param thread The thread to remove.
   */
  public void removeThread(final TargetProcessThread thread) {
    synchronized (m_threads) {
      thread.removeListener(m_threadListener);

      m_threads.remove(thread);
    }

    fireTableDataChanged();
  }

  /**
   * Resets the table model.
   */
  public void reset() {
    synchronized (m_threads) {
      for (final TargetProcessThread thread : m_threads) {
        thread.removeListener(m_threadListener);
      }

      m_threads.clear();
    }

    fireTableDataChanged();
  }

}
