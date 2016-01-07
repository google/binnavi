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
package com.google.security.zynamics.binnavi.Gui.Debug.DebuggerSelectionPanel;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;

/**
 * Synchronizes a debugger selection box with a debug perspective model.
 */
public final class CDebuggerSelectionPanelSynchronizer {
  /**
   * The debugger selection box that is synchronized with the debug GUI perspective.
   */
  private final CDebuggerComboBox m_debuggerBox;

  /**
   * The debug GUI perspective that is synchronized with the debugger selection box.
   */
  private final CDebugPerspectiveModel m_debugPerspectiveModel;

  /**
   * Listener that keeps track of changes in the debugger selection box.
   */
  private final ItemListener m_internalItemListener = new InternalItemListener();

  /**
   * Creates a new synchronizer object that synchronizes a debugger selection box with a debug GUI
   * perspective.
   *
   * @param debuggerBox The debugger selection box that is synchronized with the debug GUI
   *        perspective.
   * @param debugPerspectiveModel The debug GUI perspective that is synchronized with the debugger
   *        selection box.
   */
  public CDebuggerSelectionPanelSynchronizer(
      final CDebuggerComboBox debuggerBox, final CDebugPerspectiveModel debugPerspectiveModel) {
    m_debuggerBox =
        Preconditions.checkNotNull(debuggerBox, "IE01366: Debugger box can not be null");
    m_debugPerspectiveModel = Preconditions.checkNotNull(
        debugPerspectiveModel, "IE01367: Debug perspective model can not be null");

    // When the user selects another debugger, we have to update
    // the debugger display.
    m_debuggerBox.addItemListener(m_internalItemListener);

    // If we have at least one debugger, we can display the panel of that debugger.
    if (m_debuggerBox.getItemCount() != 0) {
      final IDebugger debugger = m_debuggerBox.getSelectedItem().getObject();

      // ATTENTION: We are doing this activeThread resetting to solve Case 2037 (Problem with
      // register view in new graph window)
      // which led to incorrect threads being pre-selected in new graph windows.

      final TargetProcessThread activeThread =
          debugger == null ? null : debugger.getProcessManager().getActiveThread();

      m_debugPerspectiveModel.setActiveDebugger(debugger);

      if (debugger != null) {
        debugger.getProcessManager().setActiveThread(activeThread);
      }
    }
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    m_debuggerBox.removeItemListener(m_internalItemListener);
  }

  /**
   * Updates the debugger combobox when another debugger was selected.
   */
  private class InternalItemListener implements ItemListener {
    @Override
    public void itemStateChanged(final ItemEvent event) {
      final CDebuggerWrapper mapper = m_debuggerBox.getSelectedItem();

      if (mapper == null) {
        m_debugPerspectiveModel.setActiveDebugger(null);
      } else {
        final IDebugger debugger = m_debuggerBox.getSelectedItem().getObject();

        m_debugPerspectiveModel.setActiveDebugger(debugger);
      }
    }
  }
}
