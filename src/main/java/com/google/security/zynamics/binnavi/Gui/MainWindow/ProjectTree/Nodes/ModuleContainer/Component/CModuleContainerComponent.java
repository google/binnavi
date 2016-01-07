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

import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;
import javax.swing.JTree;

import com.google.security.zynamics.binnavi.Database.CDatabaseListenerAdapter;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.CTablePanel;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.IFilterFieldListener;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.AddressSpace.Component.Help.CModuleFilterHelp;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.filters.CModuleFilterCreator;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;


/**
 * Component that is displayed on the right side of the main window whenever a modules node was
 * selected.
 */
public final class CModuleContainerComponent extends CTablePanel<INaviModule> {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 8335750212998290254L;

  /**
   * Database from where the displayed modules are read.
   */
  private final IDatabase m_database;

  /**
   * Updates the GUI on relevant changes in the database object.
   */
  private final InternalDatabaseListener m_listener = new InternalDatabaseListener();

  /**
   * Shows a context menu when the filter field is clicked.
   */
  private final IFilterFieldListener m_filterFieldListener = new InternalFilterFieldListener();

  /**
   * Creates a new component object.
   * 
   * @param projectTree Project tree that is updated on certain events.
   * @param database Database from where the displayed modules are read.
   */
  public CModuleContainerComponent(final JTree projectTree, final IDatabase database) {
    super(new CModulesTable(projectTree, database), new CModuleFilterCreator(),
        new CModuleFilterHelp());

    // Arguments are checked in CModulesTable

    m_database = database;

    m_database.addListener(m_listener);

    addListener(m_filterFieldListener);

    updateBorderText(getBorderText());
  }

  /**
   * Creates the border text that displays the number of modules in the database.
   * 
   * @return The created border text.
   */
  private String getBorderText() {
    return String
        .format("%d Modules in Database '%s'", m_database.getContent().getModules().size(),
            m_database.getConfiguration().getDescription());
  }

  @Override
  protected void disposeInternal() {
    m_database.removeListener(m_listener);

    removeListener(m_filterFieldListener);
  }

  /**
   * Updates the GUI on relevant changes in the database object.
   */
  private class InternalDatabaseListener extends CDatabaseListenerAdapter {
    @Override
    public void addedModule(final IDatabase database, final INaviModule module) {
      updateBorderText(getBorderText());
    }

    @Override
    public void deletedModule(final IDatabase database, final INaviModule module) {
      updateBorderText(getBorderText());
    }
  }

  /**
   * Shows a context menu when the filter field is clicked.
   */
  private class InternalFilterFieldListener implements IFilterFieldListener {
    /**
     * Shows a context menu depending on the mouse event.
     * 
     * @param event The mouse event the context menu depends on.
     */
    private void showPopupMenu(final MouseEvent event) {
      final JPopupMenu menu = new CModuleFilterFieldMenu(getFilterField());

      menu.show(event.getComponent(), event.getX(), event.getY());
    }

    @Override
    public void mousePressed(final MouseEvent event) {
      if (event.isPopupTrigger()) {
        showPopupMenu(event);
      }
    }

    @Override
    public void mouseReleased(final MouseEvent event) {
      if (event.isPopupTrigger()) {
        showPopupMenu(event);
      }
    }
  }
}
