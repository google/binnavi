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
package com.google.security.zynamics.binnavi.Gui.Debug.ModulesPanel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;

import com.google.security.zynamics.binnavi.Gui.FilterPanel.CFilteredTableModel;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.IFilter;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryModule;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.general.comparators.HexStringComparator;
import com.google.security.zynamics.zylib.general.comparators.LongComparator;
import com.google.security.zynamics.zylib.types.lists.FilledList;
import com.google.security.zynamics.zylib.types.lists.IFilledList;



/**
 * Table model of the table where the modules in the address space of a debugged target process are
 * shown.
 */
public final class CModulesTableModel extends CFilteredTableModel<MemoryModule> {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 1926202151838011045L;

  /**
   * Index of the column where the module names are shown.
   */
  private static final int NAME_COLUMN = 0;

  /**
   * Index of the column where the module base addresses are shown.
   */
  private static final int ADDRESS_COLUMN = 1;

  /**
   * Index of the column where the module sizes are shown.
   */
  private static final int SIZE_COLUMN = 2;

  /**
   * Titles of the columns of this model.
   */
  private static final String[] COLUMN_NAMES = {"Name", "Base Address", "Size"};

  /**
   * The modules shown by the model.
   */
  private final IFilledList<MemoryModule> m_modules = new FilledList<MemoryModule>();

  /**
   * The displayed modules are cached for performance reasons.
   */
  private List<MemoryModule> m_cachedValues = null;

  /**
   * Makes sure that only one thread has access to the cached values list at any given time.
   */
  private final Semaphore m_cachedValuesSemaphore = new Semaphore(1);

  /**
   * Flag that days whether full paths to modules or only module names should be shown.
   */
  private boolean m_useFullPaths;

  /**
   * Adds a module to be shown.
   * 
   * @param module The module to be shown.
   */
  public void addModule(final MemoryModule module) {
    m_cachedValuesSemaphore.acquireUninterruptibly();

    m_modules.add(module);
    m_cachedValues = null;

    m_cachedValuesSemaphore.release();

    fireTableDataChanged();
  }

  @Override
  public void delete() {
    // Nothing to dispose
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
   * Returns the currently shown modules.
   * 
   * @return The currently shown modules.
   */
  public IFilledList<MemoryModule> getModules() {
    m_cachedValuesSemaphore.acquireUninterruptibly();

    if (m_cachedValues == null) {
      final IFilter<MemoryModule> filter = getFilter();

      if (filter == null) {
        m_cachedValues = m_modules;
      } else {
        m_cachedValues = filter.get(m_modules);
      }
    }

    final FilledList<MemoryModule> returnValue = new FilledList<MemoryModule>(m_cachedValues);

    m_cachedValuesSemaphore.release();

    return returnValue;
  }

  @Override
  public int getRowCount() {
    return getModules().size();
  }

  @Override
  public List<Pair<Integer, Comparator<?>>> getSorters() {
    final List<Pair<Integer, Comparator<?>>> sorters =
        new ArrayList<Pair<Integer, Comparator<?>>>();

    sorters.add(new Pair<Integer, Comparator<?>>(ADDRESS_COLUMN, new HexStringComparator()));
    sorters.add(new Pair<Integer, Comparator<?>>(SIZE_COLUMN, new LongComparator()));

    return sorters;
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    switch (columnIndex) {
      case NAME_COLUMN:
        return m_useFullPaths ? getModules().get(rowIndex).getPath() : getModules().get(rowIndex)
            .getName();
      case ADDRESS_COLUMN:
        return getModules().get(rowIndex).getBaseAddress().getAddress().toHexString();
      case SIZE_COLUMN:
        return Long.valueOf(getModules().get(rowIndex).getSize());
      default:
        return null;
    }
  }

  /**
   * Removes a module from table model.
   * 
   * @param module The module to remove.
   */
  public void removeModule(final MemoryModule module) {
    m_cachedValuesSemaphore.acquireUninterruptibly();

    m_modules.remove(module);
    m_cachedValues = null;

    m_cachedValuesSemaphore.release();

    fireTableDataChanged();
  }

  /**
   * Resets the table model.
   */
  public void reset() {
    m_cachedValuesSemaphore.acquireUninterruptibly();

    m_modules.clear();
    m_cachedValues = null;

    m_cachedValuesSemaphore.release();

    fireTableDataChanged();
  }

  @Override
  public void setFilter(final IFilter<MemoryModule> filter) {
    m_cachedValuesSemaphore.acquireUninterruptibly();

    m_cachedValues = null;

    m_cachedValuesSemaphore.release();

    super.setFilter(filter);
  }

  /**
   * Controls whether the modules column should contain full module paths or only module names
   * 
   * @param useFullPaths The boolean flag to control the path setting
   */
  public void setUseFullModulePaths(final boolean useFullPaths) {
    m_useFullPaths = useFullPaths;

    m_cachedValuesSemaphore.acquireUninterruptibly();

    m_cachedValues = null;

    m_cachedValuesSemaphore.release();

    fireTableDataChanged();
  }
}
