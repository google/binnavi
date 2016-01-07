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
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations.CNodeDeleter;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;

/**
 * Action class that can be used to delete a node from a graph.
 */
public final class CActionDeleteNode extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -3547430393412378672L;

  /**
   * View from which the node is deleted.
   */
  private final INaviView m_view;

  /**
   * The node to be deleted from the view.
   */
  private final INaviViewNode m_node;

  /**
   * Creates a new action object.
   *
   * @param view View from which the node is deleted.
   * @param node The node to be deleted from the view.
   */
  public CActionDeleteNode(final INaviView view, final INaviViewNode node) {
    super("Delete Node");

    m_view = Preconditions.checkNotNull(view, "IE00930: View argument can't be null");
    m_node = Preconditions.checkNotNull(node, "IE00931: Node argument can't be null");
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CNodeDeleter.deleteNode(m_view, m_node);
  }
}
