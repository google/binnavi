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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.AddressSpace.Component;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.HotKeys;
import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CAddressSpaceFunctions;
import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CModuleFunctions;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractTreeTable;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CLoadedRenderer;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Module.CModuleNodeMenuBuilder;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.ModuleContainer.Component.Help.CModulesTableHelp;
import com.google.security.zynamics.binnavi.disassembly.INaviAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JTree;

/**
 * Table that is used to display all modules of an address space.
 * 
 * TODO: Is this class necessary? Pretty much the same as CModulesTable.
 */
public final class CProjectModulesTable extends CAbstractTreeTable<INaviModule> {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 463220162389426879L;

  /**
   * Database the address space belongs to.
   */
  private final IDatabase m_database;

  /**
   * The address space that contains the modules.
   */
  private final INaviAddressSpace m_addressSpace;

  /**
   * Creates a new modules table.
   * 
   * @param projectTree Project tree of the main window.
   * @param database Database the address space belongs to.
   * @param addressSpace The address space described by the node.
   */
  public CProjectModulesTable(final JTree projectTree, final IDatabase database,
      final INaviAddressSpace addressSpace) {
    super(projectTree, new CProjectModulesModel(addressSpace), new CModulesTableHelp());

    m_database = Preconditions.checkNotNull(database, "IE02868: database argument can not be null");
    m_addressSpace =
        Preconditions.checkNotNull(addressSpace, "IE02869: addressSpace argument can not be null");

    setDefaultRenderer(Object.class, new ModuleLoadedRenderer());

    final InputMap windowImap = getInputMap(JComponent.WHEN_FOCUSED);

    windowImap.put(HotKeys.LOAD_HK.getKeyStroke(), "LOAD");
    getActionMap().put("LOAD", CActionProxy.proxy(new LoadModuleAction()));
  }

  /**
   * Returns the modules that correspond to the selected table rows.
   * 
   * @param sortSelectedRows The selected rows.
   * 
   * @return The modules that correspond to the selected rows.
   */
  private INaviModule[] getModules(final int[] sortSelectedRows) {
    final INaviModule[] modules = new INaviModule[sortSelectedRows.length];

    for (int i = 0; i < modules.length; i++) {
      modules[i] = m_addressSpace.getContent().getModules().get(sortSelectedRows[i]);
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

      return new INaviModule[] {m_addressSpace.getContent().getModules().get(sortedRow)};
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
      if (module.getConfiguration().getRawModule().isComplete() && module.isInitialized()
          && !module.isLoaded()) {
        unloadedModules.add(module);
      }
    }

    return unloadedModules.toArray(new INaviModule[] {});
  }


  @Override
  protected void deleteRows() {
    CAddressSpaceFunctions.removeModules(getParentWindow(), m_addressSpace,
        getModules(getSortSelectedRows()));
  }

  @Override
  protected JPopupMenu getPopupMenu(final int x, final int y, final int row) {
    final CModuleNodeMenuBuilder menu =
        new CModuleNodeMenuBuilder(getProjectTree(), null, m_database, m_addressSpace,
            getSelectedModules(row), this);

    return menu.getPopupMenu();
  }

  @Override
  protected void handleDoubleClick(final int row) {
    // Do nothing when a module is double-clicked
  }

  /**
   * Action class used for loading modules.
   */
  private class LoadModuleAction extends AbstractAction {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = 6811696892700433916L;

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
    private static final long serialVersionUID = -7356576005733389597L;

    @Override
    public boolean isLoaded(final int row) {
      return getModules(new int[] {convertRowIndexToModel(row)})[0].isLoaded();
    }
  }
}
