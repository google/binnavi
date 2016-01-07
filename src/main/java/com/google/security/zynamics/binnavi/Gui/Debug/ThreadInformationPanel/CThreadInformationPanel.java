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
package com.google.security.zynamics.binnavi.Gui.Debug.ThreadInformationPanel;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.CAbstractResultsPanel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;

/**
 * Panel that displays information about the threads of the target process.
 */
public class CThreadInformationPanel extends CAbstractResultsPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 3156187195132738483L;

  /**
   * Updates the information on changes to the debugger GUI.
   */
  private final CDebugPerspectiveModel m_debugPerspectiveModel;

  /**
   * Table where the thread information is shown.
   */
  private final CThreadInformationTable m_table = new CThreadInformationTable();

  /**
   * Synchronizes the process with the table.
   */
  private final CThreadInformationTableSynchronizer m_synchronizer;

  /**
   * Creates a new thread information panel.
   *
   * @param debugPerspectiveModel Updates the information on changes to the debugger GUI.
   */
  public CThreadInformationPanel(final CDebugPerspectiveModel debugPerspectiveModel) {
    super(new BorderLayout());

    Preconditions.checkNotNull(
        debugPerspectiveModel, "IE00647: Debug perspective model argument can not be null");

    m_debugPerspectiveModel = debugPerspectiveModel;

    add(new JScrollPane(m_table));

    m_synchronizer = new CThreadInformationTableSynchronizer(m_table, debugPerspectiveModel);

    m_table.addMouseListener(new InternalMouseListener());
  }

  @Override
  public void dispose() {
    m_synchronizer.dispose();
  }

  @Override
  public String getTitle() {
    return "Threads";
  }

  /**
   * Displays a popup menu when the user right-clicks on a thread in the table.
   */
  private class InternalMouseListener extends MouseAdapter {
    /**
     * Displays a popup menu when the user right-clicks on a thread in the table.
     *
     * @param event The mouse event.
     */
    private void displayPopupMenu(final MouseEvent event) {
      final int selectedIndex = getSelectionIndex(event);

      if (selectedIndex != -1) {
        final TargetProcessThread thread = m_table.getModel().getThreads().get(selectedIndex);

        final IDebugger debugger = m_debugPerspectiveModel.getCurrentSelectedDebugger();

        final CThreadInformationTableMenu popupMenu = new CThreadInformationTableMenu(SwingUtilities
            .getWindowAncestor(CThreadInformationPanel.this), debugger, thread);

        popupMenu.show(m_table, event.getX(), event.getY());
      }
    }

    /**
     * Returns the row that was clicked on a mouse event.
     *
     * @param event The mouse event.
     *
     * @return The clicked row.
     */
    private int getSelectionIndex(final MouseEvent event) {
      return m_table.rowAtPoint(event.getPoint());
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
