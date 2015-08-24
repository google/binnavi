/*
Copyright 2015 Google Inc. All Rights Reserved.

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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.ProjectContainer;

import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.API.disassembly.Database;
import com.google.security.zynamics.binnavi.API.plugins.IProjectFolderMenuPlugin;
import com.google.security.zynamics.binnavi.API.plugins.PluginInterface;
import com.google.security.zynamics.binnavi.APIHelpers.ObjectFinders;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CCreateProjectAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractMenuBuilder;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CProjectTreeNode;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CProjectTreeNodeHelpers;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Updaters.CNodeSelectionUpdater;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.api2.IPluginInterface;
import com.google.security.zynamics.binnavi.api2.plugins.IPlugin;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.JTree;

/**
 * Builds the main menu when project container nodes are selected or the context
 * menu when project container nodes are right-clicked.
 */
public final class CProjectContainerNodeMenuBuilder extends CAbstractMenuBuilder {
  /**
   * Database the projects belong to.
   */
  private final IDatabase database;

  /**
   * Creates a new menu builder object.
   *
   * @param projectTree Project tree of the main window.
   * @param database Database the projects belong to.
   */
  public CProjectContainerNodeMenuBuilder(final JTree projectTree, final IDatabase database) {
    super(projectTree);

    this.database = database;
  }

  /**
   * Finds the node for which the menu was built in the project tree.
   *
   * @return The clicked node.
   */
  private CProjectTreeNode<?> findNode() {
    return findProjectContainerNode(
        CProjectTreeNodeHelpers.findDatabaseNode(getProjectTree(), database));
  }

  /**
   * Given a database node of the project tree, this function finds the child
   * node that is a project container node.
   *
   * @param databaseNode The database node where the search starts.
   *
   * @return The project container node below the database node.
   */
  private CProjectTreeNode<?> findProjectContainerNode(final CProjectTreeNode<?> databaseNode) {
    final List<CProjectTreeNode<?>> nodes = Lists.newArrayList();

    nodes.add(databaseNode);

    while (!nodes.isEmpty()) {
      final CProjectTreeNode<?> current = nodes.get(0);
      nodes.remove(0);

      if (current instanceof CProjectContainerNode) {
        return current;
      }

      for (final Enumeration<?> e = current.children(); e.hasMoreElements();) {
        nodes.add((CProjectTreeNode<?>) e.nextElement());
      }
    }

    throw new IllegalStateException("IE01203: Project container node not found");
  }

  /**
   * Finds the API database object that represents a given database object.
   *
   * @return The API database object.
   */
  private Database getPluginDatabase() {
    return ObjectFinders.getObject(database,
        PluginInterface.instance().getDatabaseManager().getDatabases());
  }

  @Override
  protected void createMenu(final JComponent menu) {
    menu.add(new JMenuItem(CActionProxy.proxy(new CCreateProjectAction(getParent(), database,
        new CNodeSelectionUpdater(getProjectTree(), findNode())))));

    final List<IProjectFolderMenuPlugin> plugins = new ArrayList<IProjectFolderMenuPlugin>();

    for (final IPlugin<IPluginInterface> plugin : PluginInterface.instance().getPluginRegistry()) {
      if (plugin instanceof IProjectFolderMenuPlugin) {
        plugins.add((IProjectFolderMenuPlugin) plugin);
      }
    }

    if (!plugins.isEmpty()) {
      menu.add(new JSeparator());

      for (final IProjectFolderMenuPlugin plugin : plugins) {
        try {
          final List<JComponent> menuItems = plugin.extendProjectFolderMenu(getPluginDatabase());

          if (menuItems != null) {
            for (final JComponent menuItem : menuItems) {
              menu.add(menuItem);
            }
          }
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);

          final String innerMessage = "E00089: " + "Plugin caused an unexpected exception";
          final String innerDescription = CUtilityFunctions.createDescription(String.format(
              "The plugin %s caused an unexpected exception.", plugin.getName()),
              new String[] {"The plugin contains a bug."}, new String[] {
                  "The plugin probably behaves erroneously from this point on but it remains active"});

          NaviErrorDialog.show(getParent(), innerMessage, innerDescription, exception);
        }
      }
    }
  }

  @Override
  protected JMenu getMenu() {
    final JMenu menu = new JMenu("Projects");
    menu.setMnemonic("HK_MENU_PROJECTS".charAt(0));

    return menu;
  }
}
