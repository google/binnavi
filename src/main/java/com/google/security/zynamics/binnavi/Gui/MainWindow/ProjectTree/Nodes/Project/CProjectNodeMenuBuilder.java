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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Project;



import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.API.disassembly.Database;
import com.google.security.zynamics.binnavi.API.disassembly.DatabaseManager;
import com.google.security.zynamics.binnavi.API.disassembly.Project;
import com.google.security.zynamics.binnavi.API.plugins.IProjectMenuPlugin;
import com.google.security.zynamics.binnavi.API.plugins.PluginInterface;
import com.google.security.zynamics.binnavi.APIHelpers.ObjectFinders;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CAddAddressSpaceAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CDeleteProjectAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CLoadProjectAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CResolveAllFunctionsSingleProjectAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CSearchAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CSearchTableAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractMenuBuilder;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CProjectTreeNode;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CProjectTreeNodeHelpers;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.ProjectContainer.CProjectContainerNode;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Updaters.CEmptyNodeSelectionUpdater;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Updaters.CEmptyUpdater;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Updaters.CNodeSelectionUpdater;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Updaters.CParentSelectionUpdater;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Updaters.INodeSelectionUpdater;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Updaters.ITreeUpdater;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.api2.plugins.IPlugin;
import com.google.security.zynamics.binnavi.disassembly.CProject;
import com.google.security.zynamics.binnavi.disassembly.CProjectContainer;
import com.google.security.zynamics.binnavi.disassembly.CProjectListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.zylib.gui.tables.CopySelectionAction;

/**
 * Menu builder class that is used to build the context menus of project nodes and project row
 * entries in tables.
 */
public final class CProjectNodeMenuBuilder extends CAbstractMenuBuilder {
  /**
   * Parent node of the node for which the menu was built.
   */
  private final DefaultMutableTreeNode m_parentNode;

  /**
   * Database that contains the project.
   */
  private final IDatabase m_database;

  /**
   * Selected projects
   */
  private final INaviProject[] m_projects;

  /**
   * Optional table attribute where the context menu is shown.
   */
  private final JTable m_table;

  /**
   * Action that is used to add an address space to the project.
   */
  private Action m_addAddressSpaceAction;

  /**
   * Action that is used to search for views in a project.
   */
  private final Action m_searchViewAction;

  /**
   * Action that is used to forward functions in projects;
   */
  private final Action m_forwardAction;

  /**
   * Listener that updates the actions that depend on the state of the project.
   */
  private final InternalProjectListener m_listener = new InternalProjectListener();

  /**
   * Action class used for loading projects.
   */
  private final Action m_loadProjectAction;

  /**
   * Provides a context container if the menu is built for a single project.
   */
  private IViewContainer m_container;

  /**
   * Creates a new project node menu builder object.
   * 
   * @param projectTree Project tree of the main window.
   * @param parentNode Parent node of the node for which the menu was built.
   * @param database The database that contains the project.
   * @param projects The project that was clicked.
   * @param table Optional table argument where the popup menu is shown. If this argument is null,
   *        the menu is shown in another component.
   */
  public CProjectNodeMenuBuilder(final JTree projectTree, final DefaultMutableTreeNode parentNode,
      final IDatabase database, final INaviProject[] projects, final JTable table) {
    super(projectTree);

    m_database = Preconditions.checkNotNull(database, "IE01982: Database argument can't be null");
    Preconditions.checkNotNull(projects, "IE01983: Project argument can't be null");

    m_parentNode = parentNode;
    m_projects = projects.clone();
    m_table = table;

    m_loadProjectAction = CActionProxy.proxy(new CLoadProjectAction(projectTree, projects));

    if (projects.length == 1) {
      // If there is just a single project selected, we add the menus
      // that invoke actions that only work on single projects.

      final INaviProject singleProject = projects[0];

      if (m_table == null) {
        // We do not need to add a listener to the project if the
        // created menu is just a short-lived context menu in a
        // table.

        singleProject.addListener(m_listener);
      }

      m_container = new CProjectContainer(database, singleProject);

      m_searchViewAction = CActionProxy.proxy(new CSearchAction(projectTree, m_container));

      m_forwardAction =
          CActionProxy.proxy(new CResolveAllFunctionsSingleProjectAction(projectTree, m_database,
              m_projects[0]));

      updateActions(singleProject);
    } else {
      m_addAddressSpaceAction = null;
      m_searchViewAction = null;
      m_forwardAction = null;
    }
  }

  /**
   * Adds the plugin-generated menus to the context menu.
   * 
   * @param menu The context menu where the menu items are added.
   */
  private void addPluginMenus(final JComponent menu) {
    final List<IProjectMenuPlugin> plugins = new ArrayList<IProjectMenuPlugin>();

    for (@SuppressWarnings("rawtypes")
    final IPlugin plugin : PluginInterface.instance().getPluginRegistry()) {
      if (plugin instanceof IProjectMenuPlugin) {
        plugins.add((IProjectMenuPlugin) plugin);
      }
    }

    if (!plugins.isEmpty()) {
      boolean addedSeparator = false;

      for (final IProjectMenuPlugin plugin : plugins) {
        try {
          final List<JComponent> menuItems = plugin.extendProjectMenu(getPluginProjects());

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

          final String innerMessage = "E00088: " + "Plugin caused an unexpected exception";
          final String innerDescription =
              CUtilityFunctions
                  .createDescription(
                      String.format("The plugin %s caused an unexpected exception.",
                          plugin.getName()),
                      new String[] {"The plugin contains a bug."},
                      new String[] {"The plugin probably behaves erroneously from this point on but it remains active"});

          NaviErrorDialog.show(getParent(), innerMessage, innerDescription, exception);
        }
      }
    }
  }

  /**
   * Determines whether all nodes for which the menu is built are unloaded.
   * 
   * @return True, if all projects are unloaded. False, if at least one project is loaded.
   */
  private boolean canOpen() {
    for (final INaviProject project : m_projects) {
      if (project.isLoaded()) {
        return false;
      }
    }

    return true;
  }

  /**
   * Finds the node for which the menu was built in the project tree.
   * 
   * @return The clicked node.
   */
  private CProjectTreeNode<?> findNode() {
    return findProjectNode(CProjectTreeNodeHelpers.findDatabaseNode(getProjectTree(), m_database),
        m_projects[0]);
  }

  /**
   * Finds the project tree node that represents a given project.
   * 
   * @param databaseNode Database node where the search operation begins.
   * @param project Project to search for.
   * 
   * @return The node that represents the given database.
   */
  private CProjectTreeNode<?> findProjectNode(final CProjectTreeNode<?> databaseNode,
      final INaviProject project) {
    final List<CProjectTreeNode<?>> nodes = new ArrayList<CProjectTreeNode<?>>();

    nodes.add(databaseNode);

    while (!nodes.isEmpty()) {
      final CProjectTreeNode<?> current = nodes.get(0);
      nodes.remove(0);

      if ((current instanceof CProjectNode) && (((CProjectNode) current).getObject() == project)) {
        return current;
      }

      for (final Enumeration<?> e = current.children(); e.hasMoreElements();) {
        final CProjectTreeNode<?> element = (CProjectTreeNode<?>) e.nextElement();

        if ((element instanceof CProjectContainerNode) || (element instanceof CProjectNode)) {
          nodes.add(element);
        }
      }
    }

    throw new IllegalStateException("IE01202: Project node not found");
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

  /**
   * Returns the API project objects for the projects for which the menu was built.
   * 
   * @return The API project objects.
   */
  private List<Project> getPluginProjects() {
    final DatabaseManager manager = PluginInterface.instance().getDatabaseManager();

    for (final Database database : manager) {
      if (database.getNative() == m_database) {
        final List<Project> allProjects = database.getProjects();
        final List<Project> menuProjects = new ArrayList<Project>();

        for (final INaviProject project : m_projects) {
          menuProjects.add(ObjectFinders.getObject(project, allProjects));
        }

        return menuProjects;
      }
    }

    throw new IllegalStateException("IE01168: Unknown database");
  }

  /**
   * Creates the project tree updater depending on the context in which the menu is built.
   * 
   * @return Created tree updater object.
   */
  private INodeSelectionUpdater getSelectionUpdater() {
    return m_table == null ? new CNodeSelectionUpdater(getProjectTree(), findNode())
        : new CEmptyNodeSelectionUpdater();
  }

  /**
   * Updates the actions that depend on the state of a single project.
   * 
   * @param project The project in question.
   */
  private void updateActions(final INaviProject project) {
    m_loadProjectAction.setEnabled(!project.isLoaded());

    if (m_addAddressSpaceAction != null) {
      m_addAddressSpaceAction.setEnabled(project.isLoaded());
    }

    m_searchViewAction.setEnabled(project.isLoaded());

    m_forwardAction.setEnabled(project.isLoaded());
  }

  @Override
  protected void createMenu(final JComponent menu) {
    if ((m_projects.length == 1) && (m_addAddressSpaceAction == null)) {
      // We delay the creation of the m_addAddressSpaceAction object because we need to have
      // getSelectionUpdater working.

      final INaviProject singleProject = m_projects[0];

      m_addAddressSpaceAction =
          CActionProxy.proxy(new CAddAddressSpaceAction(getProjectTree(), singleProject,
              getSelectionUpdater()));

      updateActions(singleProject);
    }

    final boolean isSingleSelection = m_projects.length == 1;

    if (canOpen()) {
      menu.add(new JMenuItem(m_loadProjectAction));
      menu.add(new JSeparator());
    }

    if (isSingleSelection) {
      // We can not search in more than one project at a time, and only if the project
      // is already loaded.

      menu.add(new JMenuItem(m_searchViewAction));
      menu.add(new JSeparator());

      // We do not allow the user to add address spaces to more than one
      // project at a time. And the project must be loaded too.

      menu.add(new JMenuItem(m_addAddressSpaceAction));
      menu.add(new JSeparator());

      menu.add(new JMenuItem(m_forwardAction));
      menu.add(new JSeparator());
    }

    menu.add(new JMenuItem(CActionProxy.proxy(new CDeleteProjectAction(getParent(), m_database,
        m_projects, getParentUpdater()))));

    if (m_table != null) {
      menu.add(new JSeparator());
      menu.add(new JMenuItem(CActionProxy.proxy(new CSearchTableAction(getParent(), m_table))));
      menu.add(new JMenuItem(CActionProxy.proxy(new CopySelectionAction(m_table))));
    }

    addPluginMenus(menu);
  }

  @Override
  protected JMenu getMenu() {
    final JMenu menu = new JMenu("Project");

    menu.setMnemonic("HK_MENU_PROJECT".charAt(0));

    return menu;
  }

  @Override
  public void dispose() {
    if (m_projects.length == 1) {
      m_projects[0].removeListener(m_listener);
      m_container.dispose();
    }
  }

  /**
   * Keeps the project-state dependent menus up to date.
   */
  private class InternalProjectListener extends CProjectListenerAdapter {
    @Override
    public void loadedProject(final CProject project) {
      updateActions(project);
    }
  }
}
