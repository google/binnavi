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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Debugger;



import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CDeleteDebuggerDescriptionAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractMenuBuilder;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.DebuggerContainer.Component.CDebuggersTable;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Updaters.CEmptyUpdater;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Updaters.CParentSelectionUpdater;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Updaters.ITreeUpdater;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.zylib.gui.tables.CopySelectionAction;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;



/**
 * Builds main menu and context menu of project tree debugger nodes.
 */
public final class CDebuggerNodeMenuBuilder extends CAbstractMenuBuilder {
  /**
   * Parent node of this node.
   */
  private final DefaultMutableTreeNode m_parentNode;

  /**
   * Table that was clicked (null if a tree node was clicked).
   */
  private final CDebuggersTable m_table;

  /**
   * Database the debugger belongs to.
   */
  private final IDatabase m_database;

  /**
   * Debuggers for which the menu is built.
   */
  private final DebuggerTemplate[] m_debuggers;

  /**
   * Creates a new menu builder object.
   * 
   * @param projectTree Project tree of the main window.
   * @param parentNode Parent node of this node.
   * @param table Table that was clicked (this argument is null if a tree node was clicked).
   * @param database Database the debugger belongs to.
   * @param debuggers Debuggers for which the menu is built.
   */
  public CDebuggerNodeMenuBuilder(final JTree projectTree, final DefaultMutableTreeNode parentNode,
      final CDebuggersTable table, final IDatabase database, final DebuggerTemplate debuggers[]) {
    super(projectTree);

    m_parentNode = parentNode;
    m_database = database;
    m_debuggers = debuggers.clone();
    m_table = table;
  }

  /**
   * Creates the project tree updater depending on the context in which the menu is built.
   * 
   * @return Created tree updater object.
   */
  private ITreeUpdater getParentUpdater() {
    return m_parentNode == null ? new CEmptyUpdater() : new CParentSelectionUpdater(
        getProjectTree(), m_parentNode);
  }

  @Override
  protected void createMenu(final JComponent menu) {
    menu.add(new JMenuItem(CActionProxy.proxy(new CDeleteDebuggerDescriptionAction(getParent(),
        m_database, m_debuggers, getParentUpdater()))));

    if (m_table != null) {
      menu.add(new JSeparator());
      menu.add(new JMenuItem(CActionProxy.proxy(new CopySelectionAction(m_table))));
    }
  }

  @Override
  protected JMenu getMenu() {
    final JMenu menu = new JMenu("Debugger");

    menu.setMnemonic("HK_MENU_DEBUGGER".charAt(0));

    return menu;
  }
}
