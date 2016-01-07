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
package com.google.security.zynamics.binnavi.yfileswrap.zygraph.Updaters.GroupNodes;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.ZyGraph.Builders.ZyGroupNodeBuilder;
import com.google.security.zynamics.binnavi.disassembly.CNaviGroupNodeListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.CNaviViewNodeListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.INaviGroupNode;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.IRealizerUpdater;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.IZyNodeRealizer;

import y.view.Graph2D;

import java.util.Iterator;
import java.util.List;

/**
 * This function makes sure to update function nodes if the underlying data in the IFunction object
 * the node displays changes.
 */
public final class CGroupNodeUpdater implements IRealizerUpdater<NaviNode> {
  /**
   * The graph the node belongs to.
   */
  private final Graph2D m_graph;

  /**
   * This is the node that provides the underlying data for the node.
   */
  private final INaviGroupNode m_node;

  /**
   * This is the realizer that is notified if the underlying data changes. Note that this field can
   * be null.
   */
  private IZyNodeRealizer m_realizer;

  /**
   * Updates the group node on relevant tag changes.
   */
  private final CTagUpdater m_listener;

  /**
   * Updates the group node on relevant view node changes.
   */
  private final InternalViewNodeListener m_internalViewNodeListener =
      new InternalViewNodeListener();

  /**
   * Updates the group node on relevant group node changes.
   */
  private final InternalGroupNodeListener m_internalGroupNodeListener =
      new InternalGroupNodeListener();

  /**
   * Creates a new function updater object.
   *
   * @param graph2D The graph that contains the function node.
   * @param node The function node to be updated.
   */
  public CGroupNodeUpdater(final ZyGraph zygraph, final INaviGroupNode node) {
    Preconditions.checkNotNull(zygraph, "IE00992: Graph argument can't be null");

    Preconditions.checkNotNull(node, "IE00993: Node argument can't be null");

    m_graph = zygraph.getGraph();
    m_node = node;

    m_listener = new CTagUpdater(zygraph.getGraph());

    initializeListeners();
  }

  /**
   * Initializes the listeners that are necessary for node updating.
   */
  private void initializeListeners() {
    m_node.addListener(m_internalViewNodeListener);
    m_node.addGroupListener(m_internalGroupNodeListener);

    final Iterator<CTag> it = m_node.getTagsIterator();
    while (it.hasNext()) {
      it.next().addListener(m_listener);
    }
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
    final Iterator<CTag> it = m_node.getTagsIterator();
    while (it.hasNext()) {
      it.next().removeListener(m_listener);
    }

    m_node.removeListener(m_internalViewNodeListener);
    m_node.removeGroupListener(m_internalGroupNodeListener);
  }

  @Override
  public void dispose() {
    removeListeners();
  }

  @Override
  public void generateContent(final IZyNodeRealizer realizer, final ZyLabelContent content) {
    ZyGroupNodeBuilder.buildContent(content, m_node);
  }

  @Override
  public void setRealizer(final IZyNodeRealizer realizer) {
    Preconditions.checkNotNull(realizer, "IE01740: Realizer argument can't be null");

    m_realizer = realizer;
  }

  /**
   * Updates the group node on relevant group node changes.
   */
  private class InternalGroupNodeListener extends CNaviGroupNodeListenerAdapter {
    @Override
    public void appendedGroupNodeComment(final INaviGroupNode node, final IComment comment) {
      rebuildNode();
    }

    @Override
    public void changedState(final INaviGroupNode node) {
      rebuildNode();
    }

    @Override
    public void deletedGroupNodeComment(final INaviGroupNode node, final IComment comment) {
      rebuildNode();
    }

    @Override
    public void editedGroupNodeComment(final INaviGroupNode node, final IComment comment) {
      rebuildNode();
    }

    @Override
    public void initializedGroupNodeComment(final INaviGroupNode node, final List<IComment> comment) {
      rebuildNode();
    }
  }

  /**
   * Updates the group node on relevant view node changes.
   */
  private class InternalViewNodeListener extends CNaviViewNodeListenerAdapter {
    @Override
    public void taggedNode(final INaviViewNode node, final CTag tag) {
      tag.addListener(m_listener);

      rebuildNode();
    }

    @Override
    public void untaggedNodes(final INaviViewNode node, final List<CTag> tags) {
      for (final CTag tag : tags) {
        tag.removeListener(m_listener);
      }

      rebuildNode();
    }
  }
}
