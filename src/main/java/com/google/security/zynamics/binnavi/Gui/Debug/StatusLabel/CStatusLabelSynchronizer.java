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
package com.google.security.zynamics.binnavi.Gui.Debug.StatusLabel;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.debug.debugger.BackEndDebuggerProvider;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.DebuggerProviderListener;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.Breakpoint;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointManagerListenerAdapter;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointStatus;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.interfaces.BreakpointManagerListener;
import com.google.security.zynamics.binnavi.debug.models.processmanager.interfaces.ProcessManagerListener;

import java.util.Map;

import javax.swing.JLabel;

/**
 * Synchronizes a debugger status label with a list of debuggers to make sure that the latest debug
 * events of all debuggers are shown.
 */
public final class CStatusLabelSynchronizer {
  /**
   * Provides the debuggers whose events are shown.
   */
  private final BackEndDebuggerProvider m_debuggerProvider;

  /**
   * Listens for relevant events in processes.
   */
  private final ProcessManagerListener m_processListener;

  /**
   * Listens for relevant events in breakpoint managers.
   */
  private final BreakpointManagerListener m_breakpointManagerListener =
      new InternalBreakpointManagerListener();

  /**
   * Listens for relevant events in debugger providers.
   */
  private final DebuggerProviderListener m_debuggerProviderListener =
      new InternalDebuggerProviderListener();

  /**
   * Listens for debug events.
   */
  private final CDebugEventListener m_debugEventListener;

  private final JLabel m_label;

  /**
   * Creates a new status label synchronizer.
   *
   * @param label The label to synchronize.
   * @param debuggerProvider Provides the debuggers to synchronize.
   */
  public CStatusLabelSynchronizer(final JLabel label,
      final BackEndDebuggerProvider debuggerProvider) {
    m_label = Preconditions.checkNotNull(label, "IE01095: Label argument can not be null");
    m_debuggerProvider = Preconditions.checkNotNull(debuggerProvider,
        "IE01096: Debugger provider argument can not be null");

    m_processListener = new CProcessListener(label);
    m_debugEventListener = new CDebugEventListener(label);

    for (final IDebugger debugger : debuggerProvider) {
      addListeners(debugger);
    }

    debuggerProvider.addListener(m_debuggerProviderListener);
  }

  /**
   * Adds all necessary listeners to a single debugger.
   *
   * @param debugger The debugger the listeners are added to.
   */
  private void addListeners(final IDebugger debugger) {
    debugger.getProcessManager().addListener(m_processListener);
    debugger.getBreakpointManager().addListener(m_breakpointManagerListener);
    debugger.addListener(m_debugEventListener);
  }

  /**
   * Removes all previously added listeners from a debugger.
   *
   * @param debugger The debugger the listeners are removed from.
   */
  private void removeListeners(final IDebugger debugger) {
    debugger.getProcessManager().removeListener(m_processListener);
    debugger.getBreakpointManager().removeListener(m_breakpointManagerListener);
    debugger.removeListener(m_debugEventListener);
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    m_debuggerProvider.removeListener(m_debuggerProviderListener);

    for (final IDebugger debugger : m_debuggerProvider) {
      removeListeners(debugger);
    }
  }

  private class InternalBreakpointManagerListener extends BreakpointManagerListenerAdapter {
    @Override
    public void breakpointsStatusChanged(
        final Map<Breakpoint, BreakpointStatus> breakpointToStatus,
        final BreakpointStatus newStatus) {
      for (final Breakpoint breakpoint : breakpointToStatus.keySet()) {
        if (newStatus == BreakpointStatus.BREAKPOINT_HIT) {
          m_label.setText(String.format("Breakpoint hit at address %s",
              breakpoint.getAddress().getAddress().getAddress().toHexString()));
        }
      }
    }
  }

  /**
   * Makes sure that listeners are listening on all existing debuggers.
   */
  private class InternalDebuggerProviderListener implements DebuggerProviderListener {
    @Override
    public void debuggerAdded(final BackEndDebuggerProvider provider, final IDebugger debugger) {
      addListeners(debugger);
    }

    @Override
    public void debuggerRemoved(final BackEndDebuggerProvider provider, final IDebugger debugger) {
      removeListeners(debugger);
    }
  }
}
