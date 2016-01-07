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
package com.google.security.zynamics.binnavi.ZyGraph.Updaters.CodeNodes;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.disassembly.COperandTreeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviOperandTreeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviOperandTreeNodeListener;
import com.google.security.zynamics.binnavi.disassembly.OperandDisplayStyle;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.disassembly.IReference;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.IZyNodeRealizer;

/**
 * Updates the code node on changes to the operands.
 */
public final class COperandUpdater implements INaviOperandTreeNodeListener {
  /**
   * Graph to update on changes.
   */
  private final ZyGraph graph;

  /**
   * Realizer of the node to update.
   */
  private IZyNodeRealizer nodeRealizer;

  /**
   * Creates a new updater object.
   *
   * @param graph Graph to update on changes.
   */
  public COperandUpdater(final ZyGraph graph) {
    this.graph = Preconditions.checkNotNull(graph, "Error: graph argument can not be null.");
  }

  /**
   * Regenerates the content of the node and updates the graph view.
   */
  private void rebuildNode() {
    nodeRealizer.regenerate();
    graph.updateViews();
  }

  @Override
  public void addedReference(
      final INaviOperandTreeNode operandTreeNode, final IReference reference) {
    // References are not shown in code nodes => No rebuild necessary when a reference changes
  }

  @Override
  public void changedDisplayStyle(
      final COperandTreeNode operandTreeNode, final OperandDisplayStyle style) {
    rebuildNode();
  }

  @Override
  public void changedValue(final INaviOperandTreeNode operandTreeNode) {
    rebuildNode();
  }

  @Override
  public void removedReference(
      final INaviOperandTreeNode operandTreeNode, final IReference reference) {
    // References are not shown in code nodes => No rebuild necessary when a reference changes
  }

  /**
   * Sets the realizer of the node to update.
   *
   * @param realizer The realizer of the node to update.
   */
  public void setRealizer(final IZyNodeRealizer realizer) {
    nodeRealizer = realizer;
  }
}
