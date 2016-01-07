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
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;

/**
 * Action class used to select all nodes of a graph which have the same parent function as a given
 * graph.
 */
public final class CActionSelectSameParentFunction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 6300870557894835360L;

  /**
   * Graph in which the selection event happens.
   */
  private final ZyGraph m_graph;

  /**
   * Nodes with this parent function are selected.
   */
  private final INaviFunction m_function;

  /**
   * Creates a new action object.
   *
   * @param graph Graph in which the selection event happens.
   * @param function Nodes with this parent function are selected.
   */
  public CActionSelectSameParentFunction(final ZyGraph graph, final INaviFunction function) {
    super("Select nodes from the same function");

    m_graph = Preconditions.checkNotNull(graph, "IE00936: Graph argument can't be null");
    m_function = Preconditions.checkNotNull(function, "IE00937: Function argument can't be null");
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CGraphSelecter.selectNodesWithParentFunction(m_graph, m_function);
  }
}
