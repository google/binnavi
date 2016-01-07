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
import com.google.security.zynamics.binnavi.ZyGraph.Builders.Modifiers.INodeModifier;
import com.google.security.zynamics.binnavi.ZyGraph.Builders.ZyCodeNodeBuilder;
import com.google.security.zynamics.binnavi.ZyGraph.Builders.ZyFunctionNodeBuilder;
import com.google.security.zynamics.binnavi.ZyGraph.Builders.ZyGroupNodeBuilder;
import com.google.security.zynamics.binnavi.ZyGraph.Builders.ZyTextNodeBuilder;
import com.google.security.zynamics.binnavi.ZyGraph.ZyGraphViewSettings;
import com.google.security.zynamics.binnavi.disassembly.CTextNode;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviGroupNode;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.ZyNodeData;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.IZyNodeRealizer;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.ZyGroupNodeRealizer;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.ZyNormalNodeRealizer;

import y.base.Node;
import y.view.Graph2D;

/**
 * Builds the label content of graph nodes.
 */
public final class ZyGraphNodeBuilder {
  /**
   * You are not supposed to instantiate this class.
   */
  private ZyGraphNodeBuilder() {
  }

  /**
   * Builds the content of the node depending on the node type.
   * 
   * @param node The node which provides the raw data.
   * @param graphSettings Graph settings used to build the graph.
   * @param modifier Calculates the address strings. This argument can be null.
   * 
   * @return The node content that is used to display the nodes in the Graph2D.
   */
  private static ZyLabelContent buildContent(final INaviViewNode node,
      final ZyGraphViewSettings graphSettings, final INodeModifier modifier) {
    Preconditions.checkNotNull(node, "IE02107: Node argument can not be null");

    if (node instanceof INaviFunctionNode) {
      return ZyFunctionNodeBuilder.buildContent((INaviFunctionNode) node, graphSettings,
          modifier);
    } else if (node instanceof INaviCodeNode) {
      return ZyCodeNodeBuilder.buildContent((INaviCodeNode) node, graphSettings, modifier);
    } else if (node instanceof CTextNode) {
      return ZyTextNodeBuilder.buildContent((CTextNode) node);
    } else if (node instanceof INaviGroupNode) {
      return ZyGroupNodeBuilder.buildContent((INaviGroupNode) node);
    } else {
      throw new IllegalStateException("IE00912: Unknown node type");
    }
  }

  /**
   * Creates a new yfiles graph node.
   * 
   * @param graph2D The graph in which the node is created.
   * @param node The raw node for which a yfiles node is created.
   * 
   * @return The yfiles node created for the given raw node.
   */
  private static Node createNode(final Graph2D graph2D, final INaviViewNode node) {
    if (node instanceof INaviGroupNode) {
      return ((INaviGroupNode) node).isCollapsed() ? graph2D.getHierarchyManager()
          .createFolderNode(graph2D) : graph2D.getHierarchyManager().createGroupNode(graph2D);
    } else {
      return graph2D.createNode();
    }
  }

  /**
   * Creates a new realizer for a raw node.
   * 
   * @param node The raw node for which the realizer is created.
   * @param content The label content of the realizer.
   * 
   * @return The realizer created for the raw node.
   */
  private static IZyNodeRealizer createRealizer(final INaviViewNode node,
      final ZyLabelContent content) {
    if (node instanceof INaviGroupNode) {
      final ZyGroupNodeRealizer<NaviNode> realizer =
          new ZyGroupNodeRealizer<NaviNode>(content, ((INaviGroupNode) node).isCollapsed());

      if ((node.getWidth() != 0) && (node.getHeight() != 0)) {
        realizer.setSize(node.getWidth(), node.getHeight());
      }

      return realizer;
    } else {
      final ZyNormalNodeRealizer<NaviNode> realizer = new ZyNormalNodeRealizer<NaviNode>(content);

      if ((node.getWidth() != 0) && (node.getHeight() != 0)) {
        realizer.setSize(node.getWidth(), node.getHeight());
      }

      return realizer;
    }
  }

  /**
   * Creates a graph node from a raw node.
   * 
   * @param node The raw node that provides the underlying data.
   * @param graph2D The graph object where the node is created.
   * @param graphSettings Graph settings used to build the graph.
   * 
   * @return The created YNode/NaviNode pair.
   */
  public static Pair<Node, NaviNode> convertNode(final INaviViewNode node, final Graph2D graph2D,
      final ZyGraphViewSettings graphSettings) {
    Preconditions.checkNotNull(node, "IE00909: Node argument can not be null");
    Preconditions.checkNotNull(graph2D, "IE00910: Graph2D argument can not be null");

    // Create the node in the Graph2D
    final Node yNode = createNode(graph2D, node);

    final ZyLabelContent content =
        ZyGraphNodeBuilder.buildContent(node, graphSettings, null);

    final IZyNodeRealizer realizer = createRealizer(node, content);

    // Associate the user data with the Graph2D node
    final NaviNode zyNode = new NaviNode(yNode, realizer, node);

    realizer.setUserData(new ZyNodeData<NaviNode>(zyNode));

    realizer.updateContentSelectionColor();

    graph2D.setRealizer(yNode, realizer.getRealizer());

    return new Pair<Node, NaviNode>(yNode, zyNode);
  }
}
