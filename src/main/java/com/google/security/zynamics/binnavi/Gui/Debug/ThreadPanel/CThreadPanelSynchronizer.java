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
package com.google.security.zynamics.binnavi.Gui.Debug.ThreadPanel;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModelListenerAdapter;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.IDebugPerspectiveModelListener;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ProcessManagerListenerAdapter;
import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;
import com.google.security.zynamics.binnavi.debug.models.processmanager.interfaces.ProcessManagerListener;

/**
 * Synchronizes the active Debug GUI perspective with the thread selection box.
 */
public final class CThreadPanelSynchronizer {
  /**
   * The thread box to synchronize with the Debug GUI perspective.
   */
  private final CThreadComboBox m_tidBox;

  /**
   * The Debug GUI perspective to synchronize with the thread panel.
   */
  private final CDebugPerspectiveModel m_debugPerspectiveModel;

  /**
   * Updates the Debug GUI perspective on selection changes in the thread box.
   */
  private final ItemListener m_boxListener = new InternalBoxListener();

  /**
   * Listens on relevant changes in the Debug GUI perspective.
   */
  private final IDebugPerspectiveModelListener m_debugListener = new InternalDebugListener();

  /**
   * Listens on relevant events in the process of the active debugger.
   */
  private final ProcessManagerListener m_processListener = new InternalProcessListener();

  /**
   * Creates a new thread box synchronizer object.
   *
   * @param tidBox The thread box to synchronize with the Debug GUI perspective.
   * @param debugPerspectiveModel The Debug GUI perspective to synchronize with the thread panel.
   */
  public CThreadPanelSynchronizer(
      final CThreadComboBox tidBox, final CDebugPerspectiveModel debugPerspectiveModel) {
    m_tidBox = Preconditions.checkNotNull(tidBox, "IE01519: Thead box argument can not be null");
    m_debugPerspectiveModel = Preconditions.checkNotNull(
        debugPerspectiveModel, "IE01520: Debug perspective model argument can not be null");

    m_debugPerspectiveModel.addListener(m_debugListener);
    m_tidBox.addItemListener(m_boxListener);

    synchronizeDebugger(null, debugPerspectiveModel.getCurrentSelectedDebugger());

    updateGui();
  }

  /**
   * Removes all listeners from the previously active debugger and adds all listeners to the
   * currently active debugger.
   *
   * @param oldDebugger The previously active debugger.
   * @param newDebugger The currently active debugger.
   */
  private void synchronizeDebugger(final IDebugger oldDebugger, final IDebugger newDebugger) {
    if (oldDebugger != null) {
      oldDebugger.getProcessManager().removeListener(m_processListener);
    }

    if (newDebugger != null) {
      newDebugger.getProcessManager().addListener(m_processListener);
    }
  }

  /**
   * Re-populates the thread box with all the threads of the currently selected debugger.
   */
  private void updateGui() {
    m_tidBox.removeAllItems();

    final IDebugger debugger = m_debugPerspectiveModel.getCurrentSelectedDebugger();

    if (debugger == null) {
      m_tidBox.setEnabled(false);
    } else {
      final List<TargetProcessThread> threads = debugger.getProcessManager().getThreads();

      for (final TargetProcessThread thread : threads) {
        m_tidBox.addItem(thread);
      }

      if (!threads.isEmpty()) {
        m_tidBox.setSelectedIndex(0);
      }

      m_tidBox.setEnabled(true);
    }
  }

  /**
   * Clears up allocated resources.
   */
  public void dispose() {
    m_debugPerspectiveModel.removeListener(m_debugListener);

    synchronizeDebugger(m_debugPerspectiveModel.getCurrentSelectedDebugger(), null);
  }

  /**
   * Whenever the selection of the thread box is changed manually, the now selected thread must be
   * selected in the Debug GUI perspective too. This listener is responsible for this.
   */
  private class InternalBoxListener implements ItemListener {
    @Override
    public void itemStateChanged(final ItemEvent event) {
      final IDebugger debugger = m_debugPerspectiveModel.getCurrentSelectedDebugger();

      if (debugger != null) {
        debugger.getProcessManager().setActiveThread(m_tidBox.getSelectedItem());
      }
    }
  }

  /**
   * This listener is responsible for updating the thread box on relevant events in the Debug GUI
   * perspective.
   */
  private class InternalDebugListener extends CDebugPerspectiveModelListenerAdapter {
    @Override
    public void changedActiveDebugger(final IDebugger oldDebugger, final IDebugger newDebugger) {
      updateGui();

      synchronizeDebugger(oldDebugger, newDebugger);
    }
  }

  /**
   * This listener is responsible for updating the thread box on relevant events in the debugged
   * process of the active debugger.
   */
  private class InternalProcessListener extends ProcessManagerListenerAdapter {
    @Override
    public void addedThread(final TargetProcessThread thread) {
      m_tidBox.addItem(thread);
    }

    @Override
    public void attached() {
      updateGui();
    }

    @Override
    public void changedActiveThread(final TargetProcessThread oldThread, final TargetProcessThread newThread) {
      m_tidBox.setSelectedItem(newThread);
    }

    @Override
    public void detached() {
      updateGui();
    }

    @Override
    public void removedThread(final TargetProcessThread thread) {
      m_tidBox.removeItem(thread);
    }
  }
}
