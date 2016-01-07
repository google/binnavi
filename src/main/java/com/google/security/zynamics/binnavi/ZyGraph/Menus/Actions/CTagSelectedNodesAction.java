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
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.CTagsTree;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.Implementations.CTaggingFunctions;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;

/**
 * Action class used for tagging all selected nodes of a graph with the selected tag of a tags tree.
 */
public final class CTagSelectedNodesAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -1437680223895484541L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * Provides the tag for tagging.
   */
  private final CTagsTree m_tagsTree;

  /**
   * Graph whose selected nodes are tagged.
   */
  private final ZyGraph m_graph;

  /**
   * Creates a new action object.
   *
   * @param parent Parent window used for dialogs.
   * @param tagsTree Provides the tag for tagging.
   * @param graph Graph whose selected nodes are tagged.
   */
  public CTagSelectedNodesAction(
      final JFrame parent, final CTagsTree tagsTree, final ZyGraph graph) {
    super("Tag selected Nodes");

    Preconditions.checkNotNull(parent, "IE02168: Parent argument can not be null");

    Preconditions.checkNotNull(tagsTree, "IE02169: Tree argument can not be null");

    Preconditions.checkNotNull(graph, "IE02170: Graph argument can not be null");

    m_parent = parent;
    m_tagsTree = tagsTree;
    m_graph = graph;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CTaggingFunctions.tagSelectedNodes(m_parent, m_tagsTree, m_graph);
  }
}
