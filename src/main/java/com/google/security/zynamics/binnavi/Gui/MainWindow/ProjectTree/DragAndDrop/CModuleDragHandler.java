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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.DragAndDrop;

import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.TransferHandler;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.ModuleContainer.Component.CModulesTable;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;

/**
 * Drag & Drop handler that enables D&D support for module tables.
 */
public final class CModuleDragHandler extends TransferHandler {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -8475109130068494109L;

  /**
   * The views table where the drag operation began.
   */
  private final CModulesTable m_table;

  /**
   * Creates a new drag and drop handler for a given views table.
   * 
   * @param table The modules table that support drag and dropping.
   */
  public CModuleDragHandler(final CModulesTable table) {
    Preconditions.checkNotNull(table, "IE01927: Table argument can't be null");

    m_table = table;
  }

  @Override
  protected Transferable createTransferable(final JComponent component) {
    // When the drag & drop operation begins, we determine what
    // rows are selected and then we create a D&D transferable
    // object that contains the corresponding views.

    final int[] rows = m_table.getSelectedRows();

    final List<INaviModule> modules = new ArrayList<INaviModule>();

    for (final int i : rows) {
      final int index = m_table.convertRowIndexToModel(i);
      modules.add(m_table.getTreeTableModel().getModules().get(index));
    }

    return new CModuleTransferable(modules);
  }

  @Override
  public int getSourceActions(final JComponent component) {
    return COPY_OR_MOVE;
  }
}
