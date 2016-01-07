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
import javax.swing.JFrame;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.Implementations.CTagFunctions;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.types.trees.ITreeNode;

/**
 * Action class for removing a tag from the selected nodes of a graph.
 */
public final class CRemoveTagFromSelectedNodesAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 4770699597721911225L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * The graph from which the tag is removed.
   */
  private final ZyGraph m_graph;

  /**
   * The tag to remove from the selected nodes.
   */
  private final ITreeNode<CTag> m_tag;

  /**
   * Creates a new action object.
   *
   * @param parent Parent window used for dialogs.
   * @param graph The graph from which the tag is removed.
   * @param tag The tag to remove from the selected nodes.
   */
  public CRemoveTagFromSelectedNodesAction(
      final JFrame parent, final ZyGraph graph, final ITreeNode<CTag> tag) {
    super("Remove Tag from selected Nodes");

    m_parent = Preconditions.checkNotNull(parent, "IE01217: Parent argument can not be null");
    m_graph = Preconditions.checkNotNull(graph, "IE01218: Graph argument can not be null");
    m_tag = Preconditions.checkNotNull(tag, "IE01782: Tag can't be null.");
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CTagFunctions.removeTagFromSelectedNodes(m_parent, m_graph, m_tag.getObject());
  }
}
