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
package com.google.security.zynamics.binnavi.Gui.Debug.ModulesPanel;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.Debug.ModulesPanel.Help.CMemoryModuleFilterHelp;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.CTablePanel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.CAbstractResultsPanel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryModule;

/**
 * Panel that is shown to display the modules in the address space of the debugged process while the
 * debugger is active.
 */
public final class CModulesPanel extends CAbstractResultsPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -6316724127804485173L;

  /**
   * Debug perspective model that provides the debugger that provides the module data.
   */
  private final CDebugPerspectiveModel m_debugPerspectiveModel;

  /**
   * The table that shows the modules.
   */
  private final CModulesTable m_table = new CModulesTable();

  /**
   * Synchronizer that keeps the table synchronized with the information coming from the debugger.
   */
  private final CModulesTableModelSynchronizer m_synchronizer;

  /**
   * Creates a new modules panel.
   *
   * @param debugPerspectiveModel Provides the debugger that provides the module data.
   */
  public CModulesPanel(final CDebugPerspectiveModel debugPerspectiveModel) {
    super(new BorderLayout());

    Preconditions.checkNotNull(
        debugPerspectiveModel, "IE01251: Debug perspective model argument can not be null");

    m_debugPerspectiveModel = debugPerspectiveModel;

    add(new CModulesCheckBoxPanel(m_table.getTreeTableModel()), BorderLayout.NORTH);
    add(new CTablePanel<MemoryModule>(
        m_table, new CMemoryModuleFilterCreator(), new CMemoryModuleFilterHelp()));

    m_synchronizer = new CModulesTableModelSynchronizer(m_table, debugPerspectiveModel);

    m_table.addMouseListener(new InternalMouseListener());
  }

  /**
   * Returns the row that was clicked on a mouse event.
   *
   * @param event The mouse event.
   *
   * @return The clicked row.
   */
  private int getSelectionIndex(final MouseEvent event) {
    return m_table.convertRowIndexToModel(m_table.rowAtPoint(event.getPoint()));
  }

  @Override
  public void dispose() {
    m_synchronizer.dispose();
  }

  @Override
  public String getTitle() {
    return "Modules";
  }

  /**
   * Displays a popup menu when the user right-clicks on a module in the table.
   */
  private class InternalMouseListener extends MouseAdapter {
    /**
     * Displays a popup menu when the user right-clicks on a module in the table.
     *
     * @param event The mouse event.
     */
    private void displayPopupMenu(final MouseEvent event) {
      final int selectedIndex = getSelectionIndex(event);

      if (selectedIndex != -1) {
        final MemoryModule memoryModule =
            m_table.getTreeTableModel().getModules().get(selectedIndex);

        final CModulesTableMenu popupMenu = new CModulesTableMenu(
            SwingUtilities.getWindowAncestor(CModulesPanel.this), m_debugPerspectiveModel,
            memoryModule);

        popupMenu.show(m_table, event.getX(), event.getY());
      }
    }

    @Override
    public void mousePressed(final MouseEvent event) {
      if (event.isPopupTrigger()) {
        displayPopupMenu(event);
      }
    }

    @Override
    public void mouseReleased(final MouseEvent event) {
      if (event.isPopupTrigger()) {
        displayPopupMenu(event);
      }
    }
  }
}
