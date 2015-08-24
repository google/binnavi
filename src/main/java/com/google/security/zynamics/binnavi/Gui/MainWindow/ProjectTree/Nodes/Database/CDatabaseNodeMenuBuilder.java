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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Database;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.API.disassembly.Database;
import com.google.security.zynamics.binnavi.API.disassembly.DatabaseManager;
import com.google.security.zynamics.binnavi.API.plugins.IDatabaseMenuPlugin;
import com.google.security.zynamics.binnavi.API.plugins.PluginInterface;
import com.google.security.zynamics.binnavi.Database.CDatabaseListenerAdapter;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CCloseDatabaseAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CDeleteDatabaseAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CImportModuleAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.COpenDatabaseAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractMenuBuilder;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Updaters.CParentSelectionUpdater;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.api2.IPluginInterface;
import com.google.security.zynamics.binnavi.api2.plugins.IPlugin;

import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Menu builder class that builds the context menus used on database objects.
 */
public final class CDatabaseNodeMenuBuilder extends CAbstractMenuBuilder {
  /**
   * Parent node of the node for which the menu is created.
   */
  private final DefaultMutableTreeNode parentNode;

  /**
   * The database object the context menu is created for.
   */
  private final IDatabase database;

  /**
   * Action that is used to connect to closed databases.
   */
  private final Action openAction;

  /**
   * Action that is used to close connected databases.
   */
  private final Action closeAction;

  /**
   * Listener that updates the actions that depend on the state of the database.
   */
  private final InternalDatabaseListener listener = new InternalDatabaseListener();

  /**
   * Flag that says whether the user can currently establish a connection to the
   * database.
   */
  private boolean allowConnection = true;

  /**
   * Creates a new project menu builder object.
   *
   * @param projectTree Project tree of the main window.
   * @param parentNode Parent node of the node for which the menu is created.
   * @param database The database object the menu is created for.
   */
  public CDatabaseNodeMenuBuilder(final JTree projectTree, final DefaultMutableTreeNode parentNode,
      final IDatabase database) {
    super(projectTree);

    this.database =
        Preconditions.checkNotNull(database, "IE01963: Database argument can't be null");
    Preconditions.checkNotNull(projectTree, "IE02345: projectTree argument can not be null");
    this.parentNode =
        Preconditions.checkNotNull(parentNode, "IE02346: parentNode argument can not be null");

    this.openAction = CActionProxy.proxy(new COpenDatabaseAction(projectTree, this.database));
    this.closeAction = CActionProxy.proxy(new CCloseDatabaseAction(projectTree, this.database));

    updateActions(database);

    this.database.addListener(listener);
  }

  /**
   * Adds the plugin-generated menus to the context menu.
   *
   * @param menu The context menu where the menu items are added.
   */
  private void addPluginMenus(final JComponent menu) {
    final List<IDatabaseMenuPlugin> plugins = Lists.newArrayList();

    for (final IPlugin<IPluginInterface> plugin : PluginInterface.instance().getPluginRegistry()) {
      if (plugin instanceof IDatabaseMenuPlugin) {
        plugins.add((IDatabaseMenuPlugin) plugin);
      }
    }

    if (!plugins.isEmpty()) {
      menu.add(new JSeparator());

      for (final IDatabaseMenuPlugin plugin : plugins) {
        try {
          final List<JComponent> menuItems = plugin.extendDatabaseMenu(getPluginDatabase());

          if (menuItems != null) {
            for (final JComponent menuItem : menuItems) {
              menu.add(menuItem);
            }
          }
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);

          final String innerMessage = "E00091: " + "Plugin caused an unexpected exception";
          final String innerDescription = CUtilityFunctions.createDescription(
              String.format("The plugin %s caused an unexpected exception.", plugin.getName()),
              new String[] {"The plugin contains a bug."}, new String[] {
                  "The plugin probably behaves errorneously from this "
                  + "point on but it remains active"});

          NaviErrorDialog.show(getParent(), innerMessage, innerDescription, exception);
        }
      }
    }
  }

  /**
   * Returns the API database object that represents the database object for
   * which the menu is built.
   *
   * @return The API database object.
   */
  private Database getPluginDatabase() {
    final DatabaseManager manager = PluginInterface.instance().getDatabaseManager();

    for (final Database database : manager) {
      if (database.getNative() == this.database) {
        return database;
      }
    }

    throw new IllegalStateException("IE01162: Unknown database");
  }

  /**
   * Updates the actions that depend on the state of the database.
   *
   * @param database The database in question.
   */
  private void updateActions(final IDatabase database) {
    openAction.setEnabled(allowConnection && !database.isConnected()
        && !database.getConfiguration().getIdentity().isEmpty()
        && !database.getConfiguration().getUser().isEmpty()
        && !database.getConfiguration().getPassword().isEmpty());
    closeAction.setEnabled(database.isConnected());
  }

  @Override
  protected void createMenu(final JComponent menu) {
    menu.add(new JMenuItem(openAction));
    menu.add(new JMenuItem(closeAction));
    menu.add(new JSeparator());

    menu.add(new JMenuItem(CActionProxy.proxy(new CImportModuleAction(getParent(), database))));
    menu.add(new JSeparator());

    menu.add(new JMenuItem(CActionProxy.proxy(new CDeleteDatabaseAction(getParent(), database,
        new CParentSelectionUpdater(getProjectTree(), parentNode)))));

    addPluginMenus(menu);
  }

  @Override
  protected JMenu getMenu() {
    final JMenu menu = new JMenu("Database");

    menu.setMnemonic("HK_MENU_DATABASE".charAt(0));

    return menu;
  }

  /**
   * Sets the state that says whether connections to the database are currently
   * allowed.
   *
   * @param allowed True, if connections are allowed. False, if they are not.
   */
  public void allowConnection(final boolean allowed) {
    allowConnection = allowed;
    updateActions(database);
  }

  @Override
  public void dispose() {
    database.removeListener(listener);
  }

  /**
   * Listener that updates the actions that depend on the state of the database.
   */
  private class InternalDatabaseListener extends CDatabaseListenerAdapter {
    @Override
    public void closedDatabase(final IDatabase database) {
      updateActions(database);
    }

    @Override
    public void openedDatabase(final IDatabase database) {
      updateActions(database);
    }
  }
}
