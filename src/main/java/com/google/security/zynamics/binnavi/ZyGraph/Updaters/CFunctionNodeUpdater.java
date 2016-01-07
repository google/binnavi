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
package com.google.security.zynamics.binnavi.ZyGraph.Updaters;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.ZyGraph.Builders.ZyFunctionNodeBuilder;
import com.google.security.zynamics.binnavi.ZyGraph.Builders.Modifiers.CDefaultModifier;
import com.google.security.zynamics.binnavi.debug.debugger.BackEndDebuggerProvider;
import com.google.security.zynamics.binnavi.disassembly.CFunctionListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.disassembly.IFunction;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.IRealizerUpdater;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.IZyNodeRealizer;

import java.util.List;

/**
 * This function makes sure to update function nodes if the underlying data in the IFunction object
 * the node displays changes.
 */
public final class CFunctionNodeUpdater implements IRealizerUpdater<NaviNode> {
  /**
   * The graph the node belongs to.
   */
  private final ZyGraph m_graph;

  /**
   * This is the node that provides the underlying data for the node.
   */
  private final INaviFunctionNode m_node;

  /**
   * This is the realizer that is notified if the underlying data changes. Note that this field can
   * be null.
   */
  private IZyNodeRealizer m_realizer;

  /**
   * Calculates the addresses shown in the function node depending on the active settings.
   */
  private final CDefaultModifier m_nodeModifier;

  /**
   * Updates the function node on relevant changes in the underlying function.
   */
  private final InternalFunctionListener m_functionListener = new InternalFunctionListener();

  /**
   * Creates a new function updater object.
   *
   * @param graph The graph the function node belongs to.
   * @param node The function node to be updated.
   * @param provider Provides debuggers that influence the function node.
   */
  public CFunctionNodeUpdater(
      final ZyGraph graph, final INaviFunctionNode node, final BackEndDebuggerProvider provider) {
    m_graph = Preconditions.checkNotNull(graph, "IE00989: Graph argument can't be null");
    m_node = Preconditions.checkNotNull(node, "IE00990: Node argument can't be null");
    Preconditions.checkNotNull(provider, "IE02241: Provider argument can not be null");

    m_nodeModifier = new CDefaultModifier(graph.getSettings(), provider);

    initializeListeners();
  }

  /**
   * Initializes the listeners that are necessary for node updating.
   */
  private void initializeListeners() {
    m_node.getFunction().addListener(m_functionListener);
  }

  /**
   * Regenerates the content of the node and updates the graph view.
   */
  private void rebuildNode() {
    m_realizer.regenerate();

    m_graph.updateViews();
  }

  /**
   * Removes all listeners.
   */
  private void removeListeners() {
    m_node.getFunction().removeListener(m_functionListener);
  }

  @Override
  public void dispose() {
    removeListeners();
  }

  @Override
  public void generateContent(final IZyNodeRealizer realizer, final ZyLabelContent content) {
    ZyFunctionNodeBuilder.buildContent(content, m_node, m_graph.getSettings(), m_nodeModifier);
  }

  @Override
  public void setRealizer(final IZyNodeRealizer realizer) {
    Preconditions.checkNotNull(realizer, "IE00996: Realizer argument can't be null");

    m_realizer = realizer;
  }

  /**
   * Updates the function node on relevant changes in the underlying function.
   */
  private class InternalFunctionListener extends CFunctionListenerAdapter {
    @Override
    public void appendedComment(final IFunction function, final IComment comment) {
      if (m_realizer != null) {
        rebuildNode();
      }
    }

    @Override
    public void changedDescription(final IFunction function, final String description) {
      rebuildNode();
    }

    @Override
    public void changedName(final IFunction function, final String name) {
      if (m_realizer != null) {
        rebuildNode();
      }
    }

    @Override
    public void deletedComment(final IFunction function, final IComment comment) {
      if (m_realizer != null) {
        rebuildNode();
      }
    }

    @Override
    public void editedComment(final IFunction function, final IComment comment) {
      if (m_realizer != null) {
        rebuildNode();
      }
    }

    @Override
    public void initializedComment(final IFunction function, final List<IComment> comments) {
      if (m_realizer != null) {
        rebuildNode();
      }
    }
  }
}
