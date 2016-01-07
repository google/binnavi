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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.Center;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.Debug.Synchronizers.CBreakpointSynchronizer;
import com.google.security.zynamics.binnavi.Gui.Debug.Synchronizers.CNodeSynchronizer;
import com.google.security.zynamics.binnavi.Gui.Debug.Synchronizers.CThreadEventSynchronizer;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModelListenerAdapter;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.IDebugPerspectiveModelListener;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;

/**
 * Synchronizes a graph with the debug perspective of a graph window.
 */
public final class CGraphSynchronizer {
  /**
   * The graph to synchronize.
   */
  private final ZyGraph m_graph;

  /**
   * The debug perspective to synchronize.
   */
  private final CDebugPerspectiveModel m_debugPerspective;

  /**
   * Updates the graph on changes in the active debugger.
   */
  private final IDebugPerspectiveModelListener m_debugListener = new InternalDebugListener();

  /**
   * Synchronizes breakpoints with the visible graph.
   */
  private CBreakpointSynchronizer m_breakpointSynchronizer;

  /**
   * Synchronizes thread events with the visible graph.
   */
  private CThreadEventSynchronizer m_threadEventSynchronizer;

  /**
   * Creates a new synchronizer object.
   *
   * @param graph The graph to synchronize.
   * @param debugPerspective The debug perspective to synchronize.
   */
  public CGraphSynchronizer(final ZyGraph graph, final CDebugPerspectiveModel debugPerspective) {
    m_graph = Preconditions.checkNotNull(graph, "IE02330: graph argument can not be null");
    m_debugPerspective = Preconditions.checkNotNull(
        debugPerspective, "IE02331: debugPerspective argument can not be null");

    debugPerspective.addListener(m_debugListener);
    synchronizeDebugger(null, debugPerspective.getCurrentSelectedDebugger());
  }

  /**
   * Keeps the synchronizer listening on the active debugger.
   *
   * @param oldDebugger The previously active debugger.
   * @param newDebugger The new active debugger.
   */
  private void synchronizeDebugger(final IDebugger oldDebugger, final IDebugger newDebugger) {
    if (oldDebugger != null) {
      m_breakpointSynchronizer.dispose();
      m_threadEventSynchronizer.dispose();
    }

    if (newDebugger != null) {
      m_breakpointSynchronizer =
          new CBreakpointSynchronizer(newDebugger.getBreakpointManager(), m_graph);
      m_threadEventSynchronizer = new CThreadEventSynchronizer(newDebugger, m_graph);
      new CNodeSynchronizer(newDebugger.getBreakpointManager(), m_graph, m_debugPerspective);
    }
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    m_debugPerspective.removeListener(m_debugListener);

    synchronizeDebugger(m_debugPerspective.getCurrentSelectedDebugger(), null);
  }

  /**
   * Updates the graph on changes in the active debugger.
   */
  private class InternalDebugListener extends CDebugPerspectiveModelListenerAdapter {
    @Override
    public void changedActiveDebugger(final IDebugger oldDebugger, final IDebugger newDebugger) {
      synchronizeDebugger(oldDebugger, newDebugger);
    }
  }
}
