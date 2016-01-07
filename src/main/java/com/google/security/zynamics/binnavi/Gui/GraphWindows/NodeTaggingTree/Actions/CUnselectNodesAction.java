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

/**
 * Unselects all nodes of a graph that are tagged with a given tag.
 */
public final class CUnselectNodesAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 9156801315783760360L;

  /**
   * The graph whose nodes are untagged.
   */
  private final ZyGraph m_graph;

  /**
   * Nodes with this tag are unselected.
   */
  private final CTag m_tag;

  /**
   * Creates a new action object.
   *
   * @param graph The graph whose nodes are untagged.
   * @param tag Nodes with this tag are unselected.
   */
  public CUnselectNodesAction(final ZyGraph graph, final CTag tag) {
    super("Unselect Nodes");

    m_graph = Preconditions.checkNotNull(graph, "IE02321: Graph argument can not be null");
    m_tag = Preconditions.checkNotNull(tag, "IE02322: Tag can't be null");
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CTagSelectionFunctions.unselectNodes(m_graph, m_tag);
  }
}
