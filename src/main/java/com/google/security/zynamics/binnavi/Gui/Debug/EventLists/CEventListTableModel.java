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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.Debug.EventLists.Implementations.CTraceFunctions;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.CFilteredTableModel;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.IFilter;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;
import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceEvent;
import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceListListener;
import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceListProvider;
import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceManagerListener;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.general.comparators.IntComparator;


/**
 * Table model for the table that is used to display trace lists.
 */
public final class CEventListTableModel extends CFilteredTableModel<TraceList> {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -1005721402880762292L;

  /**
   * Index of the column where trace names are shown.
   */
  private static final int NAME_COLUMN = 0;

  /**
   * Index of the column that shows the number of events belonging to a trace.
   */
  private static final int EVENT_COLUMN = 1;

  /**
   * Index of the column that shows the trace descriptions.
   */
  private static final int DESCRIPTION_COLUMN = 2;

  /**
   * Titles of the columns used by the model.
   */
  private static final String[] COLUMNS = {"Name", "Events", "Description"};

  /**
   * Provides the traces that are displayed in the model.
   */
  private final ITraceListProvider m_traceProvider;

  /**
   * Listener that is responsible for updating the table model once the trace lists changed in a
   * significant way.
   */
  private final InternalTraceListener m_listener = new InternalTraceListener();

  /**
   * Traces are cached for performance reasons.
   */
  private List<TraceList> m_cachedValues = null;

  /**
   * Creates a new table model that displays the event lists given by an event list provider.
   *
   * @param traceProvider Provides the traces that are displayed in the model.
   */
  public CEventListTableModel(final ITraceListProvider traceProvider) {
    Preconditions.checkNotNull(traceProvider, "IE01375: Event list manager can't be null");

    m_traceProvider = traceProvider;

    m_traceProvider.addListener(m_listener);

    if (traceProvider.isLoaded()) {
      for (final TraceList traceList : traceProvider) {
        traceList.addListener(m_listener);
      }
    }
  }

  @Override
  public void delete() {
    m_traceProvider.removeListener(m_listener);

    if (m_traceProvider.isLoaded()) {
      for (final TraceList traceList : m_traceProvider) {
        traceList.removeListener(m_listener);
      }
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

  @Override
  public int getRowCount() {
    return getTraces().size();
  }

  @Override
  public List<Pair<Integer, Comparator<?>>> getSorters() {
    final List<Pair<Integer, Comparator<?>>> sorters =
        new ArrayList<Pair<Integer, Comparator<?>>>();

    sorters.add(new Pair<Integer, Comparator<?>>(EVENT_COLUMN, new IntComparator()));

    return sorters;
  }

  /**
   * Returns the traces currently shown in the table.
   *
   * @return The traces currently shown in the table.
   */
  public List<TraceList> getTraces() {
    List<TraceList> localCachedValues = m_cachedValues;

    if (localCachedValues == null) {
      if (m_traceProvider.isLoaded()) {
        final IFilter<TraceList> filter = getFilter();

        if (filter == null) {
          localCachedValues = m_traceProvider.getTraces();
        } else {
          localCachedValues = filter.get(m_traceProvider.getTraces());
        }
      } else {
        localCachedValues = new ArrayList<TraceList>();
      }
    }

    m_cachedValues = localCachedValues;
    return new ArrayList<TraceList>(localCachedValues);
  }

  @Override
  public Object getValueAt(final int row, final int col) {
    final TraceList debugEvent = getTraces().get(row);

    switch (col) {
      case NAME_COLUMN:
        return debugEvent.getName();
      case EVENT_COLUMN:
        return Integer.valueOf(debugEvent.getEventCount());
      case DESCRIPTION_COLUMN:
        return debugEvent.getDescription();
      default:
        throw new IllegalStateException("IE01119: Unkown column");
    }
  }

  @Override
  public boolean isCellEditable(final int row, final int col) {
    return col != EVENT_COLUMN;
  }

  @Override
  public void setFilter(final IFilter<TraceList> filter) {
    m_cachedValues = null;

    super.setFilter(filter);
  }

  @Override
  public void setValueAt(final Object value, final int row, final int col) {
    final TraceList eventList = getTraces().get(row);

    if (col == NAME_COLUMN) {
      CTraceFunctions.setTraceName(null, eventList, (String) value);
    } else if (col == DESCRIPTION_COLUMN) {
      CTraceFunctions.setTraceDescription(null, eventList, (String) value);
    }
  }

  /**
   * Updates the table model once significant changes happen in the observed event list.
   */
  private class InternalTraceListener implements ITraceManagerListener, ITraceListListener {
    @Override
    public void addedTrace(final TraceList list) {
      m_cachedValues = null;

      list.addListener(m_listener);

      fireTableDataChanged();
    }

    @Override
    public void changedDescription(final TraceList traceList) {
      m_cachedValues = null;

      fireTableDataChanged();
    }

    @Override
    public void changedName(final TraceList traceList) {
      m_cachedValues = null;

      fireTableDataChanged();
    }

    @Override
    public void eventAdded(final TraceList trace, final ITraceEvent event) {
      m_cachedValues = null;

      fireTableDataChanged();
    }

    @Override
    public void loaded() {
      for (final TraceList traceList : m_traceProvider) {
        traceList.addListener(m_listener);
      }

      m_cachedValues = null;

      fireTableDataChanged();
    }

    @Override
    public void removedTrace(final TraceList list) {
      m_cachedValues = null;

      list.removeListener(m_listener);

      fireTableDataChanged();
    }
  }
}
