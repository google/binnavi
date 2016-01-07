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

import com.google.security.zynamics.binnavi.Gui.FilterPanel.CFilteredTableModel;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.IFilter;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;
import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceEvent;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.general.comparators.LongComparator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * This table model is used to display the individual events of an event list in a JTable.
 */
public final class CEventTableModel extends CFilteredTableModel<ITraceEvent> {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 1388925809132144996L;

  /**
   * Titles of the columns of the table.
   */
  private static final String[] COLUMNS = {"#", "Thread ID", "Module", "Address",};

  /**
   * Index of the column where event indices are shown.
   */
  private static final int INDEX_COLUMN = 0;

  /**
   * Index of the column where Thread IDs are shown.
   */
  private static final int THREAD_COLUMN = 1;

  /**
   * Index of the column where event modules are shown.
   */
  private static final int MODULE_COLUMN = 2;

  /**
   * Index of the column where event addresses are shown.
   */
  private static final int ADDRESS_COLUMN = 3;

  /**
   * The event list to display.
   */
  private TraceList m_eventList;

  /**
   * Cached values shown in the table.
   */
  private List<ITraceEvent> m_cachedValues = null;

  @Override
  public void delete() {
    // Empty default implementation
  }

  @Override
  public Class<?> getColumnClass(final int columnIndex) {
    switch (columnIndex) {
      case 0:
        return Integer.class;
      case 1:
        return String.class;
      case 2:
        return String.class;
      case 3:
        return String.class;
      default:
        return String.class;
    }
  }

  @Override
  public int getColumnCount() {
    return COLUMNS.length;
  }

  @Override
  public String getColumnName(final int column) {
    return COLUMNS[column];
  }

  /**
   * Returns the events shown in the table.
   *
   * @return The events shown in the table.
   */
  public List<ITraceEvent> getEvents() {
    // A local copy is needed here for thread safety.
    List<ITraceEvent> localCachedValues = m_cachedValues;

    if (localCachedValues == null) {
      localCachedValues = new ArrayList<ITraceEvent>();

      if (m_eventList == null) {
        m_cachedValues = localCachedValues;
        return localCachedValues;
      }

      final IFilter<ITraceEvent> filter = getFilter();

      if (filter == null) {
        localCachedValues = m_eventList.getEvents();
      } else {
        localCachedValues = filter.get(m_eventList.getEvents());
      }
    }

    m_cachedValues = localCachedValues;
    return localCachedValues;
  }

  @Override
  public int getRowCount() {
    return getEvents().size();
  }

  @Override
  public List<Pair<Integer, Comparator<?>>> getSorters() {
    final Pair<Integer, Comparator<?>> pair =
        new Pair<Integer, Comparator<?>>(CEventTableModel.THREAD_COLUMN, new LongComparator());
    final ArrayList<Pair<Integer, Comparator<?>>> list =
        new ArrayList<Pair<Integer, Comparator<?>>>();
    list.add(pair);
    return list;
  }

  @Override
  public Object getValueAt(final int row, final int col) {
    final ITraceEvent debugEvent = getEvents().get(row);
    final BreakpointAddress address = debugEvent.getOffset();

    switch (col) {
      case INDEX_COLUMN:
        return row + 1;
      case THREAD_COLUMN:
        return Long.valueOf(debugEvent.getThreadId());
      case MODULE_COLUMN:
        return address.getModule() == null ? "-" : address.getModule().getConfiguration().getName();
      case ADDRESS_COLUMN: {
      if (address.getModule() == null) {
        return address.getAddress().getAddress().toHexString();
      } else {
        final INaviFunction function = address.getModule().isLoaded() ? address.getModule()
            .getContent().getFunctionContainer().getFunction(address.getAddress().getAddress())
            : null;

        if (function == null) {
          return address.getAddress().getAddress().toHexString();
        } else {
          return function.getName();
        }
      }
    }
      default:
        throw new IllegalStateException("IE01121: Unknown column");
    }
  }

  /**
   * Determines whether a cell is editable or not.
   *
   * @param row Row of the cell.
   * @param col Column of the cell.
   *
   * @return A flag that signals whether the specified cell is editable or not.
   */
  @Override
  public boolean isCellEditable(final int row, final int col) {
    return false;
  }

  /**
   * Sets the event list that is displayed in the table.
   *
   * @param list The event list to display.
   */
  public void setEventList(final TraceList list) {
    m_eventList = list;

    m_cachedValues = null;

    fireTableDataChanged();
  }

  @Override
  public void setFilter(final IFilter<ITraceEvent> filter) {
    m_cachedValues = null;

    super.setFilter(filter);
  }
}
