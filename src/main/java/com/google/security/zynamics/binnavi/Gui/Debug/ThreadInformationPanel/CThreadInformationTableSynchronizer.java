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


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModelListenerAdapter;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.IDebugPerspectiveModelListener;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ProcessManagerListenerAdapter;
import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;
import com.google.security.zynamics.binnavi.debug.models.processmanager.interfaces.ProcessManagerListener;

/**
 * Synchronizes the thread information table with the information from the target process.
 */
public class CThreadInformationTableSynchronizer {
  /**
   * Provides the debugger state to synchronize.
   */
  private final CDebugPerspectiveModel m_debugPerspectiveModel;

  /**
   * Table model to synchronize.
   */
  private final CThreadInformationTableModel m_model;

  /**
   * Synchronizes the GUI with debug events.
   */
  private final IDebugPerspectiveModelListener m_listener = new InternalDebuggerListener();

  /**
   * Synchronizes the GUI with process events.
   */
  private final ProcessManagerListener m_processListener = new InternalProcessListener();

  /**
   * Renders the threads in the table.
   */
  private final CThreadInformationTableRenderer m_renderer;

  /**
   * Creates a new synchronizer object.
   *
   * @param table The table to synchronize.
   * @param debugPerspectiveModel Provides the debugger state to synchronize.
   */
  public CThreadInformationTableSynchronizer(
      final CThreadInformationTable table, final CDebugPerspectiveModel debugPerspectiveModel) {
    Preconditions.checkNotNull(table, "IE00652: Model argument can not be null");

    Preconditions.checkNotNull(
        debugPerspectiveModel, "IE00653: Debug perspective model argument can not be null");

    m_model = table.getModel();
    m_renderer = table.getDefaultRenderer();
    m_debugPerspectiveModel = debugPerspectiveModel;

    synchronizeDebugger(null, debugPerspectiveModel.getCurrentSelectedDebugger());

    debugPerspectiveModel.addListener(m_listener);
  }

  /**
   * Keeps the listener on the active debugger.
   *
   * @param oldDebugger The previously active debugger.
   * @param newDebugger The now active debugger.
   */
  private void synchronizeDebugger(final IDebugger oldDebugger, final IDebugger newDebugger) {
    if (oldDebugger != null) {
      oldDebugger.getProcessManager().removeListener(m_processListener);
    }

    m_model.reset();

    if (newDebugger != null) {
      for (final TargetProcessThread thread : newDebugger.getProcessManager().getThreads()) {
        m_model.addThread(thread);
      }

      newDebugger.getProcessManager().addListener(m_processListener);
    }
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    m_debugPerspectiveModel.removeListener(m_listener);
    synchronizeDebugger(m_debugPerspectiveModel.getCurrentSelectedDebugger(), null);
  }

  /**
   * Synchronizes the GUI with debug events.
   */
  private class InternalDebuggerListener extends CDebugPerspectiveModelListenerAdapter {
    @Override
    public void changedActiveDebugger(final IDebugger oldDebugger, final IDebugger newDebugger) {
      synchronizeDebugger(oldDebugger, newDebugger);
    }
  }

  /**
   * Synchronizes the GUI with process events.
   */
  private class InternalProcessListener extends ProcessManagerListenerAdapter {
    @Override
    public void addedThread(final TargetProcessThread thread) {
      m_model.addThread(thread);

      m_renderer.addThread(thread);
    }

    @Override
    public void detached() {
      m_model.reset();
    }

    @Override
    public void removedThread(final TargetProcessThread thread) {
      m_model.removeThread(thread);
    }
  }
}
