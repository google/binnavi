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
package com.google.security.zynamics.binnavi.Gui.Debug.StackPanel;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModelListenerAdapter;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.IDebugPerspectiveModelListener;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ProcessManagerListenerAdapter;
import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ThreadListenerAdapter;
import com.google.security.zynamics.binnavi.debug.models.processmanager.interfaces.ProcessManagerListener;
import com.google.security.zynamics.binnavi.debug.models.processmanager.interfaces.ThreadListener;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.RegisterValue;
import com.google.security.zynamics.zylib.gui.JHexPanel.JHexView.DefinitionStatus;
import com.google.security.zynamics.zylib.gui.JStackView.JStackView;

/**
 * Synchronizes a stack view with a debug GUI perspective.
 */
public final class CStackViewSynchronizer {
  /**
   * Stack view to be synchronized.
   */
  private final JStackView m_stackView;

  /**
   * Stack memory provider to be synchronized.
   */
  private final CStackMemoryProvider m_model;

  /**
   * Debug perspective information that is used for synchronization.
   */
  private final CDebugPerspectiveModel m_debugPerspectiveModel;

  /**
   * Keeps track of changes in the debug GUI.
   */
  private final IDebugPerspectiveModelListener m_listener = new InternalPerspectiveListener();

  /**
   * Keeps track of changes in the process.
   */
  private final ProcessManagerListener m_processListener = new ProcessManagerListenerAdapter() {
    @Override
    public void changedActiveThread(final TargetProcessThread oldThread, final TargetProcessThread newThread) {
      synchronizeThread(oldThread, newThread);

      updateGui();
    }

  };

  /**
   * Updates the GUI on changes to the active thread.
   */
  private final ThreadListener m_threadListener = new InternalThreadListener();

  /**
   * Creates a new stack view synchronizer.
   *
   * @param stackView Stack view to be synchronized.
   * @param model Stack memory provider to be synchronized.
   * @param debugPerspectiveModel Debug perspective information that is used for synchronization.
   */
  public CStackViewSynchronizer(final JStackView stackView, final CStackMemoryProvider model,
      final CDebugPerspectiveModel debugPerspectiveModel) {
    Preconditions.checkNotNull(stackView, "IE01506: Stack view argument can not be null");
    Preconditions.checkNotNull(model, "IE01507: Model argument can not be null");
    Preconditions.checkNotNull(
        debugPerspectiveModel, "IE01508: Debug perspective model argument can not be null");

    m_stackView = stackView;
    m_model = model;
    m_debugPerspectiveModel = debugPerspectiveModel;
    m_debugPerspectiveModel.addListener(m_listener);

    synchronizeDebuggers(null, m_debugPerspectiveModel.getCurrentSelectedDebugger());
  }

  /**
   * Returns the stack pointer of the active thread.
   *
   * @param activeThread The active thread.
   *
   * @return The stack pointer of the active thread.
   */
  private long getStackPointer(final TargetProcessThread activeThread) {
    for (final RegisterValue registerValue : activeThread.getRegisterValues()) {
      if (registerValue.isSp()) {
        return registerValue.getValue().longValue();
      }
    }

    return 0;
  }

  /**
   * Makes sure that listeners are added at the right debuggers.
   *
   * @param oldDebugger The previously active debugger.
   * @param newDebugger The newly active debugger.
   */
  private void synchronizeDebuggers(final IDebugger oldDebugger, final IDebugger newDebugger) {
    if (oldDebugger != null) {
      oldDebugger.getProcessManager().removeListener(m_processListener);

      final TargetProcessThread activeThread = oldDebugger.getProcessManager().getActiveThread();

      synchronizeThread(activeThread, null);
    }

    if (newDebugger != null) {
      newDebugger.getProcessManager().addListener(m_processListener);

      final TargetProcessThread activeThread = newDebugger.getProcessManager().getActiveThread();

      synchronizeThread(null, activeThread);
    }
  }

  /**
   * Keeps thread listeners synchronized on changing threads.
   *
   * @param oldThread The previously active thread.
   * @param newThread The new active thread.
   */
  private void synchronizeThread(final TargetProcessThread oldThread, final TargetProcessThread newThread) {
    if (oldThread != null) {
      oldThread.removeListener(m_threadListener);
    }

    if (newThread != null) {
      newThread.addListener(m_threadListener);
    }

    m_model.setActiveThread(newThread);
  }

  /**
   * Updates the GUI depending on the current state of the target process.
   */
  private void updateGui() {
    final IDebugger activeDebugger = m_debugPerspectiveModel.getCurrentSelectedDebugger();
    final TargetProcessThread activeThread =
        activeDebugger == null ? null : activeDebugger.getProcessManager().getActiveThread();

    if (activeThread == null) {
      m_stackView.setDefinitionStatus(DefinitionStatus.UNDEFINED);
      m_stackView.setEnabled(false);
    } else {
      m_stackView.setDefinitionStatus(DefinitionStatus.DEFINED);
      m_stackView.setEnabled(true);

      m_stackView.gotoOffset(getStackPointer(activeThread));
    }
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    m_debugPerspectiveModel.removeListener(m_listener);

    synchronizeDebuggers(m_debugPerspectiveModel.getCurrentSelectedDebugger(), null);
  }

  /**
   * Listener that keeps track of changes in the GUI. If either a new debugger or a new thread is
   * selected, the stack window must be updated to reflect this change.
   */
  private class InternalPerspectiveListener extends CDebugPerspectiveModelListenerAdapter {
    @Override
    public void changedActiveDebugger(final IDebugger oldDebugger, final IDebugger newDebugger) {
      m_model.setDebugger(newDebugger);

      synchronizeDebuggers(oldDebugger, newDebugger);

      updateGui();
    }
  }

  /**
   * Updates the GUI on changes to the active thread.
   */
  private class InternalThreadListener extends ThreadListenerAdapter {
    @Override
    public void registersChanged(final TargetProcessThread thread) {
      updateGui();
    }
  }
}
