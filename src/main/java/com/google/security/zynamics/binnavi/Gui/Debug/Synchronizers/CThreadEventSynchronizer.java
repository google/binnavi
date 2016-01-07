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
package com.google.security.zynamics.binnavi.Gui.Debug.Synchronizers;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.ZyGraph.Painters.CDebuggerPainter;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ProcessManagerListenerAdapter;
import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ThreadListenerAdapter;
import com.google.security.zynamics.binnavi.debug.models.processmanager.interfaces.ProcessManagerListener;
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;

/**
 * Synchronizes the GUI with events in a single thread.
 */
public final class CThreadEventSynchronizer {
  /**
   * Debugger who is synchronized with the GUI.
   */
  private final IDebugger m_debugger;

  /**
   * The graph that is synchronized with the target process.
   */
  private final ZyGraph m_graph;

  /**
   * Keeps track of threads and updates the GUI on relevant events.
   */
  private final InternalThreadListener m_internalThreadListener = new InternalThreadListener();

  /**
   * Synchronizes the GUI with changes in the process.
   */
  private final ProcessManagerListener m_processManagerListener =
      new ProcessManagerListenerAdapter() {
        @Override
        public void changedActiveThread(final TargetProcessThread oldThread, final TargetProcessThread newThread) {
          synchronizeThreads(oldThread, newThread);
        }
      };

  /**
   * Creates a new thread synchronization object.
   *
   * @param debugger Debugger who is synchronized with the GUI.
   * @param graph The graph that is synchronized with the target process.
   */
  public CThreadEventSynchronizer(final IDebugger debugger, final ZyGraph graph) {
    Preconditions.checkNotNull(debugger, "IE01514: Debugger argument can not be null");

    Preconditions.checkNotNull(graph, "IE01515: Graph argument can not be null");

    m_debugger = debugger;
    m_graph = graph;

    // m_threadListener = new CThreadSynchronizer(debugger.getProcessManager(),
    // m_internalThreadListener);

    synchronizeThreads(null, debugger.getProcessManager().getActiveThread());

    debugger.getProcessManager().addListener(m_processManagerListener);
  }

  /**
   * Paints the program counter of a given thread into the graph.
   *
   * @param thread The graph whose PC is painted into the graph.
   */
  private void paintProgramCounter(final TargetProcessThread thread) {
    if (thread == null) {
      CDebuggerPainter.clearDebuggerHighlighting(m_graph);
    } else {
      final RelocatedAddress address = thread.getCurrentAddress();

      if (address == null) {
        return;
      }
      CDebuggerPainter.updateDebuggerHighlighting(
          m_graph, m_debugger.memoryToFile(address), m_debugger.getModule(address));
    }
  }

  /**
   * Keeps listeners on the active thread.
   *
   * @param oldThread The previously active thread.
   * @param newThread The newly active thread.
   */
  private void synchronizeThreads(final TargetProcessThread oldThread, final TargetProcessThread newThread) {
    if (oldThread != null) {
      oldThread.removeListener(m_internalThreadListener);
    }

    if (newThread == null) {
      CDebuggerPainter.clearDebuggerHighlighting(m_graph);
    } else {
      newThread.addListener(m_internalThreadListener);
    }
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    // m_threadListener.dispose();

    synchronizeThreads(m_debugger.getProcessManager().getActiveThread(), null);
  }

  /**
   * Keeps track of threads and updates the GUI on relevant events.
   */
  private class InternalThreadListener extends ThreadListenerAdapter {
    @Override
    public void instructionPointerChanged(
        final TargetProcessThread thread, final RelocatedAddress oldAddress) {
      paintProgramCounter(thread);
    }
  }
}
