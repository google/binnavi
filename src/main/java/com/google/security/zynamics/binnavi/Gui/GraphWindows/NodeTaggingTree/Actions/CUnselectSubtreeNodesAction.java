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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.Actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.Implementations.CTagSelectionFunctions;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.types.trees.ITreeNode;

/**
 * Action that is used to unselect all selected nodes of a graph which are either tagged with a
 * given tag or any of the children of the given tag.
 */
public final class CUnselectSubtreeNodesAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -8290018810177117414L;

  /**
   * The graph whose nodes are unselected.
   */
  private final ZyGraph m_graph;

  /**
   * The root tag of the child hierarchy to unselect.
   */
  private final ITreeNode<CTag> m_tag;

  /**
   * Creates a new action object.
   *
   * @param graph The graph whose nodes are unselected.
   * @param tag The root tag of the child hierarchy to unselect.
   */
  public CUnselectSubtreeNodesAction(final ZyGraph graph, final ITreeNode<CTag> tag) {
    super("Unselect Subtree Nodes");

    m_graph = Preconditions.checkNotNull(graph, "IE02323: Graph argument can not be null");
    m_tag = Preconditions.checkNotNull(tag, "IE02324: Tag can't be null");
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CTagSelectionFunctions.unselectSubtreeNodes(m_graph, m_tag);
  }
}
