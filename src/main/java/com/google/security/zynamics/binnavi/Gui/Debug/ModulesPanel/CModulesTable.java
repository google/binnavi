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

import com.google.security.zynamics.binnavi.Gui.Debug.ModulesPanel.Help.CMemoryModulesTableHelp;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.CFilteredTable;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryModule;

/**
 * Table where the memory modules in a debugged target process are shown.
 */
public final class CModulesTable extends CFilteredTable<MemoryModule> {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 7348197605517002979L;

  /**
   * Used to render the table.
   */
  private final CModulesTableRenderer m_renderer = new CModulesTableRenderer(this);

  /**
   * Creates a new table object.
   */
  public CModulesTable() {
    super(new CModulesTableModel(), new CMemoryModulesTableHelp());

    setDefaultRenderer(Object.class, m_renderer);
  }

  @Override
  public void dispose() {
    // Nothing to dispose
  }

  /**
   * Returns the table renderer.
   *
   * @return The table renderer.
   */
  public CModulesTableRenderer getDefaultRenderer() {
    return m_renderer;
  }

  @Override
  public CModulesTableModel getTreeTableModel() {
    return (CModulesTableModel) super.getTreeTableModel();
  }
}
