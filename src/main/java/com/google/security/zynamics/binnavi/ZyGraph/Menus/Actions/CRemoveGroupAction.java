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

import com.google.security.zynamics.binnavi.disassembly.CGroupNode;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;


/**
 * Action class used for removing group nodes from a graph.
 */
public final class CRemoveGroupAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 3268013685079704040L;

  /**
   * View from which the group node is removed.
   */
  private final INaviView m_graph;

  /**
   * Group node to be removed from the graph.
   */
  private final CGroupNode m_node;

  /**
   * Create a new action object.
   *
   * @param view View from which the group node is removed.
   * @param node Group node to be removed from the graph.
   */
  public CRemoveGroupAction(final INaviView view, final CGroupNode node) {
    super("Remove Group");

    m_graph = view;
    m_node = node;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    m_graph.getContent().deleteNode(m_node);
  }
}
