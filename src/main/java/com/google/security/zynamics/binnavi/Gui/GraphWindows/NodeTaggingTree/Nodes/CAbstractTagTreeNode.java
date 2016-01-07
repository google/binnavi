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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.Nodes;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.gui.jtree.IconNode;

/**
 * Abstract base class for all nodes of the node tagging tree.
 */
public abstract class CAbstractTagTreeNode extends IconNode implements ITagTreeNode {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -4461967381178120275L;

  /**
   * The graph whose nodes are tagged.
   */
  private final ZyGraph m_graph;

  /**
   * Creates a new node object.
   *
   * @param uniqueNodeId Unique identifier of the node.
   * @param graph The graph whose nodes are tagged.
   */
  public CAbstractTagTreeNode(final Integer uniqueNodeId, final ZyGraph graph) {
    super(uniqueNodeId); // must be unique in order to restore the tree's exact folding state after
                         // nodes has been inserted or deleted

    m_graph = Preconditions.checkNotNull(graph, "IE01795: Graph can not be null.");
  }

  /**
   * Disposes all children.
   */
  protected void deleteChildren() {
    for (int i = 0; i < getChildCount(); i++) {
      final ITagTreeNode child = (ITagTreeNode) getChildAt(i);

      child.dispose();
    }

    removeAllChildren();
  }

  /**
   * Returns the graph whose nodes are tagged.
   *
   * @return The graph whose nodes are tagged.
   */
  protected ZyGraph getGraph() {
    return m_graph;
  }
}
