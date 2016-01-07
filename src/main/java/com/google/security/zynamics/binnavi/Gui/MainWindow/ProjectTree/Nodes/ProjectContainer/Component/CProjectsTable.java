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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.ProjectContainer.Component;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.HotKeys;
import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CDatabaseFunctions;
import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CProjectFunctions;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractTreeTable;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CLoadedRenderer;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Project.CProjectNodeMenuBuilder;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.ProjectContainer.Component.Help.CProjectsTableHelp;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Updaters.CEmptyUpdater;
import com.google.security.zynamics.binnavi.disassembly.CProject;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JTree;

/**
 * Table class that is used to display all projects in a database.
 */
public final class CProjectsTable extends CAbstractTreeTable<INaviProject> {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 2268882856382188306L;

  /**
   * The database that contains the projects.
   */
  private final IDatabase m_database;

  /**
   * Creates a new table object.
   * 
   * @param projectTree The project tree of the main window.
   * @param database The database to search for.
   */
  public CProjectsTable(final JTree projectTree, final IDatabase database) {
    super(projectTree, new CProjectsModel(database), new CProjectsTableHelp());
    m_database = Preconditions.checkNotNull(database, "IE02874: database argument can not be null");
    setDefaultRenderer(Object.class, new ProjectLoadedRenderer());
    final InputMap windowImap = getInputMap(JComponent.WHEN_FOCUSED);
    windowImap.put(HotKeys.LOAD_HK.getKeyStroke(), "LOAD");
    getActionMap().put("LOAD", CActionProxy.proxy(new LoadProjectAction()));
  }

  /**
   * From a list of projects, the projects that are not loaded are returned.
   * 
   * @param iNaviProjects The projects to search through.
   * 
   * @return The unloaded projects.
   */
  private static CProject[] getUnloadedProjects(final INaviProject[] iNaviProjects) {
    final List<INaviProject> unloadedProjects = new ArrayList<INaviProject>();

    for (final INaviProject project : iNaviProjects) {
      if (!project.isLoaded()) {
        unloadedProjects.add(project);
      }
    }

    return unloadedProjects.toArray(new CProject[] {});
  }

  /**
   * Returns the projects that correspond to the selected table rows.
   * 
   * @param sortSelectedRows The selected rows.
   * 
   * @return The projects that correspond to the selected rows.
   */
  private INaviProject[] getProjects(final int[] sortSelectedRows) {
    final INaviProject[] projects = new CProject[sortSelectedRows.length];

    for (int i = 0; i < projects.length; i++) {
      projects[i] = getTreeTableModel().getProjects().get(sortSelectedRows[i]);
    }

    return projects;
  }

  /**
   * Returns the project that correspond to the selected rows of the table. If the given row is not
   * selected, the given row is selected.
   * 
   * @param sortedRow Row to select if necessary.
   * 
   * @return The projects that correspond to the selected rows.
   */
  private INaviProject[] getSelectedProjects(final int sortedRow) {
    final int[] sortSelectedRows = getSortSelectedRows();
    if (Ints.asList(sortSelectedRows).indexOf(sortedRow) != -1) {
      return getProjects(sortSelectedRows);
    } else {
      final int viewRow = convertRowIndexToView(sortedRow);

      setRowSelectionInterval(viewRow, viewRow);

      return new INaviProject[] {getTreeTableModel().getProjects().get(sortedRow)};
    }
  }

  @Override
  protected void deleteRows() {
    CDatabaseFunctions.deleteProjects(getParentWindow(), m_database,
        getProjects(getSortSelectedRows()), new CEmptyUpdater());
  }

  @Override
  protected JPopupMenu getPopupMenu(final int x, final int y, final int row) {
    final CProjectNodeMenuBuilder menu =
        new CProjectNodeMenuBuilder(getProjectTree(), null, m_database, getSelectedProjects(row),
            this);

    return menu.getPopupMenu();
  }

  @Override
  protected void handleDoubleClick(final int row) {
    // Do nothing when a project is double-clicked.
  }

  @Override
  public CProjectsModel getTreeTableModel() {
    return (CProjectsModel) super.getTreeTableModel();
  }

  /**
   * Action class for loading projects.
   */
  private class LoadProjectAction extends AbstractAction {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = 5356344926300001300L;

    @Override
    public void actionPerformed(final ActionEvent event) {
      CProjectFunctions.openProjects(getProjectTree(),
          getUnloadedProjects(getProjects(getSortSelectedRows())));
    }
  }

  /**
   * Renderer that makes sure that loaded projects are rendered differently from unloaded projects.
   */
  private class ProjectLoadedRenderer extends CLoadedRenderer {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = -4896137097642356066L;

    @Override
    public boolean isLoaded(final int row) {
      return getProjects(new int[] {convertRowIndexToModel(row)})[0].isLoaded();
    }
  }
}
