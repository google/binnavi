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
package com.google.security.zynamics.binnavi.Gui.Debug.EventLists.Implementations;

import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;
import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceEvent;
import com.google.security.zynamics.binnavi.disassembly.CCodeNodeHelpers;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.GraphHelpers;
import com.google.security.zynamics.zylib.types.common.ICollectionFilter;

import java.util.List;



/**
 * Contains code for finding the nodes of a graph that were hit by a trace.
 */
public final class CTraceNodeFinder {
  /**
   * You are not supposed to instantiate this class.
   */
  private CTraceNodeFinder() {
  }

  /**
   * Checks whether a code node was hit by a trace.
   *
   * @param node The node to check.
   * @param eventList Provides the events of the trace.
   *
   * @return True, if the node was hit by the trace. False, otherwise.
   */
  private static boolean isEventNode(final INaviCodeNode node, final TraceList eventList) {
    for (final ITraceEvent traceEvent : eventList) {
      final BreakpointAddress eventAddress = traceEvent.getOffset();

      try {
        if (node.getParentFunction().getModule() == eventAddress.getModule()
            && CCodeNodeHelpers.containsAddress(node, eventAddress.getAddress().getAddress())) {
          return true;
        }
      } catch (final MaybeNullException e) {
      }
    }

    return false;
  }

  /**
   * Checks whether a function node was hit by a trace.
   *
   * @param node The node to check.
   * @param eventList Provides the events of the trace.
   *
   * @return True, if the node was hit by the trace. False, otherwise.
   */
  private static boolean isEventNode(final INaviFunctionNode node, final TraceList eventList) {
    for (final ITraceEvent traceEvent : eventList) {
      if (traceEvent.getOffset().getModule() == node.getFunction().getModule() && node.getFunction()
          .getAddress().equals(traceEvent.getOffset().getAddress().getAddress())) {
        return true;
      }
    }

    return false;
  }

  /**
   * Returns the nodes of a graph that were hit by the events in a given trace list.
   *
   * @param graph The graph whose nodes are returned.
   * @param eventList Provides the events to search for.
   *
   * @return The nodes of the graph that were hit by the events in the list.
   */
  public static List<NaviNode> getTraceNodes(final ZyGraph graph, final TraceList eventList) {
    return GraphHelpers.filter(graph, new ICollectionFilter<NaviNode>() {
      @Override
      public boolean qualifies(final NaviNode item) {
        return item.getRawNode() instanceof INaviCodeNode && isEventNode(
            (INaviCodeNode) item.getRawNode(), eventList)
            || item.getRawNode() instanceof INaviFunctionNode
            && isEventNode((INaviFunctionNode) item.getRawNode(), eventList);
      }
    });
  }

}
