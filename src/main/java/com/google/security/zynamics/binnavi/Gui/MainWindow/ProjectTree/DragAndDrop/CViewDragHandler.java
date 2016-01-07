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
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Views.Component.CViewsTable;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;

/**
 * Drag & Drop handler that enables D&D support for view tables.
 */
public final class CViewDragHandler extends TransferHandler {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 2261869070089411831L;

  /**
   * The views table where the drag operation began.
   */
  private final CViewsTable m_table;

  /**
   * Creates a new drag and drop handler for a given views table.
   * 
   * @param table The views table that support drag and dropping.
   */
  public CViewDragHandler(final CViewsTable table) {
    Preconditions.checkNotNull(table, "IE01932: Table argument can't be null");

    m_table = table;
  }

  @Override
  protected Transferable createTransferable(final JComponent component) {
    // When the drag & drop operation begins, we determine what
    // rows are selected and then we create a D&D transferable
    // object that contains the corresponding views.

    final int[] rows = m_table.getSelectedRows();

    final List<INaviView> views = new ArrayList<INaviView>();

    for (final int i : rows) {
      final int index = m_table.convertRowIndexToModel(i);
      views.add(m_table.getTreeTableModel().getViews().get(index));
    }

    return new CViewTransferable(views);
  }

  @Override
  public int getSourceActions(final JComponent component) {
    return COPY_OR_MOVE;
  }
}
