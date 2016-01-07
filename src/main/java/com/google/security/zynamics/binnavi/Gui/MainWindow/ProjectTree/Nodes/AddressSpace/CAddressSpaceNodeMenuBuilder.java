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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.AddressSpace;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.API.disassembly.AddressSpace;
import com.google.security.zynamics.binnavi.API.disassembly.Database;
import com.google.security.zynamics.binnavi.API.disassembly.DatabaseManager;
import com.google.security.zynamics.binnavi.API.disassembly.Project;
import com.google.security.zynamics.binnavi.API.plugins.IAddressSpaceMenuPlugin;
import com.google.security.zynamics.binnavi.API.plugins.PluginInterface;
import com.google.security.zynamics.binnavi.APIHelpers.ObjectFinders;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CCreateCombinedCallgraphAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CDeleteAddressSpaceAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CLoadAddressSpaceAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CResolveAllFunctionsSingleAddressSpaceAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CSearchTableAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractMenuBuilder;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Updaters.CEmptyUpdater;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Updaters.CParentSelectionUpdater;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Updaters.ITreeUpdater;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.api2.IPluginInterface;
import com.google.security.zynamics.binnavi.api2.plugins.IPlugin;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpaceListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.INaviAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
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
 * Menu builder that builds the frame menu and popup menu for address spaces.
 */
public final class CAddressSpaceNodeMenuBuilder extends CAbstractMenuBuilder {
  /**
   * Parent node of this node.
   */
  private final DefaultMutableTreeNode m_parentNode;

  /**
   * Table for which the menu is created (can be null).
   */
  private final JTable m_table;

  /**
   * Database where the address spaces are stored.
   */
  private final IDatabase m_database;

  /**
   * Project that contains the address space.
   */
  private final INaviProject m_project;

  /**
   * Address space the menu belongs to.
   */
  private final INaviAddressSpace[] m_addressSpaces;

  /**
   * Context in which views of the address space are opened.
   */
  private final IViewContainer m_container;

  /**
   * Action that is used to load address spaces.
   */
  private final Action m_loadAddressSpaceAction;

  /**
   * Updates the menu on changes in the address space.
   */
  private final InternalAddressSpaceListener m_listener = new InternalAddressSpaceListener();

  /**
   * Creates a new menu builder object for address space nodes.
   *
   * @param parentNode Parent node of this node.
   * @param projectTree Project tree to update on certain events.
   * @param table Table for which the menu is created (can be null).
   * @param database Database where the address spaces are stored.
   * @param project The project that contains the address spaces.
   * @param addressSpaces Address spaces the menu belongs to.
   * @param container Context in which views of the address space are opened.
   */
  public CAddressSpaceNodeMenuBuilder(final JTree projectTree,
      final DefaultMutableTreeNode parentNode,
      final JTable table,
      final IDatabase database,
      final INaviProject project,
      final INaviAddressSpace[] addressSpaces,
      final IViewContainer container) {
    super(projectTree);

    Preconditions.checkNotNull(projectTree, "IE01943: Project tree argument can not be null");
    m_database = Preconditions.checkNotNull(database, "IE01944: Database argument can not be null");
    m_project = Preconditions.checkNotNull(project, "IE01945: Project argument can't be null");
    Preconditions.checkNotNull(addressSpaces, "IE01946: Address space argument can't be null");

    for (final INaviAddressSpace addressSpace : addressSpaces) {
      Preconditions.checkNotNull(addressSpace,
          "IE01947: Address spaces list contains a null-element");
    }

    m_parentNode = parentNode;
    // TODO Find out why there is a clone operation here and fix it.
    m_addressSpaces = addressSpaces.clone();
    m_table = table;
    m_container = container;

    m_loadAddressSpaceAction =
        CActionProxy.proxy(new CLoadAddressSpaceAction(projectTree, m_addressSpaces));

    if (addressSpaces.length == 1) {
      updateActions(m_addressSpaces[0]);

      m_addressSpaces[0].addListener(m_listener);
    }
  }

  /**
   * Tells a plugin to create an extension to this menu.
   *
   * @param menu The menu the extensions are added to.
   * @param plugin The plugin to extend the menu.
   */
  private void addPluginMenu(final JComponent menu, final IAddressSpaceMenuPlugin plugin) {
    // ESCA-JAVA0166: Catching Exception because we are calling a plugin method
    try {
      final List<JComponent> menuItems = plugin.extendAddressSpaceMenu(getPluginAddressSpaces());

      if (menuItems != null) {
        for (final JComponent menuItem : menuItems) {
          if (menuItem != null) {
            menu.add(menuItem);
          }
        }
      }
    } catch (final Exception exception) {
      CUtilityFunctions.logException(exception);

      final String innerMessage = "E00094: " + "Plugin caused an unexpected exception";
      final String innerDescription = CUtilityFunctions.createDescription(
          String.format("The plugin %s caused an unexpected exception.", plugin.getName()),
          new String[] {"The plugin contains a bug."}, new String[] {
              "The plugin probably behaves erroneously from this point on but it remains active"});

      NaviErrorDialog.show(getParent(), innerMessage, innerDescription, exception);
    }
  }

  /**
   * Iterates over all known plugins and tells them to create their extensions to this menu.
   *
   * @param menu The menu the extensions are added to.
   */
  private void addPluginMenus(final JComponent menu) {
    final List<IAddressSpaceMenuPlugin> plugins = Lists.newArrayList();
    for (final IPlugin<IPluginInterface> plugin : PluginInterface.instance().getPluginRegistry()) {
      if (plugin instanceof IAddressSpaceMenuPlugin) {
        plugins.add((IAddressSpaceMenuPlugin) plugin);
      }
    }

    if (!plugins.isEmpty()) {
      menu.add(new JSeparator());
      for (final IAddressSpaceMenuPlugin plugin : plugins) {
        addPluginMenu(menu, plugin);
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
   * Returns the API address space objects for the address spaces for which the menu was built.
   *
   * @return The API address space objects.
   */
  private List<AddressSpace> getPluginAddressSpaces() {
    final DatabaseManager manager = PluginInterface.instance().getDatabaseManager();

    for (final Database database : manager) {
      if (database.getNative() == m_database) {
        for (final Project project : database.getProjects()) {
          if (project.getNative() == m_project) {
            final List<AddressSpace> allSpaces = project.getAddressSpaces();
            final List<AddressSpace> menuSpaces = new ArrayList<AddressSpace>();

            for (final INaviAddressSpace addressSpace : m_addressSpaces) {
              menuSpaces.add(ObjectFinders.getObject(addressSpace, allSpaces));
            }

            return menuSpaces;
          }
        }
      }
    }

    throw new IllegalStateException("IE01159: Unknown database");
  }

  /**
   * Updates the menu actions depending on the state of the given address space.
   *
   * @param addressSpace The address space whose state determines the action states.
   */
  private void updateActions(final INaviAddressSpace addressSpace) {
    m_loadAddressSpaceAction.setEnabled(!addressSpace.isLoaded());
  }

  @Override
  protected void createMenu(final JComponent menu) {
    menu.add(new JMenuItem(m_loadAddressSpaceAction));

    menu.add(new JSeparator());

    menu.add(new JMenuItem(CActionProxy.proxy(new CDeleteAddressSpaceAction(getParent(), m_project,
        m_addressSpaces, getParentUpdater()))));

    if (m_addressSpaces.length == 1) {
      menu.add(new JSeparator());
      menu.add(new JMenuItem(CActionProxy.proxy(new CCreateCombinedCallgraphAction(getParent(),
          m_container, m_project, m_addressSpaces[0]))));

      menu.add(new JSeparator());
      menu.add(new JMenuItem(CActionProxy.proxy(
          new CResolveAllFunctionsSingleAddressSpaceAction(menu, m_database, m_addressSpaces[0]))));
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
    final JMenu menu = new JMenu("Address Space");

    menu.setMnemonic("HK_MENU_ADDRESS_SPACE".charAt(0));

    return menu;
  }

  @Override
  public void dispose() {
    if (m_addressSpaces.length == 1) {
      m_addressSpaces[0].removeListener(m_listener);
    }
  }

  /**
   * Updates the menu on changes in the address space.
   */
  private class InternalAddressSpaceListener extends CAddressSpaceListenerAdapter {
    @Override
    public void loaded(final INaviAddressSpace addressSpace) {
      updateActions(addressSpace);
    }
  }
}
