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


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModelListenerAdapter;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.IDebugPerspectiveModelListener;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryModule;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ProcessManagerListenerAdapter;
import com.google.security.zynamics.binnavi.debug.models.processmanager.interfaces.ProcessManagerListener;

/**
 * Synchronizes memory module table model with the memory modules in the debugged target process.
 */
public final class CModulesTableModelSynchronizer {
  /**
   * Table model to synchronize.
   */
  private final CModulesTableModel m_model;

  /**
   * Debug GUI to synchronize.
   */
  private final CDebugPerspectiveModel m_debugPerspectiveModel;

  /**
   * Renderer of the synchronized table.
   */
  private final CModulesTableRenderer m_renderer;

  /**
   * Synchronizes the GUI with debug events.
   */
  private final IDebugPerspectiveModelListener m_listener = new InternalDebuggerListener();

  /**
   * Synchronizes the GUI with process events.
   */
  private final ProcessManagerListener m_processListener = new InternalProcessListener();

  /**
   * Creates a new synchronizer object.
   *
   * @param table Table model to synchronize.
   * @param debugPerspectiveModel Debug GUI to synchronize.
   */
  public CModulesTableModelSynchronizer(
      final CModulesTable table, final CDebugPerspectiveModel debugPerspectiveModel) {
    Preconditions.checkNotNull(table, "IE01460: Model argument can not be null");

    Preconditions.checkNotNull(
        debugPerspectiveModel, "IE01461: Debug perspective model argument can not be null");

    m_model = table.getTreeTableModel();
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
      for (final MemoryModule module : newDebugger.getProcessManager().getModules()) {
        m_model.addModule(module);
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
    public void addedModule(final MemoryModule module) {
      m_renderer.addModule(module);
      m_model.addModule(module);
    }

    @Override
    public void detached() {
      m_model.reset();
    }

    @Override
    public void removedModule(final MemoryModule module) {
      m_model.removeModule(module);
    }
  }
}
