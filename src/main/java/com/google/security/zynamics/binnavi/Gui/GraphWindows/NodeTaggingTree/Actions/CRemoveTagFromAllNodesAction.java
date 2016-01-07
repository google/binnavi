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

/**
 * Action class used to remove a certain tag from all nodes of a graph.
 */
public final class CRemoveTagFromAllNodesAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 3076088346942638018L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * Graph whose nodes are untagged.
   */
  private final ZyGraph m_graph;

  /**
   * The tag to remove from all the nodes of the graph.
   */
  private final CTag m_tag;

  /**
   * Creates a new action object.
   *
   * @param parent Parent window used for dialogs.
   * @param graph Graph whose nodes are untagged.
   * @param tag The tag to remove from all the nodes of the graph.
   */
  public CRemoveTagFromAllNodesAction(final JFrame parent, final ZyGraph graph, final CTag tag) {
    super("Remove Tag from all Nodes");

    Preconditions.checkNotNull(parent, "IE01254: Parent argument can not be null");

    Preconditions.checkNotNull(graph, "IE01255: Graph argument can not be null");

    Preconditions.checkNotNull(tag, "IE01787: Tag can't be null");

    m_parent = parent;
    m_graph = graph;
    m_tag = tag;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CTagFunctions.removeTagFromAllNodes(m_parent, m_graph, m_tag);
  }
}
