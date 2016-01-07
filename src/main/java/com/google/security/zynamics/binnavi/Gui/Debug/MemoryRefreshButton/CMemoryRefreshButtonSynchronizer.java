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
package com.google.security.zynamics.binnavi.Gui.Debug.MemoryRefreshButton;

import javax.swing.Action;
import javax.swing.ImageIcon;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModelListenerAdapter;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.IDebugPerspectiveModelListener;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ProcessManagerListenerAdapter;
import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;
import com.google.security.zynamics.binnavi.debug.models.processmanager.interfaces.ProcessManagerListener;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerOptions;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.TargetInformation;

/**
 * Synchronizes a memory refresh button with the state of a given debug GUI perspective.
 */
public final class CMemoryRefreshButtonSynchronizer {
  /**
   * The refresh button to synchronize.
   */
  private final CMemoryRefreshButton m_refreshButton;

  /**
   * The debug GUI perspective to synchronize.
   */
  private final CDebugPerspectiveModel m_debugPerspectiveModel;

  /**
   * Default refresh action.
   */
  private final Action m_defaultAction;

  /**
   * Alternative action class for debuggers that do not support memory sections.
   */
  private final Action m_askAction;

  /**
   * Keeps track of relevant changes in the available debugger options.
   */
  private final ProcessManagerListener m_internalProcessListener = new InternalProcessListener();

  /**
   * Keeps track of relevant changes in the debug GUI perspective.
   */
  private final IDebugPerspectiveModelListener m_debugListener = new InternalDebugListener();

  /**
   * Creates a new refresh button synchronizer.
   *
   * @param refreshButton The refresh button to synchronize.
   * @param debugPerspectiveModel The debug GUI perspective to synchronize.
   * @param defaultAction Default refresh action.
   * @param askAction Alternative action class for debuggers that do not support memory sections.
   */
  public CMemoryRefreshButtonSynchronizer(final CMemoryRefreshButton refreshButton,
      final CDebugPerspectiveModel debugPerspectiveModel, final Action defaultAction,
      final Action askAction) {
    Preconditions.checkNotNull(refreshButton, "IE01442: Refresh button argument can not be null");

    Preconditions.checkNotNull(
        debugPerspectiveModel, "IE01443: Debug perspective model argument can not be null");

    Preconditions.checkNotNull(defaultAction, "IE01444: Default action argument can not be null");

    Preconditions.checkNotNull(askAction, "IE01445: Ask action argument can not be null");

    m_refreshButton = refreshButton;
    m_debugPerspectiveModel = debugPerspectiveModel;
    m_defaultAction = defaultAction;
    m_askAction = askAction;

    m_refreshButton.setAction(m_defaultAction);

    synchronizeDebugger(null, debugPerspectiveModel.getCurrentSelectedDebugger());

    debugPerspectiveModel.addListener(m_debugListener);
  }

  /**
   * Synchronizes the refresh button with the currently selected debugger in the debug GUI
   * perspective.
   *
   * @param oldDebugger The previously selected debugger.
   * @param newDebugger The currently selected debugger.
   */
  private void synchronizeDebugger(final IDebugger oldDebugger, final IDebugger newDebugger) {
    if (oldDebugger != null) {
      oldDebugger.getProcessManager().removeListener(m_internalProcessListener);
    }

    if (newDebugger != null) {
      final TargetInformation targetInformation =
          newDebugger.getProcessManager().getTargetInformation();

      if (targetInformation != null) {
        updateGuiFromOptions(targetInformation.getDebuggerOptions());
      }

      newDebugger.getProcessManager().addListener(m_internalProcessListener);
    }

    updateGui();
  }

  /**
   * Updates the button according to the currently available information about the target process.
   */
  private void updateGui() {
    final IDebugger debugger = m_debugPerspectiveModel.getCurrentSelectedDebugger();
    final TargetProcessThread thread = debugger == null ? null : debugger.getProcessManager().getActiveThread();

    final boolean connected = debugger != null && debugger.isConnected();
    final boolean suspended = connected && thread != null;

    m_refreshButton.setEnabled(suspended);
  }

  /**
   * Updates the button according to the options information supported by the target debugger.
   *
   * @param options The supported debugger options.
   */
  private void updateGuiFromOptions(final DebuggerOptions options) {
    m_refreshButton.setAction(options.canValidMemory() ? m_defaultAction : m_askAction);

    m_refreshButton.setIcon(new ImageIcon(CMain.class.getResource("data/memoryupdate_up.jpg")));
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    m_debugPerspectiveModel.removeListener(m_debugListener);
    synchronizeDebugger(m_debugPerspectiveModel.getCurrentSelectedDebugger(), null);
  }

  /**
   * This listener is responsible for keeping the synchronizer updated if the debug GUI perspective
   * changes.
   */
  private class InternalDebugListener extends CDebugPerspectiveModelListenerAdapter {
    @Override
    public void changedActiveDebugger(final IDebugger oldDebugger, final IDebugger newDebugger) {
      synchronizeDebugger(oldDebugger, newDebugger);
    }
  }

  /**
   * Keeps track of changes in the debugged process and updates the refresh button if new
   * information about the target process arrive.
   */
  private final class InternalProcessListener extends ProcessManagerListenerAdapter {
    @Override
    public void changedActiveThread(final TargetProcessThread oldThread, final TargetProcessThread newThread) {
      updateGui();
    }

    @Override
    public void changedTargetInformation(final TargetInformation information) {
      updateGuiFromOptions(information.getDebuggerOptions());

      updateGui();
    }

    @Override
    public void detached() {
      updateGui();
    }
  }
}
