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

import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.disassembly.CFunctionListenerAdapter;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.disassembly.IFunction;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.IZyNodeRealizer;

import java.util.List;



/**
 * Updates the code node on changes to relevant functions.
 */
public final class CFunctionUpdater extends CFunctionListenerAdapter {
  /**
   * Graph to update on changes.
   */
  private final ZyGraph m_graph;

  /**
   * Realizer of the node to update.
   */
  private IZyNodeRealizer m_realizer;

  /**
   * Creates a new updater object.
   * 
   * @param graph Graph to update on changes.
   */
  public CFunctionUpdater(final ZyGraph graph) {
    m_graph = graph;
  }

  /**
   * Regenerates the content of the node and updates the graph view.
   */
  private void rebuildNode() {
    m_realizer.regenerate();
    m_graph.updateViews();
  }

  @Override
  public void appendedComment(final IFunction function, final IComment comment) {
    rebuildNode();
  }

  @Override
  public void changedName(final IFunction function, final String name) {
    rebuildNode();
  }

  @Override
  public void deletedComment(final IFunction function, final IComment comment) {
    rebuildNode();
  }

  @Override
  public void editedComment(final IFunction function, final IComment comment) {
    rebuildNode();
  }

  @Override
  public void initializedComment(final IFunction function, final List<IComment> comment) {
    rebuildNode();
  }

  /**
   * Sets the realizer of the node to update.
   * 
   * @param realizer The realizer of the node to update.
   */
  public void setRealizer(final IZyNodeRealizer realizer) {
    m_realizer = realizer;
  }
}
