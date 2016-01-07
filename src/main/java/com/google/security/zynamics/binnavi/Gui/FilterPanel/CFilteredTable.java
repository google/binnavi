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

import com.google.security.zynamics.binnavi.Help.IHelpInformation;
import com.google.security.zynamics.binnavi.Help.IHelpProvider;
import com.google.security.zynamics.zylib.general.Pair;

import java.util.Comparator;

import javax.swing.JTable;
import javax.swing.table.TableRowSorter;



/**
 * Base class for tables that can be filtered.
 * 
 * @param <T> Type of the elements shown in the table.
 */
public abstract class CFilteredTable<T> extends JTable implements IFilteredTable<T>, IHelpProvider {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -2942941726221762628L;

  /**
   * Model of the table.
   */
  private final CFilteredTableModel<T> m_model;

  /**
   * Context-sensitive help information of the table.
   */
  private final IHelpInformation m_helpInfo;

  /**
   * Sorts the entries of the table.
   */
  private final TableRowSorter<CFilteredTableModel<T>> m_tableSorter;

  /**
   * Creates a new table object.
   * 
   * @param model Model of the table.
   * @param helpInfo Context-sensitive help information of the table.
   */
  public CFilteredTable(final CFilteredTableModel<T> model, final IHelpInformation helpInfo) {
    super(model);

    m_model = model;
    m_helpInfo = helpInfo;

    // All tree tables support sorting
    m_tableSorter = new TableRowSorter<CFilteredTableModel<T>>(model);

    // Since the default sorter might not be appropriate for all columns
    // the model provides sorters for the corresponding columns.
    for (final Pair<Integer, Comparator<?>> sorter : model.getSorters()) {
      m_tableSorter.setComparator(sorter.first(), sorter.second());
    }

    setRowSorter(m_tableSorter);
  }

  /**
   * Frees allocated resources.
   */
  @Override
  public abstract void dispose();

  @Override
  public IHelpInformation getHelpInformation() {
    return m_helpInfo;
  }

  @SuppressWarnings("unchecked")
  @Override
  public CFilteredTableModel<T> getModel() {
    return (CFilteredTableModel<T>) super.getModel();
  }

  /**
   * Returns the table model of the table.
   * 
   * @return The table model of the table.
   */
  @Override
  public CFilteredTableModel<T> getTreeTableModel() {
    return m_model;
  }

  @Override
  public JTable self() {
    return this;
  }
}
