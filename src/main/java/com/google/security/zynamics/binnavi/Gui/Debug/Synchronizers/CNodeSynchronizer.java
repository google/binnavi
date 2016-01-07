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
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;
import com.google.security.zynamics.binnavi.ZyGraph.Painters.CBreakpointPainter;
import com.google.security.zynamics.binnavi.ZyGraph.Painters.CDebuggerPainter;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointManager;
import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.INodeCallback;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.IZyNodeRealizerListener;
import com.google.security.zynamics.zylib.types.common.IterationMode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.IZyNodeRealizer;

/**
 * Keeps track of graph nodes and updates them with information from the debugger if something
 * relevant happens.
 */
public final class CNodeSynchronizer {
  /**
   * Breakpoint manager where the breakpoints are set.
   */
  private final BreakpointManager m_manager;

  /**
   * The graph to update on relevant debugger events.
   */
  private final ZyGraph m_graph;

  /**
   * Makes sure to recreate the node after the node realizer was regenerated.
   */
  private final InternalRealizerListener m_realizerListener = new InternalRealizerListener();

  /**
   * Describes the debug GUI perspective where the graph is shown.
   */
  private final CDebugPerspectiveModel m_debugPerspective;

  /**
   * Creates a new node synchronizer object.
   *
   * @param manager Breakpoint manager where the breakpoints are set.
   * @param graph The graph to update on relevant debugger events.
   * @param debugPerspective Describes the debug GUI perspective where the graph is shown.
   */
  public CNodeSynchronizer(final BreakpointManager manager, final ZyGraph graph,
      final CDebugPerspectiveModel debugPerspective) {
    m_graph = Preconditions.checkNotNull(graph, "IE01511: Graph argument can not be null");
    m_manager = Preconditions.checkNotNull(manager, "IE01512: Manager argument can not be null");
    m_debugPerspective = Preconditions.checkNotNull(
        debugPerspective, "IE02296: debugPerspective argument can not be null");
    graph.addNodeModifier(m_realizerListener);
  }

  /**
   * Removes a node modifier from all the nodes.
   *
   * @param modifier The modifier to remove from the nodes.
   */
  private void removeNodeModifier(final IZyNodeRealizerListener<NaviNode> modifier) {
    Preconditions.checkNotNull(modifier, "IE01513: Modifier argument can not be null");

    m_graph.iterate(new INodeCallback<NaviNode>() {
      @Override
      public IterationMode next(final NaviNode node) {
        node.removeNodeModifier(modifier);

        return IterationMode.CONTINUE;
      }
    });
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    removeNodeModifier(m_realizerListener);
  }

  /**
   * Makes sure to recreate the node after the node realizer was regenerated.
   */
  private class InternalRealizerListener implements IZyNodeRealizerListener<NaviNode> {
    @Override
    public void changedLocation(final IZyNodeRealizer realizer, final double x, final double y) {
      // Content does not depend on location
    }

    @Override
    public void changedSelection(final IZyNodeRealizer realizer) {
      // Content does not depend on selection state
    }

    @Override
    public void changedSize(final IZyNodeRealizer realizer, final double x, final double y) {
      // Content does not depend on node size
    }

    @Override
    public void changedVisibility(final IZyNodeRealizer realizer) {
      // Content does not depend on visibility state
    }

    @Override
    public void regenerated(final IZyNodeRealizer realizer) {
      // When the realizer of a node was regenerated, all the information
      // from the debugger disappeared from the node. That means we have
      // to re-add them at this point.

      final IDebugger activeDebugger = m_debugPerspective.getCurrentSelectedDebugger();
      final TargetProcessThread currentThread =
          activeDebugger == null ? null : activeDebugger.getProcessManager().getActiveThread();

      if (currentThread != null) {
        final UnrelocatedAddress fileAddress = m_debugPerspective.getCurrentSelectedDebugger()
            .memoryToFile(currentThread.getCurrentAddress());

        CDebuggerPainter.updateSingleNodeDebuggerHighlighting(
            m_graph, fileAddress, (NaviNode) realizer.getUserData().getNode());
      }

      final INaviViewNode rawNode = (INaviViewNode) realizer.getUserData().getNode().getRawNode();

      if (rawNode instanceof INaviCodeNode) {
        CBreakpointPainter.paintBreakpoints(m_manager, (NaviNode) realizer.getUserData().getNode(),
            (INaviCodeNode) rawNode);
      } else if (rawNode instanceof INaviFunctionNode) {
        CBreakpointPainter.paintBreakpoints(m_manager, (NaviNode) realizer.getUserData().getNode(),
            (INaviFunctionNode) rawNode);
      }
    }
  }
}
