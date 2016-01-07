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

import javax.swing.tree.DefaultMutableTreeNode;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.CDatabaseManager;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Database.CDatabaseNode;
import com.google.security.zynamics.zylib.gui.dndtree.DNDTree;

/**
 * Drag & Drop handler that is used to sort databases in the project tree.
 */
public final class CDatabaseSortingHandler extends CAbstractDropHandler {
  /**
   * Provides the databases to sort.
   */
  private final CDatabaseManager m_manager;

  /**
   * Creates a new handler object.
   * 
   * @param manager Provides the databases to sort.
   */
  public CDatabaseSortingHandler(final CDatabaseManager manager) {
    super(CModuleTransferable.MODULE_FLAVOR);

    m_manager = Preconditions.checkNotNull(manager, "IE01926: Manager argument can not be null");
  }

  @Override
  public boolean canHandle(final DefaultMutableTreeNode parentNode,
      final DefaultMutableTreeNode draggedNode) {
    return (parentNode instanceof CDatabaseNode) && (draggedNode instanceof CDatabaseNode)
        && (parentNode != draggedNode);
  }

  @Override
  public boolean canHandle(final DefaultMutableTreeNode parentNode, final Object data) {
    return false;
  }

  @Override
  public void drop(final DefaultMutableTreeNode parentNode, final Object data) {
    // Can not drop from table to tree because databases are never shown in tables
  }

  @Override
  public void drop(final DNDTree target, final DefaultMutableTreeNode parentNode,
      final DefaultMutableTreeNode draggedNode) {
    final IDatabase draggedDatabase = ((CDatabaseNode) draggedNode).getObject();
    final IDatabase parentDatabase = ((CDatabaseNode) parentNode).getObject();

    int pnIndex = 0;

    for (final IDatabase database : m_manager) {
      if (database == parentDatabase) {
        break;
      }

      pnIndex++;
    }

    m_manager.moveDatabase(draggedDatabase, pnIndex + 1);
  }
}
