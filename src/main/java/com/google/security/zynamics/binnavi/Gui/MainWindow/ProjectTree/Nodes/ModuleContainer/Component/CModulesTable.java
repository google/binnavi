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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.ModuleContainer.Component;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.HotKeys;
import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CModuleFunctions;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.DragAndDrop.CModuleDragHandler;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractTreeTable;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CLoadedRenderer;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Module.CModuleNodeMenuBuilder;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.ModuleContainer.Component.Help.CModulesTableHelp;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Updaters.CEmptyUpdater;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModule;

import java.awt.Color;
import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Table that is used to display all modules of a database.
 */
public final class CModulesTable extends CAbstractTreeTable<INaviModule> {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 7323735951997220913L;

  /**
   * Database the modules belong to.
   */
  private final IDatabase m_database;

  /**
   * Creates a new modules table.
   * 
   * @param projectTree The project tree of the main window.
   * @param database The database that contains the module information.
   */
  public CModulesTable(final JTree projectTree, final IDatabase database) {
    super(projectTree, new CModulesModel(database), new CModulesTableHelp());

    m_database = Preconditions.checkNotNull(database, "IE02870: database argument can not be null");

    setDefaultRenderer(Object.class, new ModuleLoadedRenderer());
    getColumnModel().getColumn(1).setCellRenderer(new CNameRenderer());

    getColumnModel().getColumn(0).setResizable(false);
    getColumnModel().getColumn(0).setPreferredWidth(50);
    getColumnModel().getColumn(0).setMaxWidth(50);

    if (!GraphicsEnvironment.isHeadless()) {
      setDragEnabled(true);
    }

    setTransferHandler(new CModuleDragHandler(this));

    final InputMap windowImap = getInputMap(JComponent.WHEN_FOCUSED);

    windowImap.put(HotKeys.LOAD_HK.getKeyStroke(), "LoadKeyStroke");
    getActionMap().put("LoadKeyStroke", CActionProxy.proxy(new LoadModuleAction()));
  }

  /**
   * Returns the modules that correspond to the selected table rows.
   * 
   * @param sortSelectedRows The selected rows.
   * 
   * @return The modules that correspond to the selected rows.
   */
  private INaviModule[] getModules(final int[] sortSelectedRows) {
    final INaviModule[] modules = new CModule[sortSelectedRows.length];

    for (int i = 0; i < modules.length; i++) {
      modules[i] = getTreeTableModel().getModules().get(sortSelectedRows[i]);
    }

    return modules;
  }

  /**
   * This function is used to determine what modules are the targets of a module context menu. If
   * the clicked row is inside the row selection interval, the selected modules are the target. If
   * the clicked row is outside the selection interval, the clicked row is selected and the clicked
   * module is the target.
   * 
   * @param sortedRow The sorted row index of the clicked module.
   * 
   * @return The module targets for the context menu.
   */
  private INaviModule[] getSelectedModules(final int sortedRow) {
    final int[] sortSelectedRows = getSortSelectedRows();
    if (Ints.asList(sortSelectedRows).indexOf(sortedRow) != -1) {
      return getModules(sortSelectedRows);
    } else {
      final int viewRow = convertRowIndexToView(sortedRow);

      setRowSelectionInterval(viewRow, viewRow);

      return new INaviModule[] {getTreeTableModel().getModules().get(sortedRow)};
    }
  }

  /**
   * From a list of modules, the modules that are not loaded are returned.
   * 
   * @param modules The modules to search through.
   * 
   * @return The unloaded modules.
   */
  private INaviModule[] getUnloadedModules(final INaviModule[] modules) {
    final List<INaviModule> unloadedModules = new ArrayList<INaviModule>();

    for (final INaviModule module : modules) {
      if (module.getConfiguration().getRawModule().isComplete() && !module.isLoaded()) {
        unloadedModules.add(module);
      }
    }

    return unloadedModules.toArray(new INaviModule[] {});
  }

  @Override
  protected void deleteRows() {
    CModuleFunctions.deleteModules(getParentWindow(), m_database,
        getModules(getSortSelectedRows()), new CEmptyUpdater());
  }

  @Override
  protected JPopupMenu getPopupMenu(final int x, final int y, final int row) {
    final CModuleNodeMenuBuilder menu =
        new CModuleNodeMenuBuilder(getProjectTree(), null, m_database, null,
            getSelectedModules(row), this);

    return menu.getPopupMenu();
  }

  @Override
  protected void handleDoubleClick(final int row) {
    // Nothing happens when a module row is double-clicked.
  }

  @Override
  public CModulesModel getTreeTableModel() {
    return (CModulesModel) super.getTreeTableModel();
  }

  /**
   * Renderer used to render the names column of the table.
   */
  private class CNameRenderer extends DefaultTableCellRenderer {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = -5822178069477234135L;

    @Override
    public Component getTableCellRendererComponent(final JTable table, final Object value,
        final boolean isSelected, final boolean hasFocus, final int row, final int column) {
      return new CModuleNameLabel(CModulesTable.this,
          getModules(new int[] {convertRowIndexToModel(row)})[0], isSelected
              ? table.getSelectionBackground() : Color.WHITE);
    }
  }

  /**
   * Action class used for loading modules.
   */
  private class LoadModuleAction extends AbstractAction {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = 3649209251674113287L;

    @Override
    public void actionPerformed(final ActionEvent event) {
      CModuleFunctions.loadModules(getProjectTree(),
          getUnloadedModules(getModules(getSortSelectedRows())));
    }
  }

  /**
   * Renderer class that makes sure that loaded modules are shown differently from unloaded modules.
   */
  private class ModuleLoadedRenderer extends CLoadedRenderer {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = -2926632553462013220L;

    @Override
    public boolean isLoaded(final int row) {
      return getModules(new int[] {convertRowIndexToModel(row)})[0].isLoaded();
    }
  }
}
