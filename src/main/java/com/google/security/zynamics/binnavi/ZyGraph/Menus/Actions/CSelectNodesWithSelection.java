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
import com.google.security.zynamics.binnavi.ZyGraph.Implementations.CGraphFunctions;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;

/**
 * Action class that can be used to select nodes with a given content.
 */
public final class CSelectNodesWithSelection extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 8767876653212720262L;

  /**
   * Graph where the selection happens.
   */
  private final ZyGraph m_graph;

  /**
   * Node that provides the selected content.
   */
  private final NaviNode m_node;

  /**
   * Creates a new action object.
   *
   * @param graph Graph where the selection happens.
   * @param node Node that provides the selected content.
   */
  public CSelectNodesWithSelection(final ZyGraph graph, final NaviNode node) {
    super("Select nodes with selection");

    Preconditions.checkNotNull(graph, "IE00944: Graph argument can not be null");

    Preconditions.checkNotNull(node, "IE00945: Node argument can not be null");

    m_graph = graph;
    m_node = node;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    final String searchString = m_node.getRealizer().getNodeContent().getSelectedText();

    CGraphFunctions.selectNodesWithString(m_graph, searchString);
  }
}
