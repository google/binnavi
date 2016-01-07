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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.ModuleContainer;



import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CNodeExpander;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CActionSortModulesById;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CActionSortModulesByName;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CImportModuleAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CRefreshRawModulesAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CResolveAllFunctionsAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractMenuBuilder;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CProjectTreeNode;

/**
 * Menu builder for building the main menu and the context menu shown when module container nodes
 * are active.
 */
public final class CModuleContainerNodeMenuBuilder extends CAbstractMenuBuilder {
  /**
   * Database the module belongs to.
   */
  private final IDatabase m_database;

  /**
   * Container node for which the menu is built.
   */
  private CModuleContainerNode m_containerNode = null;

  /**
   * Creates a new menu builder object.
   * 
   * @param projectTree Project tree of the main window.
   * @param database Database the module belongs to.
   */
  public CModuleContainerNodeMenuBuilder(final JTree projectTree, final IDatabase database) {
    super(projectTree);

    Preconditions.checkNotNull(database, "IE01975: Database argument can't be null");

    m_database = database;
  }

  /**
   * Finds the module container node for a given database.
   * 
   * @param node The project node that represents the database.
   * 
   * @return The module container node for that database.
   */
  private CModuleContainerNode getModuleContainerNode(final CProjectTreeNode<?> node) {
    for (int i = 0; i < node.getChildCount(); i++) {
      final TreeNode child = node.getChildAt(i);

      if (child instanceof CModuleContainerNode) {
        return (CModuleContainerNode) child;
      }
    }

    throw new IllegalStateException("IE00693: Could not find module container node");
  }

  @Override
  protected void createMenu(final JComponent menu) {
    if (m_containerNode == null) {
      m_containerNode =
          getModuleContainerNode(CNodeExpander.findNode(getProjectTree(), m_database));
    }

    menu.add(new JMenuItem(CActionProxy.proxy(new CImportModuleAction(getParent(), m_database))));
    menu.add(new JMenuItem(CActionProxy
        .proxy(new CRefreshRawModulesAction(getParent(), m_database))));
    menu.add(new JMenuItem(CActionProxy.proxy(new CResolveAllFunctionsAction(menu, m_database))));
    menu.add(new JSeparator());

    final JMenu sortMenu = new JMenu("Sort");

    final JRadioButtonMenuItem idMenu =
        new JRadioButtonMenuItem(new CActionSortModulesById(m_containerNode));
    idMenu.setSelected(!m_containerNode.isSorted());
    sortMenu.add(idMenu);

    final JRadioButtonMenuItem nameMenu =
        new JRadioButtonMenuItem(new CActionSortModulesByName(m_containerNode));
    nameMenu.setSelected(m_containerNode.isSorted());
    sortMenu.add(nameMenu);

    menu.add(sortMenu);
  }

  @Override
  protected JMenu getMenu() {
    final JMenu menu = new JMenu("Module");

    menu.setMnemonic("HK_MENU_MODULE".charAt(0));

    return menu;
  }
}
