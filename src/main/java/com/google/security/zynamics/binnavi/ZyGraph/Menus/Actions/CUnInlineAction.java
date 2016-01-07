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
import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.algorithms.CUnInliner;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;

/**
 * Action class used for uninlining a function.
 */
public class CUnInlineAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -5262453620487375988L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * Graph where the uninline operation takes place.
   */
  private final ZyGraph m_graph;

  /**
   * Node that belongs to the function to be uninlined.
   */
  private final INaviCodeNode m_node;

  /**
   * Creates a new action object.
   *
   * @param parent Parent window used for dialogs.
   * @param graph Graph where the uninline operation takes place.
   * @param node Node that belongs to the function to be uninlined.
   */
  public CUnInlineAction(final JFrame parent, final ZyGraph graph, final INaviCodeNode node) {
    super(String.format("Uninline '%s'", getParentFunction(node)));

    m_parent = Preconditions.checkNotNull(parent, "IE02365: parent argument can not be null");
    m_graph = Preconditions.checkNotNull(graph, "IE02366: graph argument can not be null");
    m_node = Preconditions.checkNotNull(node, "IE02367: node argument can not be null");
  }

  /**
   * Returns the name of the parent function of the given node.
   *
   * @param node The node whose parent function is returned.
   *
   * @return The parent function name of the node.
   */
  private static String getParentFunction(final INaviCodeNode node) {
    try {
      return node.getParentFunction().getName();
    } catch (final MaybeNullException exception) {
      // This should never happen

      return "";
    }
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CUnInliner.unInline(m_parent, m_graph, m_node);
  }
}
