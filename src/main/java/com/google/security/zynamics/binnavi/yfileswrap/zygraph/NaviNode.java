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
package com.google.security.zynamics.binnavi.yfileswrap.zygraph;

import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.types.graphs.IGraphNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.IZyNodeRealizer;

import y.base.Node;

import java.util.ArrayList;
import java.util.List;


/**
 * Node class that can be used for all nodes in ZyGraph objects.
 */
public final class NaviNode extends ZyGraphNode<INaviViewNode> implements IGraphNode<NaviNode> {
  /**
   * Realizer of the node.
   */
  private final IZyNodeRealizer m_realizer;

  /**
   * Parents of the node.
   */
  private final List<NaviNode> m_parents = new ArrayList<NaviNode>();

  /**
   * Children of the node.
   */
  private final List<NaviNode> m_children = new ArrayList<NaviNode>();

  /**
   * Creates a new node object.
   *
   * @param node The yFiles node that is used to display the node.
   * @param realizer The realizer of the yFiles node.
   * @param rawNode The raw node that provides the data for the node.
   */
  public NaviNode(final Node node, final IZyNodeRealizer realizer, final INaviViewNode rawNode) {
    super(node, realizer, rawNode);

    m_realizer = realizer;
  }

  /**
   * Links two node objects.
   *
   * @param sourceNode The source node of the link operation.
   * @param targetNode The target node of the link operation.
   */
  public static void link(final NaviNode sourceNode, final NaviNode targetNode) {
    sourceNode.m_children.add(targetNode);
    targetNode.m_parents.add(sourceNode);
  }

  /**
   * Unlinks two node objects.
   *
   * @param sourceNode The source node of the unlink operation.
   * @param targetNode The target node of the unlink operation.
   */
  public static void unlink(final NaviNode sourceNode, final NaviNode targetNode) {
    sourceNode.m_children.remove(targetNode);
    targetNode.m_parents.remove(sourceNode);
  }

  @Override
  public List<NaviNode> getChildren() {
    return new ArrayList<NaviNode>(m_children);
  }

  @Override
  public List<NaviNode> getParents() {
    return new ArrayList<NaviNode>(m_parents);
  }

  @Override
  public INaviViewNode getRawNode() {
    return super.getRawNode();
  }

  @Override
  public IZyNodeRealizer getRealizer() {
    return m_realizer;
  }

  @Override
  public String toString() {
    return getRawNode().toString();
  }

  public ZyLabelContent getNodeContent() {
    return getRealizer().getNodeContent();
  }
}
