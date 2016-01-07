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
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations.CGraphGrouper;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;

/**
 * Action class that can be used to remove a node from its parent group.
 */
public final class CRemoveFromGroupAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -2302023300149848549L;

  /**
   * Node to be removed from its parent group.
   */
  private final NaviNode m_node;

  /**
   * Creates a new action object.
   *
   * @param node Node to be removed from its parent group.
   */
  public CRemoveFromGroupAction(final NaviNode node) {
    super("Remove node from group");

    Preconditions.checkNotNull(node, "IE00941: Node argument can't be null");

    m_node = node;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CGraphGrouper.removeFromGroup(m_node);
  }
}
