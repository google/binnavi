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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Module;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.API.disassembly.Database;
import com.google.security.zynamics.binnavi.API.disassembly.DatabaseManager;
import com.google.security.zynamics.binnavi.API.disassembly.Module;
import com.google.security.zynamics.binnavi.API.plugins.IModuleMenuPlugin;
import com.google.security.zynamics.binnavi.API.plugins.PluginInterface;
import com.google.security.zynamics.binnavi.APIHelpers.ObjectFinders;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CDeleteModuleAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CLoadModuleAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CModuleInitializeAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CRemoveModuleAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CResolveAllFunctionsSingleModuleAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CResolveFunctionsAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CSearchAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CSearchTableAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CStarModulesAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CToggleStarsModulesAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CUnstarModulesAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractMenuBuilder;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Updaters.CEmptyUpdater;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Updaters.CParentSelectionUpdater;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Updaters.ITreeUpdater;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.api2.IPluginInterface;
import com.google.security.zynamics.binnavi.api2.plugins.IPlugin;
import com.google.security.zynamics.binnavi.disassembly.INaviAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModuleContainer;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModuleListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.algorithms.CStaredItemFunctions;
import com.google.security.zynamics.zylib.gui.tables.CopySelectionAction;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Menu builder for building the main menu and the context menu shown when module nodes are active.
 */
public final class CModuleNodeMenuBuilder extends CAbstractMenuBuilder {
  /**
   * Parent node of the node for which the menu is built.
   */
  private final DefaultMutableTreeNode m_parentNode;

  /**
   * Database the module belongs to.
   */
  private final IDatabase m_database;

  /**
   * Address spaces the module belongs to (can be null).
   */
  private final INaviAddressSpace m_addressSpace;

  /**
   * Modules for which the menu is created.
   */
  private final INaviModule[] m_modules;

  /**
   * Context container of the module if the menu is built for a single module.
   */
  private CModuleContainer m_container;

  /**
   * Table where the context menu is shown (can be null).
   */
  private final JTable m_table;

  /**
   * Action class for loading modules.
   */
  private final Action m_loadModulesAction;

  /**
   * Action class for resolving the imported functions of a module.
   */
  private final Action m_resolveAction;

  private final Action m_resolveAllAction;

  /**
   * Action class for searching for a view inside a module.
   */
  private final Action m_searchAction;

  /**
   * Updates menu actions on changes in the module.
   */
  private final InternalModuleListener m_listener = new InternalModuleListener();

  private final Action m_initializeModuleAction;

  /**
   * Creates a new menu builder object.
   *
   * @param projectTree Project tree of the main window.
   * @param parentNode Parent node of the node for which the menu is built.
   * @param database Database the module belongs to.
   * @param addressSpace Address spaces the module belongs to (this argument can be null).
   * @param modules Modules for which the menu is created.
   * @param table Table where the context menu is shown (this argument can be null).
   */
  public CModuleNodeMenuBuilder(final JTree projectTree,
      final DefaultMutableTreeNode parentNode,
      final IDatabase database,
      final INaviAddressSpace addressSpace,
      final INaviModule[] modules,
      final JTable table) {
    super(projectTree);

    m_database = Preconditions.checkNotNull(database, "IE01109: Database argument can't be null");

    m_parentNode = parentNode;
    m_addressSpace = addressSpace;
    m_modules = modules.clone();
    m_table = table;

    final boolean singleModule = m_modules.length == 1;

    m_loadModulesAction = CActionProxy.proxy(new CLoadModuleAction(projectTree, m_modules));
    m_initializeModuleAction =
        CActionProxy.proxy(new CModuleInitializeAction(projectTree, m_modules));

    if (singleModule) {
      m_container = new CModuleContainer(database, m_modules[0]);

      m_searchAction = CActionProxy.proxy(new CSearchAction(projectTree, m_container));
      m_resolveAction =
          CActionProxy.proxy(new CResolveFunctionsAction(projectTree, m_database, m_modules[0]));
      m_resolveAllAction = CActionProxy.proxy(
          new CResolveAllFunctionsSingleModuleAction(projectTree, m_database, m_modules[0]));

      updateActions(m_modules[0]);

      m_modules[0].addListener(m_listener);
    } else {
      m_searchAction = null;
      m_resolveAction = null;
      m_resolveAllAction = null;

      updateActions(m_modules);
    }
  }

  /**
   * Adds the plugin-generated menus to the context menu.
   *
   * @param menu The context menu where the menu items are added.
   */
  private void addPluginMenus(final JComponent menu) {
    final List<IModuleMenuPlugin> plugins = new ArrayList<>();
    for (final IPlugin<IPluginInterface> plugin : PluginInterface.instance().getPluginRegistry()) {
      if (plugin instanceof IModuleMenuPlugin) {
        plugins.add((IModuleMenuPlugin) plugin);
      }
    }

    boolean addedSeparator = false;
    for (final IModuleMenuPlugin plugin : plugins) {
      try {
        final List<JComponent> menuItems = plugin.extendModuleMenu(getPluginModules());
        if (menuItems != null) {
          for (final JComponent menuItem : menuItems) {
            if (!addedSeparator) {
              menu.add(new JSeparator());
              addedSeparator = true;
            }
            menu.add(menuItem);
          }
        }
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);

        final String innerMessage = "E00093: " + "Plugin caused an unexpected exception";
        final String innerDescription = CUtilityFunctions.createDescription(String.format(
            "The plugin %s caused an unexpected exception.", plugin.getName()),
            new String[] {"The plugin contains a bug."}, new String[] {
                "The plugin probably behaves erroneously from this point on but it remains active"});

        NaviErrorDialog.show(getParent(), innerMessage, innerDescription, exception);
      }
    }
  }

  /**
   * Creates the project tree updater depending on the context in which the menu is built.
   *
   * @return Created tree updater object.
   */
  private ITreeUpdater getParentUpdater() {
    return m_parentNode == null ? new CEmptyUpdater()
        : new CParentSelectionUpdater(getProjectTree(), m_parentNode);
  }

  /**
   * Returns the API module objects for the modules for which the menu was built.
   *
   * @return The API module objects.
   */
  private List<Module> getPluginModules() {
    final DatabaseManager manager = PluginInterface.instance().getDatabaseManager();

    for (final Database database : manager) {
      if (database.getNative() == m_database) {
        final List<Module> allModules = database.getModules();
        final List<Module> menuModules = new ArrayList<Module>();

        for (final INaviModule module : m_modules) {
          menuModules.add(ObjectFinders.getObject(module, allModules));
        }

        return menuModules;
      }
    }

    throw new IllegalStateException("IE01165: Unknown database");
  }

  /**
   * Updates the menu actions depending on the state of the given module.
   *
   * @param module The module whose state determines the action states.
   */
  private void updateActions(final INaviModule module) {

    m_loadModulesAction.putValue("Name", CLoadModuleAction.generateActionString(module));
    m_loadModulesAction.setEnabled(
        module.getConfiguration().getRawModule().isComplete() && !module.isLoaded());

    m_initializeModuleAction.setEnabled(
        !module.isInitialized() && module.getConfiguration().getRawModule().isComplete());

    m_resolveAction.setEnabled(module.isLoaded());
    m_resolveAllAction.setEnabled(module.isLoaded());
    m_searchAction.setEnabled(module.isLoaded());
  }

  private void updateActions(final INaviModule[] m_modules) {
    for (final INaviModule module : m_modules) {
      if (module.isInitialized()) {
        m_initializeModuleAction.setEnabled(false);
        return;
      }
    }
    m_initializeModuleAction.setEnabled(true);
  }

  @Override
  protected void createMenu(final JComponent menu) {
    menu.add(new JMenuItem(m_loadModulesAction));
    menu.add(new JMenuItem(m_initializeModuleAction));
    menu.add(new JSeparator());

    if (CStaredItemFunctions.allStared(m_modules)) {
      menu.add(new JMenuItem(CActionProxy.proxy(new CUnstarModulesAction(getParent(), m_modules))));
    } else if (CStaredItemFunctions.allNotStared(m_modules)) {
      menu.add(new JMenuItem(CActionProxy.proxy(new CStarModulesAction(getParent(), m_modules))));
    } else {
      menu.add(
          new JMenuItem(CActionProxy.proxy(new CToggleStarsModulesAction(getParent(), m_modules))));
    }

    if (m_modules.length == 1) {
      menu.add(new JSeparator());

      menu.add(new JMenuItem(m_searchAction));
      menu.add(new JMenuItem(m_resolveAction));
      menu.add(new JMenuItem(m_resolveAllAction));
    }

    menu.add(new JSeparator());

    if (m_addressSpace == null) {
      menu.add(new JMenuItem(CActionProxy.proxy(
          new CDeleteModuleAction(getParent(), m_database, m_modules, getParentUpdater()))));
    } else {
      menu.add(new JMenuItem(
          CActionProxy.proxy(new CRemoveModuleAction(getParent(), m_addressSpace, m_modules))));
    }

    if (m_table != null) {
      menu.add(new JSeparator());
      menu.add(new JMenuItem(CActionProxy.proxy(new CSearchTableAction(getParent(), m_table))));
      menu.add(new JMenuItem(CActionProxy.proxy(new CopySelectionAction(m_table))));
    }

    addPluginMenus(menu);
  }

  @Override
  protected JMenu getMenu() {
    final JMenu menu = new JMenu("Module");

    menu.setMnemonic("HK_MENU_MODULE".charAt(0));

    return menu;
  }

  @Override
  public void dispose() {
    if (m_modules.length == 1) {
      m_container.dispose();
      m_modules[0].removeListener(m_listener);
    }
  }

  /**
   * Updates menu actions on changes in the module.
   */
  private class InternalModuleListener extends CModuleListenerAdapter {
    @Override
    public void loadedModule(final INaviModule module) {
      updateActions(module);
    }

    @Override
    public void initializedModule(final INaviModule module) {
      updateActions(module);
    }
  }
}
