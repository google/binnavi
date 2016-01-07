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
package com.google.security.zynamics.binnavi.Gui.FilterPanel;

import com.google.security.zynamics.zylib.general.Pair;

import java.util.Comparator;
import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * Table model used for tables that can be filtered.
 *
 * @param <T> Elements shown in the table.
 */
public abstract class CFilteredTableModel<T> extends AbstractTableModel implements
    IFilteredTableModel<T> {
  /**
   * Currently active filter. This element can be null if no filter is active.
   */
  private IFilter<T> m_filter = null;

  /**
   * Returns the currently active filter.
   *
   * @return The currently active filter.
   */
  protected IFilter<T> getFilter() {
    return m_filter;
  }

  /**
   * This function is called when the node the table belongs to is removed from the tree. In this
   * function the model has the opportunity to clean up resources like listeners.
   */
  public abstract void delete();

  /**
   * Returns a list of all sorters that specify how the table sorter should handle the sorting of
   * special columns. Each pair of the list contains the index of the column and the comparator that
   * is used to sort that column.
   *
   * @return A list of comparators that are used with the given columns.
   */
  public abstract List<Pair<Integer, Comparator<?>>> getSorters();

  /**
   * Sets the currently active filter.
   *
   * @param filter The new filter or null to remove any filtering.
   */
  @Override
  public void setFilter(final IFilter<T> filter) {
    m_filter = filter;

    fireTableDataChanged();
  }
}
