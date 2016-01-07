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
package com.google.security.zynamics.binnavi.yfileswrap.zygraph.Synchronizers;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.ITagListener;
import com.google.security.zynamics.binnavi.ZyGraph.INaviGraphListener;
import com.google.security.zynamics.binnavi.ZyGraph.IZyGraphInternals;
import com.google.security.zynamics.binnavi.ZyGraph.Implementations.CEdgeDrawingFunctions;
import com.google.security.zynamics.binnavi.ZyGraph.Synchronizers.CEdgeDrawingSynchronizer;
import com.google.security.zynamics.binnavi.ZyGraph.Synchronizers.CGraphSettingsSynchronizer;
import com.google.security.zynamics.binnavi.disassembly.CNaviGroupNodeListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviGroupNode;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModuleListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.views.CViewListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviEdge;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.Builders.ZyEdgeBuilder;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.Builders.ZyGraphNodeBuilder;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.gui.SwingInvoker;
import com.google.security.zynamics.zylib.gui.zygraph.edges.IViewEdge;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.INodeCallback;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IViewNode;
import com.google.security.zynamics.zylib.gui.zygraph.proximity.MultiEdgeHider;
import com.google.security.zynamics.zylib.types.common.IterationMode;
import com.google.security.zynamics.zylib.types.graphs.IDirectedGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.ZyGraphMappings;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.ZyEdgeRealizer;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.ZyNodeRealizer;
import com.google.security.zynamics.zylib.yfileswrap.yfiles.YHelpers;

import y.base.Edge;
import y.base.Node;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class is used to synchronize the visible ZyGraph with its invisible raw data from the
 * underlying INaviView object.
 */
public final class CViewGraphSynchronizer {
  /**
   * The graph to be synchronized.
   */
  private final ZyGraph m_graph;

  /**
   * Provides access to the internals of the graph.
   */
  private final IZyGraphInternals m_graphInternals;

  /**
   * Provides the mappings between graph objects and raw data objects.
   */
  private final ZyGraphMappings<NaviNode, NaviEdge> m_mappings;

  /**
   * Listener that handles the synchronization between raw views and the graph.
   */
  private final InternalViewListener m_viewListener = new InternalViewListener();

  /**
   * Listener that handles the synchronization between raw group nodes and graph group nodes.
   */
  private final InternalGroupNodeListener m_groupNodeListener = new InternalGroupNodeListener();

  /**
   * Listeners that are notified about changes in the graph.
   */
  private final ListenerProvider<INaviGraphListener> m_listeners =
      new ListenerProvider<INaviGraphListener>();

  /**
   * Synchronizes the visible edges with relevant edge visibility events.
   */
  private final CEdgeDrawingSynchronizer m_edgeDrawingSynchronizer;

  /**
   * Since the view of a graph can change arbitrarily we have to keep track of this separately.
   */
  private INaviView m_oldView;

  /**
   * Synchronizes the graph with its settings.
   */
  private final CGraphSettingsSynchronizer m_settingsSynchronizer;

  /**
   * Flag that indicates whether multi edges are enabled or disabled.
   */
  private boolean m_multiEdgeUpdatingEnabled = true;

  /**
   * Keeps track of what modules already have listeners attached to them.
   *
   * TODO: Remove listeners again if count drops to 0.
   */
  private final Map<INaviModule, Integer> m_cachedModuleListeners =
      new HashMap<INaviModule, Integer>();

  /**
   * Used to synchronize relevant changes in modules with graph nodes.
   */
  private final InternalModuleListener m_internalModuleListener = new InternalModuleListener();

  /**
   * Keeps track of what tags already have listeners attached to them.
   *
   * TODO: Remove listeners again if count drops to 0.
   */
  private final Map<CTag, Integer> m_cachedTagListeners = new HashMap<CTag, Integer>();

  /**
   * Synchronizes relevant changes in tags with the visible nodes.
   */
  private final InternalTagListener m_internalTagListener = new InternalTagListener();

  /**
   * Keeps track of the currently selected nodes.
   */
  private final Set<NaviNode> m_selectedNodes = new HashSet<NaviNode>();

  /**
   * Creates a new object that synchronizes between a raw view and a visible graph.
   *
   * @param graph The graph to be synchronized.
   * @param graphInternals Provides access to the internals of the graph.
   * @param mappings Provides the mappings between graph objects and raw data objects.
   */
  public CViewGraphSynchronizer(final ZyGraph graph, final IZyGraphInternals graphInternals,
      final ZyGraphMappings<NaviNode, NaviEdge> mappings) {

    m_graph = Preconditions.checkNotNull(graph, "IE00981: Graph argument can not be null");
    m_graphInternals = Preconditions.checkNotNull(graphInternals,
        "IE00982: Graph internals argument can not be null");
    m_mappings = Preconditions.checkNotNull(mappings, "IE00983: Mappings argument can not be null");

    m_oldView = graph.getRawView();

    m_edgeDrawingSynchronizer = new CEdgeDrawingSynchronizer(graph);
    m_settingsSynchronizer = new CGraphSettingsSynchronizer(graph);

    for (final NaviNode node : mappings.getNodes()) {
      if (node.isSelected()) {
        m_selectedNodes.add(node);
      }
    }

    initializeListeners();
  }

  /**
   * Adds a new listener to a module if the given node requires it.
   *
   * @param node The node to check.
   */
  private void addModuleListener(final INaviFunctionNode node) {
    final INaviModule module = node.getFunction().getModule();

    if (!m_cachedModuleListeners.containsKey(module)) {
      m_cachedModuleListeners.put(module, 0);

      module.addListener(m_internalModuleListener);
    }

    m_cachedModuleListeners.put(module, m_cachedModuleListeners.get(module) + 1);
  }

  /**
   * Adds a new listener to a tag if the given node requires it.
   *
   * @param tag The tag to check.
   */
  private void addTagListener(final CTag tag) {
    if (!m_cachedTagListeners.containsKey(tag)) {
      m_cachedTagListeners.put(tag, 0);

      tag.addListener(m_internalTagListener);
    }

    m_cachedTagListeners.put(tag, m_cachedTagListeners.get(tag) + 1);
  }

  /**
   * Sets up the listeners that update the graph when something in the raw data changes.
   */
  private void initializeListeners() {
    m_graph.getRawView().addListener(m_viewListener);

    for (final INaviViewNode node : m_graph.getRawView().getGraph().getNodes()) {
      if (node instanceof INaviGroupNode) {
        ((INaviGroupNode) node).addGroupListener(m_groupNodeListener);
      }
    }

    for (final INaviViewNode node : m_graph.getRawView().getGraph()) {
      if (node instanceof INaviFunctionNode) {
        addModuleListener((INaviFunctionNode) node);
      }

      final Iterator<CTag> it = node.getTagsIterator();
      while (it.hasNext()) {
        addTagListener(it.next());
      }
    }
  }

  /**
   * Rebuilds a visible node.
   *
   * @param node The node to rebuild.
   */
  private void rebuildNode(final NaviNode node) {
    (((ZyNodeRealizer<?>) m_graph.getGraph().getRealizer(node.getNode()))).regenerate();
  }

  /**
   * Removes an edge from the graph and cleans up all associated resources.
   *
   * @param edge The edge to remove from the graph.
   */
  private void removeEdge(final INaviEdge edge) {
    m_graphInternals.removeEdge(m_graph.getEdge(edge));
    m_edgeDrawingSynchronizer.updateEdgeDrawingState();
    m_graph.updateViews();
  }

  /**
   * Removes the listeners that update the graph when something in the raw data changes.
   *
   * @param view The view from which the listeners are removed.
   */
  private void removeListeners(final INaviView view) {
    view.removeListener(m_viewListener);

    for (final INaviViewNode node : view.getGraph().getNodes()) {
      if (node instanceof INaviGroupNode) {
        ((INaviGroupNode) node).removeGroupListener(m_groupNodeListener);
      }
    }
  }

  /**
   * Removes a node from the graph and cleans up all associated resources.
   *
   * @param node The node to remove from the graph.
   */
  private void removeNode(final NaviNode node) {
    if (node.getRawNode() instanceof INaviGroupNode) {
      ((INaviGroupNode) node.getRawNode()).removeGroupListener(m_groupNodeListener);
    }
    m_selectedNodes.remove(node);
    m_graphInternals.removeNode(node);
    m_graph.updateViews();
  }

  /**
   * Depending on the state of the multi edge settings option, this function either hides or shows
   * multiple edges between the same nodes.
   */
  private void updateMultiEdges() {
    if (m_multiEdgeUpdatingEnabled) {
      if (m_graph.getSettings().getEdgeSettings().getDisplayMultipleEdgesAsOne()) {
        MultiEdgeHider.hideMultipleEdgesInternal(m_graph);
      } else {
        MultiEdgeHider.unhideMultipleEdgesInternal(m_graph);
      }
    }
  }

  /**
   * Depending on the state of the multi edge settings option, this function either hides or shows
   * multiple edges between a node and its parents/children.
   *
   * @param node The node whose edges are updated.
   */
  private void updateMultiEdges(final IViewNode<?> node) {
    if (m_multiEdgeUpdatingEnabled) {
      if (m_graph.getSettings().getEdgeSettings().getDisplayMultipleEdgesAsOne()) {
        MultiEdgeHider.hideMultipleEdgesInternal(m_graph.getNode(node));
      } else {
        MultiEdgeHider.unhideMultipleEdgesInternal(m_graph);
      }
    }
  }

  /**
   * Updates the parent group node of a node.
   *
   * @param node The node whose parent group node is updated.
   * @param groupNode The new parent group node.
   */
  private void updateParentNode(final INaviViewNode node, final INaviGroupNode groupNode) {
    final Node mappedNaviNode = m_mappings.getYNode(node);
    final Node mappedGroupNode = groupNode == null ? null : m_mappings.getYNode(groupNode);

    if (mappedNaviNode != null) {
      // We need this null-check here because a group node can lose all
      // of its members to another group node while the group node itself
      // is also added to the new group. Since empty group nodes are
      // automatically removed, the original group node can already be
      // removed from the graph before the changedParentGroup event is sent.

      m_graph.getGraph().getHierarchyManager().setParentNode(mappedNaviNode, mappedGroupNode);

      m_graph.updateViews();
    }
  }

  /**
   * Adds a listener that is notified about changes in the graph.
   *
   * @param listener The listener object that is notified about changes in the graph.
   */
  public void addListener(final INaviGraphListener listener) {
    m_listeners.addListener(listener);
  }

  /**
   * Frees allocated resources. Using the object after this function was called leads to undefined
   * behavior.
   */
  public void dispose() {
    removeListeners(m_graph.getRawView());

    m_edgeDrawingSynchronizer.dispose();
    m_settingsSynchronizer.dispose();
  }

  /**
   * Returns the currently selected nodes.
   *
   * @return The currently selected nodes.
   */
  public Set<NaviNode> getSelectedNodes() {
    return new HashSet<NaviNode>(m_selectedNodes);
  }

  /**
   * Removes a listener that was previously notified about changes in the graph.
   *
   * @param listener The listener object to remove.
   */
  public void removeListener(final INaviGraphListener listener) {
    m_listeners.removeListener(listener);
  }

  /**
   * Tells the synchronizer to reset its internal state.
   */
  public void reset() {
    // A graph can change its view; when that happens we need to
    // reset the listeners on the raw objects.

    removeListeners(m_oldView);
    initializeListeners();

    m_oldView = m_graph.getRawView();
  }

  /**
   * Toggles between showing multiple edges and hiding them.
   *
   * @param enabled True, to show multiple edges between the same nodes. False, to hide them.
   */
  public void setMultiEdgeUpdatingEnabled(final boolean enabled) {
    m_multiEdgeUpdatingEnabled = enabled;

    updateMultiEdges();
  }

  /**
   * Keeps track of changes in group nodes and updates the graph nodes if necessary.
   */
  private class InternalGroupNodeListener extends CNaviGroupNodeListenerAdapter {
    @Override
    public void changedState(final INaviGroupNode node) {
      // Update the visible group node when the raw group node
      // is collapsed or expanded.

      if (node.isCollapsed()) {
        YHelpers.closeGroup(m_graph.getGraph(), m_mappings.getNode(node).getNode());
      } else {
        YHelpers.openFolder(m_graph.getGraph(), m_mappings.getNode(node).getNode());
      }

      if (m_graph.getSettings().getLayoutSettings().getAutomaticLayouting()) {
        m_graph.doLayout();
      }
    }
  }

  /**
   * Updates the function node on relevant changes in the underlying module.
   */
  private class InternalModuleListener extends CModuleListenerAdapter {
    @Override
    public void changedName(final INaviModule module, final String name) {
      m_graph.iterate(new INodeCallback<NaviNode>() {
        @Override
        public IterationMode next(final NaviNode node) {
          final INaviViewNode rawNode = node.getRawNode();

          if (rawNode instanceof INaviFunctionNode) {
            final INaviFunctionNode fnode = (INaviFunctionNode) rawNode;

            if (fnode.getFunction().getModule().equals(module)) {
              rebuildNode(node);
            }
          }

          return IterationMode.CONTINUE;
        }
      });
    }
  }

  /**
   * Updates the graph on relevant changes to tags.
   */
  private class InternalTagListener implements ITagListener {
    @Override
    public void changedDescription(final CTag tag, final String description) {
      m_graph.iterate(new INodeCallback<NaviNode>() {
        @Override
        public IterationMode next(final NaviNode node) {
          final INaviViewNode rawNode = node.getRawNode();

          if (rawNode.isTagged(tag)) {
            rebuildNode(node);
          }

          return IterationMode.CONTINUE;
        }
      });
    }

    @Override
    public void changedName(final CTag tag, final String name) {
      m_graph.iterate(new INodeCallback<NaviNode>() {
        @Override
        public IterationMode next(final NaviNode node) {
          final INaviViewNode rawNode = node.getRawNode();

          if (rawNode.isTagged(tag)) {
            rebuildNode(node);
          }

          return IterationMode.CONTINUE;
        }
      });
    }

    @Override
    public void deletedTag(final CTag tag) {}
  }

  /**
   * Listener that handles the synchronization between raw views and the graph.
   */
  private class InternalViewListener extends CViewListenerAdapter {
    /**
     * Deletes a node from a view.
     *
     * @param node The node to delete.
     */
    private void deletedNodeImplementation(final INaviViewNode node) {
      removeNode(m_mappings.getNode(node));

      if (m_graph.getSettings().getLayoutSettings().getAutomaticLayouting()
          && isCollapsedGroupNode(node)) {
        m_graph.doLayout();
      }

      m_graphInternals.notifyNodeDeleted();
    }

    /**
     * Deletes nodes from a view.
     *
     * @param nodes The nodes to delete.
     */
    private void deletedNodesImplementation(final Iterable<INaviViewNode> nodes) {
      final boolean oldProxyMode =
          m_graph.getSettings().getProximitySettings().getProximityBrowsing();

      boolean needsLayout = false;

      for (final INaviViewNode node : nodes) {
        removeNode(m_mappings.getNode(node));

        needsLayout |= isCollapsedGroupNode(node);
      }

      if (oldProxyMode) {
        m_graph.getSettings().getProximitySettings().setProximityBrowsing(oldProxyMode);
      }

      if (m_graph.getSettings().getLayoutSettings().getAutomaticLayouting() && needsLayout) {
        m_graph.doLayout();
      }

      m_graphInternals.notifyNodeDeleted();
    }

    /**
     * Determines whether a view node is a collapsed group node.
     *
     * @param node The node to check.
     *
     * @return True, if the node is a collapsed group node. False, otherwise.
     */
    private boolean isCollapsedGroupNode(final INaviViewNode node) {
      return (node instanceof INaviGroupNode) && ((INaviGroupNode) node).isCollapsed();
    }

    /**
     * Inserts a new edge into the graph for a given raw node.
     *
     * @param edge The edge to insert.
     */
    private void setupNewEdge(final INaviEdge edge) {
      // We need to make source and target visible, otherwise
      // yfiles will not allow us to create the edge.

      Preconditions.checkNotNull(m_graph.getYNode(edge.getSource()), "Unknown source node");
      Preconditions.checkNotNull(m_graph.getYNode(edge.getTarget()),
          String.format("Unknown target node '%s'", edge.getTarget()));

      edge.getSource().setVisible(true);
      edge.getTarget().setVisible(true);

      final Pair<Edge, NaviEdge> result = ZyEdgeBuilder.convertEdge(edge,
          m_mappings.getNode(edge.getSource()), m_mappings.getNode(edge.getTarget()),
          m_graph.getGraph(), true);

      @SuppressWarnings("unchecked")
      final ZyEdgeRealizer<NaviEdge> realizer =
          (ZyEdgeRealizer<NaviEdge>) m_graph.getGraph().getRealizer(result.first());

      realizer.setDrawBends(m_graph.getSettings().getEdgeSettings().getDrawSelectedBends());
      realizer.setDrawSloppyEdges(CEdgeDrawingFunctions.calcDrawSloppyEdges(m_graph));

      m_mappings.addEdge(result.first(), result.second());
      m_edgeDrawingSynchronizer.updateEdgeDrawingState();
    }

    /**
     * Inserts a new node into the graph for a given raw node.
     *
     * @param rawNode The raw node that provides the data.
     */
    private void setupNewNode(final INaviViewNode rawNode) {
      final Pair<Node, NaviNode> result =
          ZyGraphNodeBuilder.convertNode(rawNode, m_graph.getGraph(), m_graph.getSettings());

      m_mappings.addNode(result.first(), result.second());

      // rawNode.addListener(m_nodeListener);

      if (rawNode instanceof INaviGroupNode) {
        ((INaviGroupNode) rawNode).addGroupListener(m_groupNodeListener);
      }

      for (final INaviGraphListener listener : m_listeners) {
        try {
          listener.addedNode(m_graph, result.second());
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void addedEdge(final INaviView view, final INaviEdge edge) {
      setupNewEdge(edge);
    }

    @Override
    public void addedNode(final INaviView view, final INaviViewNode node) {
      if (node instanceof INaviFunctionNode) {
        addModuleListener((INaviFunctionNode) node);
      }

      if (node.isSelected()) {
        m_selectedNodes.add(m_mappings.getNode(node));
      }

      new SwingInvoker() {
        @Override
        protected void operation() {
          setupNewNode(node);
        }
      }.invokeAndWait();

      if (node instanceof INaviGroupNode) {
        final INaviGroupNode gnode = (INaviGroupNode) node;

        for (final INaviViewNode viewNode : gnode.getElements()) {
          updateParentNode(viewNode, gnode);
        }
      }
    }

    @Override
    public void appendedGlobalEdgeComment(final INaviView view, final INaviEdge edge) {
      (((ZyEdgeRealizer<?>) m_graph.getGraph().getRealizer(m_graph.getEdge(edge).getEdge())))
          .regenerate();
    }

    @Override
    public void appendedLocalEdgeComment(final INaviView view, final INaviEdge edge) {
      (((ZyEdgeRealizer<?>) m_graph.getGraph().getRealizer(m_graph.getEdge(edge).getEdge())))
          .regenerate();
    }

    @Override
    public void appendedLocalFunctionNodeComment(final INaviView view, final INaviFunctionNode node,
        final IComment comment) {
      rebuildNode(m_graph.getNode(node));
    }

    @Override
    public void changedParentGroup(final INaviView view, final INaviViewNode node,
        final INaviGroupNode groupNode) {
      // Update the group a node belongs to

      updateParentNode(node, groupNode);
    }

    @Override
    public void changedSelection(final INaviView view, final IViewNode<?> node,
        final boolean selected) {
      // Edges are selected if both their connected nodes are selected.

      for (final IViewEdge<? extends IViewNode<?>> edge : node.getIncomingEdges()) {
        edge.setSelected(selected || edge.getSource().isSelected());
      }

      for (final IViewEdge<? extends IViewNode<?>> edge : node.getOutgoingEdges()) {
        edge.setSelected(selected || edge.getTarget().isSelected());
      }

      if (selected) {
        m_selectedNodes.add(m_graph.getNode(node));
      } else {
        m_selectedNodes.remove(m_graph.getNode(node));
      }
    }

    @Override
    public void changedVisibility(final INaviView view, final IViewEdge<?> edge) {
      m_edgeDrawingSynchronizer.updateEdgeDrawingState();

      final NaviEdge naviEdge = m_mappings.getEdge(edge);

      if (edge != null) {
        naviEdge.getRealizer().setDrawSloppyEdges(
            CEdgeDrawingFunctions.calcDrawSloppyEdges(m_graph));
      }
    }

    @Override
    public void changedVisibility(final INaviView view, final IViewNode<?> node,
        final boolean visible) {
      updateMultiEdges(node);
    }

    @Override
    public void closedView(final INaviView view,
        final IDirectedGraph<INaviViewNode, INaviEdge> oldGraph) {
      // TODO: NH what about CNaviViewNodeListener? They are not removed explicitly!
      // This is a valid concern; it is unclear at this point what happens if a view
      // is closed that was not open in a ZyGraph object.
    }

    @Override
    public boolean closingView(final INaviView view) {
      return true;
    }

    @Override
    public void deletedEdge(final INaviView view, final INaviEdge edge) {
      removeEdge(edge);
    }

    @Override
    public void deletedGlobalEdgeComment(final INaviView view, final INaviEdge edge) {
      (((ZyEdgeRealizer<?>) m_graph.getGraph().getRealizer(m_graph.getEdge(edge).getEdge())))
          .regenerate();
    }

    @Override
    public void deletedLocalEdgeComment(final INaviView view, final INaviEdge edge) {
      (((ZyEdgeRealizer<?>) m_graph.getGraph().getRealizer(m_graph.getEdge(edge).getEdge())))
          .regenerate();
    }

    @Override
    public void deletedLocalFunctionNodeComment(final INaviView view, final INaviFunctionNode node,
        final IComment comment) {
      // Empty default implementation
    }

    @Override
    public void deletedNode(final INaviView view, final INaviViewNode node) {
      new SwingInvoker() {
        @Override
        protected void operation() {
          deletedNodeImplementation(node);
        }
      }.invokeAndWait();
    }

    @Override
    public void deletedNodes(final INaviView view, final Collection<INaviViewNode> nodes) {
      new SwingInvoker() {
        @Override
        protected void operation() {
          deletedNodesImplementation(nodes);
        }
      }.invokeAndWait();
    }

    @Override
    public void editedGlobalEdgeComment(final INaviView view, final INaviEdge edge) {
      (((ZyEdgeRealizer<?>) m_graph.getGraph().getRealizer(m_graph.getEdge(edge).getEdge())))
          .regenerate();
    }

    @Override
    public void editedLocalEdgeComment(final INaviView view, final INaviEdge edge) {
      (((ZyEdgeRealizer<?>) m_graph.getGraph().getRealizer(m_graph.getEdge(edge).getEdge())))
          .regenerate();
    }

    @Override
    public void editedLocalFunctionNodeComment(final INaviView view, final INaviFunctionNode node,
        final IComment comment) {
      // Empty default implementation
    }

    @Override
    public void taggedNode(final INaviView view, final INaviViewNode node, final CTag tag) {
      m_graph.iterate(new INodeCallback<NaviNode>() {
        @Override
        public IterationMode next(final NaviNode cnode) {
          final INaviViewNode rawNode = cnode.getRawNode();

          if (rawNode.equals(node)) {
            rebuildNode(cnode);
          }

          return IterationMode.CONTINUE;
        }
      });

      addTagListener(tag);

      for (final INaviGraphListener listener : m_listeners) {
        try {
          listener.taggedNode(m_graph.getRawView(), node, tag);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void untaggedNodes(final INaviView view, final INaviViewNode node,
        final List<CTag> tags) {
      m_graph.iterate(new INodeCallback<NaviNode>() {
        @Override
        public IterationMode next(final NaviNode cnode) {
          final INaviViewNode rawNode = cnode.getRawNode();

          if (rawNode.equals(node)) {
            rebuildNode(cnode);
          }

          return IterationMode.CONTINUE;
        }
      });

      for (final INaviGraphListener listener : m_listeners) {
        for (final CTag tag : tags) {
          try {
            listener.untaggedNode(m_graph.getRawView(), node, tag);
          } catch (final Exception exception) {
            CUtilityFunctions.logException(exception);
          }
        }
      }
    }
  }
}
