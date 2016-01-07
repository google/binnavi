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
package com.google.security.zynamics.binnavi.Gui.Debug.RegisterPanel;


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
import com.google.security.zynamics.binnavi.debug.models.targetinformation.RegisterDescription;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.RegisterValue;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.TargetInformation;
import com.google.security.zynamics.zylib.gui.JRegisterView.JRegisterView;
import com.google.security.zynamics.zylib.types.lists.FilledList;

/**
 * Updates a register view when relevant changes in the GUI or the register values happen.
 */
public final class CRegisterViewSynchronizer {
  /**
   * The register view to synchronize.
   */
  private final JRegisterView m_registerView;

  /**
   * The data provider to synchronize.
   */
  private final CRegisterProvider m_dataProvider;

  /**
   * The perspective model that provides information about the GUI state.
   */
  private final CDebugPerspectiveModel m_debugPerspectiveModel;

  /**
   * Keeps track of changes in the GUI.
   */
  private final IDebugPerspectiveModelListener m_debugListener = new InternalDebugListener();

  /**
   * Keeps track of relevant register changes.
   */
  private final ThreadListener m_threadListener = new InternalThreadListener();

  /**
   * Keeps track of relevant changes in the process of the active debugger.
   */
  private final ProcessManagerListener m_processListener = new InternalProcessListener();

  /**
   * Creates a new register view synchronizer.
   *
   * @param registerView The register view to synchronize.
   * @param dataProvider The data provider to synchronize.
   * @param debugPerspectiveModel The perspective model that provides information about the GUI
   *        state.
   */
  public CRegisterViewSynchronizer(final JRegisterView registerView,
      final CRegisterProvider dataProvider, final CDebugPerspectiveModel debugPerspectiveModel) {
    Preconditions.checkNotNull(registerView, "IE01479: Register view argument can not be null");

    Preconditions.checkNotNull(dataProvider, "IE01480: Data provider argument can not be null");

    Preconditions.checkNotNull(
        debugPerspectiveModel, "IE01481: Debug perspective model argument can not be null");

    m_registerView = registerView;
    m_dataProvider = dataProvider;
    m_debugPerspectiveModel = debugPerspectiveModel;

    final IDebugger activeDebugger = debugPerspectiveModel.getCurrentSelectedDebugger();

    synchronizeDebugger(null, activeDebugger);

    debugPerspectiveModel.addListener(m_debugListener);
  }

  /**
   *
   *
   * @param oldDebugger
   * @param activeDebugger
   */
  private void synchronizeDebugger(final IDebugger oldDebugger, final IDebugger activeDebugger) {
    TargetProcessThread oldThread = null;

    if (oldDebugger != null) {
      oldDebugger.getProcessManager().removeListener(m_processListener);
      oldThread = oldDebugger.getProcessManager().getActiveThread();
    }

    if (activeDebugger != null) {
      final TargetInformation targetInformation =
          activeDebugger.getProcessManager().getTargetInformation();

      if (targetInformation != null) {
        m_dataProvider.setRegisterDescription(targetInformation.getRegisters());
      }

      activeDebugger.getProcessManager().addListener(m_processListener);

      synchronizeThread(oldThread, activeDebugger.getProcessManager().getActiveThread());
    }
  }

  /**
   * Shows the register values of a given thread in the GUI.
   *
   * @param oldThread The previously active thread.
   * @param newThread The thread to display.
   */
  private void synchronizeThread(final TargetProcessThread oldThread, final TargetProcessThread newThread) {
    if (oldThread != null) {
      oldThread.removeListener(m_threadListener);
    }

    if (newThread != null) {
      m_dataProvider.setRegisterInformation(newThread.getRegisterValues());
      newThread.addListener(m_threadListener);
    }

    updateGui();
  }

  /**
   * Updates the GUI according to the state of the debugger.
   */
  private void updateGui() {
    final IDebugger activeDebugger = m_debugPerspectiveModel.getCurrentSelectedDebugger();
    final TargetProcessThread activeThread =
        activeDebugger == null ? null : activeDebugger.getProcessManager().getActiveThread();

    m_registerView.setEnabled(activeThread != null && activeDebugger != null
        && activeDebugger.isConnected());
  }

  /**
   * Cleans up allocated resources.
   */
  public void dispose() {
    m_debugPerspectiveModel.removeListener(m_debugListener);

    final IDebugger activeDebugger = m_debugPerspectiveModel.getCurrentSelectedDebugger();
    final TargetProcessThread activeThread =
        activeDebugger == null ? null : activeDebugger.getProcessManager().getActiveThread();

    if (activeThread != null) {
      activeThread.removeListener(m_threadListener);
    }
  }

  /**
   * This listener is responsible for keeping a thread listener on the thread that is selected in
   * the GUI. Every time the selected GUI thread changes, the thread listener must be moved from the
   * formerly active thread to the currently active thread.
   */
  private class InternalDebugListener extends CDebugPerspectiveModelListenerAdapter {
    @Override
    public void changedActiveDebugger(final IDebugger oldDebugger, final IDebugger newDebugger) {
      if (newDebugger != null) {
        final TargetInformation targetInformation =
            newDebugger.getProcessManager().getTargetInformation();

        if (targetInformation != null) {
          m_dataProvider.setRegisterDescription(targetInformation.getRegisters());
        }
      }

      synchronizeDebugger(oldDebugger, newDebugger);
    }
  }

  /**
   * Keeps track of relevant changes in the process of the active debugger.
   */
  private class InternalProcessListener extends ProcessManagerListenerAdapter {
    @Override
    public void changedActiveThread(final TargetProcessThread oldThread, final TargetProcessThread newThread) {
      synchronizeThread(oldThread, newThread);
    }

    @Override
    public void changedTargetInformation(final TargetInformation information) {
      m_dataProvider.setRegisterDescription(information.getRegisters());
    }

    @Override
    public void detached() {
      m_dataProvider.setRegisterDescription(new FilledList<RegisterDescription>());
      m_dataProvider.setRegisterInformation(new FilledList<RegisterValue>());

      updateGui();
    }
  }

  /**
   * This listener is always listening on the thread that is currently active in the GUI. When the
   * register values of this thread change, the data provider is updated to display the new register
   * values in the GUI.
   */
  private class InternalThreadListener extends ThreadListenerAdapter {
    @Override
    public void registersChanged(final TargetProcessThread thread) {
      m_dataProvider.setRegisterInformation(thread.getRegisterValues());
    }

    @Override
    public void stateChanged(final TargetProcessThread thread) {
      updateGui();
    }
  }
}
