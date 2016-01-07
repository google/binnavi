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

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.ZyGraph.Painters.CBreakpointPainter;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.Breakpoint;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointManager;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointManagerListenerAdapter;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointStatus;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointType;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;

/**
 * Keeps track of breakpoints and updates the debugger GUI on relevant breakpoint events.
 */
public final class CBreakpointSynchronizer {
  /**
   * Breakpoint manager where the breakpoints are set.
   */
  private final BreakpointManager m_manager;

  /**
   * The graph to be updated.
   */
  private final ZyGraph m_graph;

  /**
   * Keeps track of available breakpoints and updates the GUI on relevant events.
   */
  private final InternalBreakpointManagerListener m_breakpointManagerListener =
      new InternalBreakpointManagerListener();

  /**
   * Creates a new breakpoint synchronizer object.
   *
   * @param manager Breakpoint manager where the breakpoints are set.
   * @param graph The graph to be updated.
   */
  public CBreakpointSynchronizer(final BreakpointManager manager, final ZyGraph graph) {
    m_manager = Preconditions.checkNotNull(manager, "IE01509: Manager argument can not be null");
    m_graph = Preconditions.checkNotNull(graph, "IE01510: Graph argument can not be null");

    manager.addListener(m_breakpointManagerListener);

    CBreakpointPainter.paintBreakpoints(manager, m_graph);
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    m_manager.removeListener(m_breakpointManagerListener);
  }

  /**
   * Keeps track of available breakpoints and updates the GUI on relevant events.
   */
  private class InternalBreakpointManagerListener extends BreakpointManagerListenerAdapter {
    @Override
    public void breakpointsAdded(final List<Breakpoint> breakpoints) {
      Preconditions.checkNotNull(breakpoints, "IE00735: breakpoints argument can not be null");

      for (final Breakpoint breakpoint : breakpoints) {
        if (breakpoint.getType() == BreakpointType.REGULAR) {
          // This code hides a lot of complexity which can be reduced if one thinks about it.
          CBreakpointPainter.paintBreakpoints(m_manager, m_graph, breakpoint.getAddress());
        }
      }
    }

    @Override
    public void breakpointsRemoved(final Set<Breakpoint> breakpoints) {
      Preconditions.checkNotNull(breakpoints, "IE00736: breakpoints argument can not be null");

      for (final Breakpoint breakpoint : breakpoints) {
        if (breakpoint.getType() == BreakpointType.REGULAR) {
          // This code hides a lot of complexity which can be reduced if one thinks about it.
          CBreakpointPainter.paintBreakpoints(m_manager, m_graph, breakpoint.getAddress());
        }
      }
    }

    @Override
    public void breakpointsStatusChanged(
        final Map<Breakpoint, BreakpointStatus> breakpointsToOldStatus,
        final BreakpointStatus newStatus) {
      Preconditions.checkNotNull(
          breakpointsToOldStatus, "IE01002: breakpoints argument can not be null");
      Preconditions.checkNotNull(newStatus, "IE01006: newStatus argument can not be null");

      for (final Breakpoint breakpoint : breakpointsToOldStatus.keySet()) {
        if (breakpoint.getType() == BreakpointType.REGULAR) {
          CBreakpointPainter.paintBreakpoints(m_manager, m_graph, breakpoint.getAddress());
        }
      }
    }
  }
}
