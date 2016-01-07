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
import com.google.security.zynamics.binnavi.disassembly.CGroupNode;
import com.google.security.zynamics.binnavi.yfileswrap.Gui.GraphWindows.Implementations.CGraphDialogs;

/**
 * Action class that can be used to delete an edge from a graph.
 */
public final class CEditGroupCommentAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -7122593725486189118L;

  /**
   * Parent class used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * Group node whose comment is edited.
   */
  private final CGroupNode m_node;

  /**
   * Creates a new edit group comment action.
   *
   * @param parent Parent class used for dialogs.
   * @param node Group node whose comment is edited.
   */
  public CEditGroupCommentAction(final JFrame parent, final CGroupNode node) {
    super("Edit Group Comment");

    Preconditions.checkNotNull(parent, "IE00939: Parent argument can't be null");

    Preconditions.checkNotNull(node, "IE00940: Node argument can't be null");

    m_parent = parent;
    m_node = node;

  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CGraphDialogs.showGroupNodeCommentDialog(m_parent, m_node);
  }
}
