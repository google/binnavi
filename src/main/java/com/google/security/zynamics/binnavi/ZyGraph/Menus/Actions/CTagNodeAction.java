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
import javax.swing.JTree;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.Implementations.CTaggingFunctions;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;

/**
 * Action used for tagging a node with the currently selected node of a tags tree.
 */
public final class CTagNodeAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -3099619241100541458L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * Tags tree that provides the tag used for tagging.
   */
  private final JTree m_tagsTree;

  /**
   * Node to be tagged.
   */
  private final NaviNode m_node;

  /**
   * Creates a new action object.
   *
   * @param parent Parent window used for dialogs.
   * @param tagsTree Tags tree that provides the tag used for tagging.
   * @param node Node to be tagged.
   */
  public CTagNodeAction(final JFrame parent, final JTree tagsTree, final NaviNode node) {
    super("Tag Node");

    Preconditions.checkNotNull(parent, "IE02165: Parent argument can not be null");

    Preconditions.checkNotNull(tagsTree, "IE02166: Tags tree argument can not be null");

    Preconditions.checkNotNull(node, "IE02167: Node argument can not be null");

    m_parent = parent;
    m_tagsTree = tagsTree;
    m_node = node;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CTaggingFunctions.tagNode(m_parent, m_tagsTree, m_node);
  }
}
