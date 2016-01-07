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


/**
 * Action class used for collapsing group nodes.
 */
public final class CActionCollapseNode extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 4155542247255806292L;

  /**
   * The group node to collapse.
   */
  private final CGroupNode m_node;

  /**
   * Creates a new action object.
   *
   * @param node The group node to collapse.
   */
  public CActionCollapseNode(final CGroupNode node) {
    super("Collapse Group");

    m_node = node;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    m_node.setCollapsed(true);
  }
}
