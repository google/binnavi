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
import com.google.security.zynamics.binnavi.ZyGraph.Implementations.CProximityFunctions;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.proximity.ZyProximityNode;

/**
 * Action class used to unhide nodes hidden by a proxmity node and all of their children.
 */
public final class CUnhideChildrenAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 6853096864373572146L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * Graph where the operation takes place.
   */
  private final ZyGraph m_graph;

  /**
   * Proximity node to remove.
   */
  private final ZyProximityNode<INaviViewNode> m_node;

  /**
   * Creates a new action object.
   *
   * @param parent Parent window used for dialogs.
   * @param graph Graph where the operation takes place.
   * @param node Proximity node to remove.
   */
  public CUnhideChildrenAction(
      final JFrame parent, final ZyGraph graph, final ZyProximityNode<INaviViewNode> node) {
    super("Unhide All Children");

    Preconditions.checkNotNull(parent, "IE00950: Parent argument can not be null");

    Preconditions.checkNotNull(graph, "IE00951: Graph argument can't be null");

    Preconditions.checkNotNull(node, "IE00952: Node argument can't be null");

    m_parent = parent;
    m_graph = graph;
    m_node = node;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    new CProximityFunctions().unhideChildren(m_parent, m_graph, m_node);
  }
}
