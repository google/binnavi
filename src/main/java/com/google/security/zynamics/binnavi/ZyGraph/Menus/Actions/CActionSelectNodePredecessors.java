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
package com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations.CGraphSelecter;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;

/**
 * Action object used for selecting the predecessors of a node.
 */
public final class CActionSelectNodePredecessors extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 2179870546585372561L;

  /**
   * Graph in which the selection event happens.
   */
  private final ZyGraph m_graph;

  /**
   * Node whose predecessors are selected.
   */
  private final NaviNode m_node;

  /**
   * Creates a new action object.
   *
   * @param graph Graph in which the selection event happens.
   * @param node Node whose predecessors are selected.
   */
  public CActionSelectNodePredecessors(final ZyGraph graph, final NaviNode node) {
    super("Select predecessors");

    m_graph = Preconditions.checkNotNull(graph, "IE00932: Graph argument can't be null");
    m_node = Preconditions.checkNotNull(node, "IE00933: Node argument can't be null");
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CGraphSelecter.selectPredecessors(m_graph, m_node);
  }
}
