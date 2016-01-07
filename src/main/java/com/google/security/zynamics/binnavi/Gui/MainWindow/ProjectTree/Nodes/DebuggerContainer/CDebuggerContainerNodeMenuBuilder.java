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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.DebuggerContainer;



import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JTree;

import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CCreateDebuggerDescriptionAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractMenuBuilder;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CProjectTreeNode;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CProjectTreeNodeHelpers;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Updaters.CNodeSelectionUpdater;


/**
 * Menu builder for debugger container project nodes.
 */
public final class CDebuggerContainerNodeMenuBuilder extends CAbstractMenuBuilder {
  /**
   * Database the debuggers belong to.
   */
  private final IDatabase m_database;

  /**
   * Creates a new menu builder object.
   * 
   * @param projectTree Project tree of the main window.
   * @param database Database the debuggers belong to.
   */
  public CDebuggerContainerNodeMenuBuilder(final JTree projectTree, final IDatabase database) {
    super(projectTree);

    m_database = database;
  }

  /**
   * Given a database node of the project tree, this function finds the child node that is a
   * debugger container node.
   * 
   * @param databaseNode The database node where the search starts.
   * 
   * @return The debugger container node below the database node.
   */
  private CProjectTreeNode<?> findDebuggerContainerNode(final CProjectTreeNode<?> databaseNode) {
    final List<CProjectTreeNode<?>> nodes = new ArrayList<CProjectTreeNode<?>>();

    nodes.add(databaseNode);

    while (!nodes.isEmpty()) {
      final CProjectTreeNode<?> current = nodes.get(0);
      nodes.remove(0);

      if (current instanceof CDebuggerContainerNode) {
        return current;
      }

      for (final Enumeration<?> e = current.children(); e.hasMoreElements();) {
        nodes.add((CProjectTreeNode<?>) e.nextElement());
      }
    }

    throw new IllegalStateException("IE01201: Debugger container node not found");
  }

  /**
   * Finds the node for which the menu was built in the project tree.
   * 
   * @return The clicked node.
   */
  private CProjectTreeNode<?> findNode() {
    return findDebuggerContainerNode(CProjectTreeNodeHelpers.findDatabaseNode(getProjectTree(),
        m_database));
  }

  @Override
  protected void createMenu(final JComponent menu) {
    menu.add(new JMenuItem(CActionProxy.proxy(new CCreateDebuggerDescriptionAction(getParent(),
        m_database, "New Debugger", "localhost", 2222, new CNodeSelectionUpdater(getProjectTree(),
            findNode())))));
  }

  @Override
  protected JMenu getMenu() {
    final JMenu menu = new JMenu("Debuggers");

    menu.setMnemonic("HK_MENU_DEBUGGERS".charAt(0));

    return menu;
  }
}
