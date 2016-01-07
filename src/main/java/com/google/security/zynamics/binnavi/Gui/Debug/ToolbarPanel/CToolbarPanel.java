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
package com.google.security.zynamics.binnavi.Gui.Debug.ToolbarPanel;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.Debug.ThreadPanel.CThreadPanel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;
import com.google.security.zynamics.zylib.gui.CMessageBox;

/**
 * Encapsulates the toolbar and the thread panel which are both located on top of the debugger
 * control panel. Furthermore a synchronizer is used to synchronize the toolbar panel with the
 * active debug GUI perspective.
 */
public final class CToolbarPanel extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -3977448670006322794L;

  /**
   * Parent window of the tool bar.
   */
  private final JFrame m_parent;

  /**
   * Synchronizes the toolbar panel with the active debug GUI perspective.
   */
  private final CToolbarPanelSynchronizer m_synchronizer;

  /**
   * Synchronizes the tool bar with its trace logger.
   */
  private final IToolbarPanelSynchronizerListener m_internalSynchronizerListener =
      new InternalSynchronizerListener();

  private final CThreadPanel m_threadPanel;

  /**
   * Creates a new toolbar panel.
   *
   * @param parent Parent window of the tool bar.
   * @param debugPerspectiveModel Debugger that is debugged by the toolbar.
   */
  public CToolbarPanel(final JFrame parent, final CDebugPerspectiveModel debugPerspectiveModel) {
    super(new BorderLayout());

    Preconditions.checkNotNull(parent, "IE01523: Parent argument can not be null");

    Preconditions.checkNotNull(
        debugPerspectiveModel, "IE01522: Debug perspective model argument can not be null");

    m_parent = parent;

    final JPanel toolbarPanel = new JPanel(new BorderLayout());
    toolbarPanel.setBorder(new EmptyBorder(1, 1, 1, 1));

    m_threadPanel = new CThreadPanel(debugPerspectiveModel);
    final CDebuggerToolbar toolbar = new CDebuggerToolbar(debugPerspectiveModel);

    toolbarPanel.add(toolbar, BorderLayout.WEST);
    toolbarPanel.add(m_threadPanel, BorderLayout.CENTER);

    add(toolbarPanel, BorderLayout.SOUTH);

    m_synchronizer = new CToolbarPanelSynchronizer(toolbar, m_threadPanel, debugPerspectiveModel);

    m_synchronizer.addListener(m_internalSynchronizerListener);
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    m_threadPanel.dispose();

    m_synchronizer.removeListener(m_internalSynchronizerListener);
    m_synchronizer.dispose();
  }

  /**
   * Synchronizes the tool bar with its trace logger.
   */
  private class InternalSynchronizerListener implements IToolbarPanelSynchronizerListener {
    @Override
    public void errorSavingTrace(final TraceList list) {
      CMessageBox.showError(m_parent,
          String.format("The trace '%s' could not be saved to the database.", list.getName()));
    }
  }
}
