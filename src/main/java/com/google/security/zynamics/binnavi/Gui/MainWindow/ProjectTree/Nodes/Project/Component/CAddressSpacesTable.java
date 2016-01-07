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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Project.Component;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.HotKeys;
import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CAddressSpaceFunctions;
import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CProjectFunctions;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.AddressSpace.CAddressSpaceNodeMenuBuilder;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractTreeTable;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CLoadedRenderer;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Project.Component.Help.CAddressSpacesTableHelp;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Updaters.CEmptyUpdater;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.INaviAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JTree;

/**
 * The address spaces table displays information about all address spaces of a project.
 */
public final class CAddressSpacesTable extends CAbstractTreeTable<INaviAddressSpace> {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -2366271508529117739L;

  /**
   * Database the address space belongs to.
   */
  private final IDatabase m_database;

  /**
   * The project that contains the address spaces.
   */
  private final INaviProject m_project;

  /**
   * The view container of the project.
   */
  private final IViewContainer m_container;

  /**
   * Creates a new address spaces table.
   * 
   * @param projectTree Project tree of the main window.
   * @param database Database the address space belongs to.
   * @param project The project that contains the address spaces.
   * @param container View container of the project.
   */
  public CAddressSpacesTable(final JTree projectTree, final IDatabase database,
      final INaviProject project, final IViewContainer container) {
    super(projectTree, new CAddressSpacesModel(project), new CAddressSpacesTableHelp());

    m_database = Preconditions.checkNotNull(database, "IE02871: database argument can not be null");
    m_project = Preconditions.checkNotNull(project, "IE02872: project argument can not be null");
    m_container =
        Preconditions.checkNotNull(container, "IE02873: container argument can not be null");

    setDefaultRenderer(Object.class, new AddressSpaceLoadedRenderer());

    final InputMap windowImap = getInputMap(JComponent.WHEN_FOCUSED);

    windowImap.put(HotKeys.LOAD_HK.getKeyStroke(), "LOAD");
    getActionMap().put("LOAD", CActionProxy.proxy(new LoadAddressSpaceAction()));
  }

  /**
   * Returns the address spaces that correspond to the selected table rows.
   * 
   * @param sortSelectedRows The selected rows.
   * 
   * @return The address spaces that correspond to the selected rows.
   */
  private INaviAddressSpace[] getAddressSpaces(final int[] sortSelectedRows) {
    final INaviAddressSpace[] addressSpaces = new INaviAddressSpace[sortSelectedRows.length];

    for (int i = 0; i < addressSpaces.length; i++) {
      addressSpaces[i] = getTreeTableModel().getAddressSpaces().get(sortSelectedRows[i]);
    }

    return addressSpaces;
  }

  /**
   * This function is used to determine what address spaces are the targets of an address space
   * context menu. If the clicked row is inside the row selection interval, the selected address
   * spaces are the target. If the clicked row is outside the selection interval, the clicked row is
   * selected and the clicked address space is the target.
   * 
   * @param sortedRow The sorted row index of the clicked address space.
   * 
   * @return The address space targets for the context menu.
   */
  private INaviAddressSpace[] getSelectedAddressSpaces(final int sortedRow) {
    final int[] sortSelectedRows = getSortSelectedRows();
    if (Ints.asList(sortSelectedRows).indexOf(sortedRow) != -1) {
      return getAddressSpaces(sortSelectedRows);
    } else {
      final int viewRow = convertRowIndexToView(sortedRow);

      setRowSelectionInterval(viewRow, viewRow);

      return new INaviAddressSpace[] {getTreeTableModel().getAddressSpaces().get(sortedRow)};
    }
  }

  /**
   * From a list of address spaces, the address spaces that are not loaded are returned.
   * 
   * @param addressSpaces The address spaces to search through.
   * 
   * @return The unloaded address spaces.
   */
  private CAddressSpace[] getUnloadedAddressSpaces(final INaviAddressSpace[] addressSpaces) {
    final List<INaviAddressSpace> unloadedAddressSpaces = new ArrayList<INaviAddressSpace>();

    for (final INaviAddressSpace module : addressSpaces) {
      if (!module.isLoaded()) {
        unloadedAddressSpaces.add(module);
      }
    }

    return unloadedAddressSpaces.toArray(new CAddressSpace[] {});
  }

  @Override
  protected void deleteRows() {
    CProjectFunctions.removeAddressSpace(getParentWindow(), m_project,
        getAddressSpaces(getSortSelectedRows()), new CEmptyUpdater());
  }

  @Override
  protected JPopupMenu getPopupMenu(final int x, final int y, final int row) {
    // Create the popup menu for the address space that was right-clicked.
    final CAddressSpaceNodeMenuBuilder menu =
        new CAddressSpaceNodeMenuBuilder(getProjectTree(), null, this, m_database, m_project,
            getSelectedAddressSpaces(row), m_container);

    return menu.getPopupMenu();
  }

  @Override
  protected void handleDoubleClick(final int row) {
    // Do nothing when an address space is double-clicked.
  }

  @Override
  public CAddressSpacesModel getTreeTableModel() {
    return (CAddressSpacesModel) super.getTreeTableModel();
  }


  /**
   * Renderer that makes sure that loaded address spaces are shown differently from unloaded address
   * spaces.
   */
  private class AddressSpaceLoadedRenderer extends CLoadedRenderer {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = -7964915698433160450L;

    @Override
    public boolean isLoaded(final int row) {
      return getAddressSpaces(new int[] {convertRowIndexToModel(row)})[0].isLoaded();
    }
  }

  /**
   * Action class used for loading address spaces.
   */
  private class LoadAddressSpaceAction extends AbstractAction {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = -8036270322086134717L;

    @Override
    public void actionPerformed(final ActionEvent event) {
      CAddressSpaceFunctions.loadAddressSpaces(getProjectTree(),
          getUnloadedAddressSpaces(getAddressSpaces(getSortSelectedRows())));
    }
  }
}
