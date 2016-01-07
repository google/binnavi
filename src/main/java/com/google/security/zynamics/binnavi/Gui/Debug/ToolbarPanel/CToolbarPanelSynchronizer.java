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

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Gui.Debug.ThreadPanel.CThreadPanel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModelListenerAdapter;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.IDebugPerspectiveModelListener;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ProcessManagerListenerAdapter;
import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ThreadListenerAdapter;
import com.google.security.zynamics.binnavi.debug.models.processmanager.interfaces.ProcessManagerListener;
import com.google.security.zynamics.binnavi.debug.models.processmanager.interfaces.ThreadListener;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerOptions;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.TargetInformation;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;
import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceLoggerListener;
import com.google.security.zynamics.zylib.general.ListenerProvider;

/**
 * Synchronizes the debug toolbar with the current process state.
 */
public final class CToolbarPanelSynchronizer {
  /**
   * The toolbar to synchronize.
   */
  private final CDebuggerToolbar m_toolBar;

  /**
   * The thread panel to synchronize.
   */
  private final CThreadPanel m_threadPanel;

  /**
   * The debug perspective that provides information about the GUI.
   */
  private final CDebugPerspectiveModel m_debugPerspectiveModel;

  /**
   * Listens on relevant changes in the GUI.
   */
  private final IDebugPerspectiveModelListener m_debugListener = new InternalDebugListener();

  /**
   * Listens on relevant changes in the process.
   */
  private final ProcessManagerListener m_processListener = new InternalProcessListener();

  /**
   * Listens on relevant changes in threads.
   */
  private final ThreadListener m_threadListener = new InternalThreadListener();

  /**
   * Listens on relevant changes in the active trace.
   */
  private final ITraceLoggerListener m_traceListener = new InternalTraceListener();

  /**
   * Listeners that are notified about changes in traces.
   */
  private final ListenerProvider<IToolbarPanelSynchronizerListener> m_listeners =
      new ListenerProvider<IToolbarPanelSynchronizerListener>();

  /**
   * Creates a new toolbar synchronizer.
   *
   * @param toolBar The toolbar to synchronize.
   * @param threadPanel The thread panel to synchronize.
   * @param debugPerspectiveModel The debug perspective that provides information about the GUI.
   */
  public CToolbarPanelSynchronizer(final CDebuggerToolbar toolBar, final CThreadPanel threadPanel,
      final CDebugPerspectiveModel debugPerspectiveModel) {
    m_toolBar = Preconditions.checkNotNull(toolBar, "IE01524: Toolbar argument can not be null");
    m_threadPanel =
        Preconditions.checkNotNull(threadPanel, "IE01525: Thread panel argument can not be null");
    m_debugPerspectiveModel = Preconditions.checkNotNull(
        debugPerspectiveModel, "IE01526: Debug perspective model argument can not be null");
    final IDebugger activeDebugger = m_debugPerspectiveModel.getCurrentSelectedDebugger();
    final TargetProcessThread activeThread =
        activeDebugger == null ? null : activeDebugger.getProcessManager().getActiveThread();
    synchronizeDebugger(null, m_debugPerspectiveModel.getCurrentSelectedDebugger());
    synchronizeThread(null, activeThread);

    m_debugPerspectiveModel.addListener(m_debugListener);
  }

  /**
   * Keeps the synchronizer up to date with changing active debuggers.
   *
   * @param oldDebugger Previously active debugger.
   * @param newDebugger Currently active debugger.
   */
  private void synchronizeDebugger(final IDebugger oldDebugger, final IDebugger newDebugger) {
    if (oldDebugger != null) {
      oldDebugger.getProcessManager().removeListener(m_processListener);
      m_debugPerspectiveModel.getTraceLogger(oldDebugger).removeListener(m_traceListener);
      synchronizeThread(oldDebugger.getProcessManager().getActiveThread(), null);
    }
    if (newDebugger != null) {
      newDebugger.getProcessManager().addListener(m_processListener);
      m_debugPerspectiveModel.getTraceLogger(newDebugger).addListener(m_traceListener);
      final TargetInformation targetInformation =
          newDebugger.getProcessManager().getTargetInformation();
      if (targetInformation != null) {
        updateFromDebuggerOptions(targetInformation.getDebuggerOptions());
      }
      synchronizeThread(null, newDebugger.getProcessManager().getActiveThread());
    }
    m_toolBar.updateGui();
  }

  /**
   * Keeps the synchronizer up to date with changing active threads.
   *
   * @param oldThread Previously active thread.
   * @param newThread Currently active thread.
   */
  private void synchronizeThread(final TargetProcessThread oldThread, 
                                 final TargetProcessThread newThread) {
    if (oldThread != null) {
      oldThread.removeListener(m_threadListener);
    }
    if (newThread != null) {
      newThread.addListener(m_threadListener);
    }
    m_toolBar.updateGui();
  }

  /**
   * Updates the panel according to the options information supported by the target debugger.
   *
   * @param options The supported debugger options.
   */
  private void updateFromDebuggerOptions(final DebuggerOptions options) {
    m_toolBar.updateFromDebuggerOptions(options);
    m_threadPanel.setVisible(options.canMultithread());
  }

  /**
   * Adds a listener object that is notified about changes in debug traces.
   *
   * @param listener The listener object to add.
   */
  public void addListener(final IToolbarPanelSynchronizerListener listener) {
    m_listeners.addListener(listener);
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    m_debugPerspectiveModel.removeListener(m_debugListener);
    final IDebugger activeDebugger = m_debugPerspectiveModel.getCurrentSelectedDebugger();
    synchronizeDebugger(activeDebugger, null);
  }

  /**
   * Removes a listener object.
   *
   * @param listener The listener object to remove.
   */
  public void removeListener(final IToolbarPanelSynchronizerListener listener) {
    m_listeners.removeListener(listener);
  }

  /**
   * This listener is responsible for updating the toolbar whenever the selected debugger or the
   * selected thread changes.
   */
  private class InternalDebugListener extends CDebugPerspectiveModelListenerAdapter {
    @Override
    public void changedActiveDebugger(final IDebugger oldDebugger, final IDebugger newDebugger) {
      synchronizeDebugger(oldDebugger, newDebugger);
    }
  }

  /**
   * Responsible for updating the toolbar whenever the debugger options of the selected debugger
   * change.
   */
  private class InternalProcessListener extends ProcessManagerListenerAdapter {
    @Override
    public void changedActiveThread(final TargetProcessThread oldThread, 
                                    final TargetProcessThread newThread) {
      synchronizeThread(oldThread, newThread);
    }

    @Override
    public void changedTargetInformation(final TargetInformation information) {
      updateFromDebuggerOptions(information.getDebuggerOptions());
    }

    @Override
    public void detached() {
      m_toolBar.updateGui();
    }
  }

  /**
   * Listens on relevant changes in threads.
   */
  private class InternalThreadListener extends ThreadListenerAdapter {
    @Override
    public void stateChanged(final TargetProcessThread thread) {
      m_toolBar.updateGui();
    }
  }

  /**
   * Keeps the trace buttons of the toolbar panel up to date on changes in the trace.
   */
  private class InternalTraceListener implements ITraceLoggerListener {
    @Override
    public void addedBreakpoint() {
      m_toolBar.updateGui();
    }

    @Override
    public void finished(final TraceList list) {
      try {
        list.save();
      } catch (final CouldntSaveDataException exception) {
        CUtilityFunctions.logException(exception);

        for (final IToolbarPanelSynchronizerListener listener : m_listeners) {
          try {
            listener.errorSavingTrace(list);
          } catch (final Exception e) {
            CUtilityFunctions.logException(e);
          }
        }
      }
      m_toolBar.updateGui();
    }

    @Override
    public void removedBreakpoint() {
      m_toolBar.updateGui();
    }
  }
}
