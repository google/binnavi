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
package com.google.security.zynamics.binnavi.yfileswrap.zygraph.Updaters;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.ZyGraph.Builders.ZyTextNodeBuilder;
import com.google.security.zynamics.binnavi.disassembly.CNaviTextNodeListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.INaviTextNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.IRealizerUpdater;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.IZyNodeRealizer;

import y.view.Graph2D;

/**
 * Updates text nodes on relevant events.
 */
public final class CTextNodeUpdater implements IRealizerUpdater<NaviNode> {
  /**
   * The graph where the code node is shown.
   */
  private final Graph2D m_graph;

  /**
   * The text node that is updated by the updater.
   */
  private final INaviTextNode m_node;

  /**
   * The realizer of the code node.
   */
  private IZyNodeRealizer m_realizer;

  /**
   * Listener that rebuilds the text node on relevant events.
   */
  private final InternalListener m_listener = new InternalListener();

  /**
   * Creates a new updater object.
   *
   * @param zygraph The graph where the code node is shown. The only reason this is a ZyGraph and
   *                not a Graph2D is that we want no dependency on yFiles for instantiating this
   *                class.
   * @param textNode The text node that is updated by the updater.
   */
  public CTextNodeUpdater(final ZyGraph zygraph, final INaviTextNode textNode) {
    Preconditions.checkNotNull(zygraph, "IE00994: Graph argument can't be null");

    Preconditions.checkNotNull(textNode, "IE00995: Code node argument can't be null");

    m_graph = zygraph.getGraph();
    m_node = textNode;

    initializeListeners();
  }

  /**
   * Initializes the listeners that are responsible for updating the code node.
   */
  private void initializeListeners() {
    m_node.addListener(m_listener);
  }

  /**
   * Removes all added listeners.
   */
  private void removeListeners() {
    m_node.removeListener(m_listener);
  }

  @Override
  public void dispose() {
    removeListeners();
  }

  @Override
  public void generateContent(final IZyNodeRealizer realizer, final ZyLabelContent content) {
    ZyTextNodeBuilder.buildContent(content, m_node);
  }

  @Override
  public void setRealizer(final IZyNodeRealizer realizer) {
    m_realizer = realizer;
  }

  /**
   * Listener that rebuilds the text node on relevant events.
   */
  private class InternalListener extends CNaviTextNodeListenerAdapter {
    @Override
    public void appendedTextNodeComment(final INaviTextNode node, final IComment comment) {
      m_realizer.regenerate();
      m_graph.updateViews();
    }

    @Override
    public void deletedTextNodeComment(final INaviTextNode node, final IComment comment) {
      m_realizer.regenerate();
      m_graph.updateViews();
    }

    @Override
    public void editedTextNodeComment(final INaviTextNode node, final IComment comment) {
      m_realizer.regenerate();
      m_graph.updateViews();
    }
  }
}
