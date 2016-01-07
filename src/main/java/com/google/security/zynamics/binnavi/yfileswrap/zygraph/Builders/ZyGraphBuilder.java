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
package com.google.security.zynamics.binnavi.yfileswrap.zygraph.Builders;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.ZyGraph.Builders.CGraphBuilderReporter;
import com.google.security.zynamics.binnavi.ZyGraph.Builders.GraphBuilderEvents;
import com.google.security.zynamics.binnavi.ZyGraph.Builders.IGraphBuilderListener;
import com.google.security.zynamics.binnavi.ZyGraph.ZyGraphViewSettings;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviEdge;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.general.Pair;

import y.base.Edge;
import y.base.Node;
import y.view.Graph2D;
import y.view.hierarchy.GroupNodeRealizer;
import y.view.hierarchy.HierarchyManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class builds a Graph2D object from a view object.
 */
public final class ZyGraphBuilder {
  /**
   * YNode -> NaviNode mapping of the created nodes
   */
  private final LinkedHashMap<Node, NaviNode> m_ynodeToNodeMap =
      new LinkedHashMap<Node, NaviNode>();

  /**
   * YEdge -> NaviEdge mapping of the created edges
   */
  private final LinkedHashMap<Edge, NaviEdge> m_yedgeToEdgeMap =
      new LinkedHashMap<Edge, NaviEdge>();

  /**
   * Listeners that are notified about the progress of graph creation.
   */
  private final ListenerProvider<IGraphBuilderListener> m_listeners =
      new ListenerProvider<IGraphBuilderListener>();

  /**
   * Reports graph building events to listeners.
   */
  private final CGraphBuilderReporter m_loadReporter = new CGraphBuilderReporter(m_listeners);

  /**
   * Puts nodes that have raw parent group nodes into the corresponding yFiles group nodes.
   *
   * @param nodes The nodes to be put into their parent group nodes.
   * @param graph2D The yFiles graph the nodes belong to.
   * @param rawNodeToNodeMap Maps between raw nodes and yFiles nodes.
   */
  private static void setupGroupNodes(final Iterable<INaviViewNode> nodes, final Graph2D graph2D,
      final Map<INaviViewNode, Node> rawNodeToNodeMap) {
    for (final INaviViewNode node : nodes) {
      if (node.getParentGroup() != null) {
        final Node child = rawNodeToNodeMap.get(node);
        final Node parent = rawNodeToNodeMap.get(node.getParentGroup());

        graph2D.getHierarchyManager().setParentNode(child, parent);
      }
    }
  }

  /**
   * Reports a load event and throws an exception if the user has canceled graph building.
   *
   * @param event The event to report.
   *
   * @throws LoadCancelledException Thrown if the user has canceled graph building.
   */
  private void checkCancellation(final GraphBuilderEvents event) throws LoadCancelledException {
    if (!m_loadReporter.report(event)) {
      throw new LoadCancelledException();
    }
  }

  /**
   * Converts the edges of a view into Graph2D edges.
   *
   * @param edges The edges to convert.
   * @param graph2D The graph where the edges are inserted.
   * @param rawNodeToNodeMap Keeps track of view node => graph node mappings.
   * @param adjustColors
   */
  private void convertEdges(final Collection<INaviEdge> edges, final Graph2D graph2D,
      final Map<INaviViewNode, Node> rawNodeToNodeMap,
      final boolean adjustColors) {
    for (final INaviEdge edge : edges) {
      // Get the nodes connected by the edge
      final NaviNode sourceNode = m_ynodeToNodeMap.get(rawNodeToNodeMap.get(edge.getSource()));
      final NaviNode targetNode = m_ynodeToNodeMap.get(rawNodeToNodeMap.get(edge.getTarget()));

      final Pair<Edge, NaviEdge> result =
          ZyEdgeBuilder.convertEdge(edge, sourceNode, targetNode, graph2D, adjustColors);

      m_yedgeToEdgeMap.put(result.first(), result.second());
    }
  }

  /**
   * Converts the nodes of a view into Graph2D nodes.
   *
   * @param nodes The nodes to convert.
   * @param graph2D The graph where the nodes are inserted.
   * @param rawNodeToNodeMap Keeps track of view node => graph node mappings.
   * @param graphSettings Graph settings used to build the graph.
   */
  private void convertNodes(final Collection<INaviViewNode> nodes, final Graph2D graph2D,
      final Map<INaviViewNode, Node> rawNodeToNodeMap,
      final ZyGraphViewSettings graphSettings) {
    for (final INaviViewNode node : nodes) {
      final Pair<Node, NaviNode> result =
          ZyGraphNodeBuilder.convertNode(node, graph2D, graphSettings);

      // Keep track of the view node => Graph2D node mapping
      rawNodeToNodeMap.put(node, result.first());
      m_ynodeToNodeMap.put(result.first(), result.second());
    }
  }

  /**
   * Adds a listener that is notified about progress in graph creation.
   *
   * @param listener The listener object to add.
   */
  public void addListener(final IGraphBuilderListener listener) {
    m_listeners.addListener(listener);
  }

  /**
   * Converts a view to a Graph2D object.
   *
   * @param nodes Nodes to convert.
   * @param edges Edges to convert.
   * @param graphSettings Graph settings used to build the graph.
   * @param adjustColors True, to initialize the colors of the nodes. False, otherwise.
   *
   * @return The created Graph2D object.
   * @throws LoadCancelledException Thrown if loading the graph was cancelled.
   */
  public Graph2D convert(final Collection<INaviViewNode> nodes, final Collection<INaviEdge> edges,
      final ZyGraphViewSettings graphSettings, final boolean adjustColors)
      throws LoadCancelledException {
    Preconditions.checkNotNull(nodes, "IE00905: View can not be null");
    Preconditions.checkNotNull(edges, "IE00906: Edges argument can not be null");

    if (!m_loadReporter.report(GraphBuilderEvents.Started)) {
      throw new LoadCancelledException();
    }

    m_loadReporter.start();

    final Graph2D graph2D = new Graph2D();
    final HierarchyManager hierarchyManager = new HierarchyManager(graph2D);
    graph2D.setHierarchyManager(hierarchyManager);
    hierarchyManager.addHierarchyListener(new GroupNodeRealizer.StateChangeListener());

    checkCancellation(GraphBuilderEvents.InitializedGraph);

    // Keep track of all connections between view nodes and yfiles nodes
    final HashMap<INaviViewNode, Node> rawNodeToNodeMap = new HashMap<INaviViewNode, Node>();

    // To convert the view into a Graph2D object, it is necessary to convert every node
    // and every edge from the view into the corresponding yfiles objects.
    convertNodes(nodes, graph2D, rawNodeToNodeMap, graphSettings);
    checkCancellation(GraphBuilderEvents.ConvertedNodes);

    convertEdges(edges, graph2D, rawNodeToNodeMap, adjustColors);
    checkCancellation(GraphBuilderEvents.ConvertedEdges);

    setupGroupNodes(nodes, graph2D, rawNodeToNodeMap);
    checkCancellation(GraphBuilderEvents.CreatedGroupNodes);
    checkCancellation(GraphBuilderEvents.Finished);

    return graph2D;
  }

  /**
   * Returns the index of the last finished step.
   *
   * @return The index of the last finished step.
   */
  public int getBuildStep() {
    return m_loadReporter.getStep();
  }

  /**
   * Returns the generated edge mapping.
   *
   * @return The generated edge mapping.
   */
  public LinkedHashMap<Edge, NaviEdge> getEdgeMap() {
    return m_yedgeToEdgeMap;
  }

  /**
   * Returns the generated node mapping.
   *
   * @return The generated node mapping.
   */
  public LinkedHashMap<Node, NaviNode> getNodeMap() {
    return m_ynodeToNodeMap;
  }

  /**
   * Removes a listener object from the graph builder.
   *
   * @param listener The listener object to remove.
   */
  public void removeListener(final IGraphBuilderListener listener) {
    m_listeners.removeListener(listener);
  }
}
