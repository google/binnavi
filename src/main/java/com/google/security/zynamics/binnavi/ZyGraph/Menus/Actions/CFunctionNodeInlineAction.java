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
import javax.swing.JFrame;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations.CGraphInliner;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;

/**
 * Action class used for inlining a function node.
 */
public final class CFunctionNodeInlineAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -2847174680935808654L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * Graph where the inlining operation happens.
   */
  private final ZyGraph m_graph;

  /**
   * Function node that is inlined.
   */
  private final INaviFunctionNode m_node;

  /**
   * Creates a new inline function action.
   *
   * @param parent Parent window used for dialogs.
   * @param graph Graph where the inlining operation happens.
   * @param node Function node that is inlined.
   */
  public CFunctionNodeInlineAction(
      final JFrame parent, final ZyGraph graph, final INaviFunctionNode node) {
    super(String.format("Inline function %s", node.getFunction().getName()));

    Preconditions.checkNotNull(parent, "IE02158: Parent argument can not be null");
    Preconditions.checkNotNull(graph, "IE02159: Graph argument can not be null");
    Preconditions.checkNotNull(node, "IE02288: Node argument can not be null");

    m_parent = parent;
    m_graph = graph;
    m_node = node;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CGraphInliner.inlineFunction(m_parent, m_graph, m_node);
  }
}
