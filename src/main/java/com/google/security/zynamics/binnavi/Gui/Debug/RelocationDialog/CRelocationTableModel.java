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
package com.google.security.zynamics.binnavi.Gui.Debug.RelocationDialog;

import javax.swing.table.AbstractTableModel;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryModule;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.types.lists.FilledList;
import com.google.security.zynamics.zylib.types.lists.IFilledList;

/**
 * Table model that is used to display incorrectly mapped modules.
 */
public final class CRelocationTableModel extends AbstractTableModel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -5994931123546374970L;

  /**
   * Titles of columns used by the model.
   */
  private static final String[] COLUMN_NAMES =
      {"Module", "Specified Image Base", "Real Image Base"};

  /**
   * Index of the column where module names are shown.
   */
  public static final int NAME_COLUMN = 0;

  /**
   * Index of the columns where unrelocated base addresses are shown.
   */
  public static final int SPECIFIED_COLUMN = 1;

  /**
   * Index of the columns where relocated base addresses are shown.
   */
  public static final int REAL_COLUMN = 2;

  /**
   * Debugger that picked up the wrong image bases.
   */
  private final IDebugger m_debugger;

  /**
   * List of modules with wrong image bases.
   */
  private final IFilledList<Pair<INaviModule, MemoryModule>> m_wronglyPlacedModules;

  /**
   * Creates a new model object.
   * 
   * @param debugger Debugger that picked up the wrong image bases.
   * @param wronglyPlacedModules List of modules with wrong image bases.
   */
  public CRelocationTableModel(final IDebugger debugger,
      final IFilledList<Pair<INaviModule, MemoryModule>> wronglyPlacedModules) {
    Preconditions.checkNotNull(debugger, "IE01487: Debugger argument can not be null");

    Preconditions.checkNotNull(wronglyPlacedModules,
        "IE01488: Wrongly placed modules argument can not be null");

    m_debugger = debugger;
    m_wronglyPlacedModules = new FilledList<Pair<INaviModule, MemoryModule>>(wronglyPlacedModules);
  }

  @Override
  public int getColumnCount() {
    return COLUMN_NAMES.length;
  }

  @Override
  public String getColumnName(final int column) {
    return COLUMN_NAMES[column];
  }

  @Override
  public int getRowCount() {
    return m_wronglyPlacedModules.size();
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    final Pair<INaviModule, MemoryModule> pair = m_wronglyPlacedModules.get(rowIndex);

    switch (columnIndex) {
      case NAME_COLUMN:
        return pair.first().getConfiguration().getName();
      case SPECIFIED_COLUMN:
        return m_debugger
            .fileToMemory(pair.first(),
                new UnrelocatedAddress(pair.first().getConfiguration().getFileBase()))
            .getAddress().toHexString();
      case REAL_COLUMN:
        return pair.second().getBaseAddress().getAddress().toHexString();
      default:
        throw new IllegalStateException("IE01126: Unknown column");
    }
  }
}
