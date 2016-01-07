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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JTree;

import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Database.CDatabaseNode;


/**
 * Contains helper clases for working with project tree nodes.
 */
public final class CProjectTreeNodeHelpers {
  /**
   * You are not supposed to instantiate this class.
   */
  private CProjectTreeNodeHelpers() {
  }

  /**
   * Finds the project tree node that represents to a given database.
   * 
   * @param tree The project tree of the main window.
   * @param database The database to search for.
   * 
   * @return The node that represents the given database.
   */
  public static CProjectTreeNode<?> findDatabaseNode(final JTree tree, final IDatabase database) {
    final List<CProjectTreeNode<?>> nodes = new ArrayList<CProjectTreeNode<?>>();

    nodes.add((CProjectTreeNode<?>) tree.getModel().getRoot());

    while (!nodes.isEmpty()) {
      final CProjectTreeNode<?> current = nodes.get(0);
      nodes.remove(0);

      if ((current instanceof CDatabaseNode) && (((CDatabaseNode) current).getObject() == database)) {
        return current;
      }

      for (final Enumeration<?> e = current.children(); e.hasMoreElements();) {
        nodes.add((CProjectTreeNode<?>) e.nextElement());
      }
    }

    throw new IllegalStateException("IE01200: Database node not found");
  }
}
