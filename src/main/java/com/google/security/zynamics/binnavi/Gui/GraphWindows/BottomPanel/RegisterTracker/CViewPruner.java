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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.RegisterTracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.disassembly.CCodeNode;
import com.google.security.zynamics.binnavi.disassembly.CFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType;

/**
 * Class for creating a view from the results of an operand tracking operation.
 */
public final class CViewPruner {
  /**
   * You are not supposed to instantiate this class.
   */
  private CViewPruner() {
  }

  /**
   * Converts edges of the original view to the pruned view.
   *
   * @param view The original view.
   * @param prunedView The pruned view.
   * @param nodeMap A mapping between nodes of the original view and nodes of the pruned view.
   */
  private static void convertEdges(final INaviView view, final INaviView prunedView,
      final Map<INaviViewNode, INaviViewNode> nodeMap) {
    final Set<Pair<INaviViewNode, INaviViewNode>> createdEdges =
        new HashSet<Pair<INaviViewNode, INaviViewNode>>();

    for (final INaviEdge edge : view.getGraph().getEdges()) {
      final Set<EdgeResult> sources = getSources(edge, nodeMap, new HashSet<INaviEdge>());
      final Set<EdgeResult> targets = getTargets(edge, nodeMap, new HashSet<INaviEdge>());

      for (final EdgeResult source : sources) {
        for (final EdgeResult target : targets) {
          final Pair<INaviViewNode, INaviViewNode> edgePair =
              new Pair<INaviViewNode, INaviViewNode>(source.m_node, target.m_node);

          if (createdEdges.contains(edgePair)) {
            continue;
          }

          prunedView.getContent().createEdge(source.m_node, target.m_node, source.m_type);

          createdEdges.add(edgePair);
        }
      }
    }
  }

  /**
   * Converts the nodes from the original view to the pruned view.
   *
   * @param view The original view.
   * @param prunedView The pruned view.
   * @param keptInstructions Instructions to add to the new view.
   *
   * @return A mapping between nodes of the original view and nodes of the pruned view.
   */
  private static Map<INaviViewNode, INaviViewNode> convertNodes(final INaviView view,
      final INaviView prunedView, final List<INaviInstruction> keptInstructions) {
    final Map<INaviViewNode, INaviViewNode> nodeMap = new HashMap<INaviViewNode, INaviViewNode>();

    for (final INaviViewNode node : view.getGraph().getNodes()) {
      if (node instanceof INaviCodeNode) {
        final INaviCodeNode cnode = (INaviCodeNode) node;

        final ArrayList<INaviInstruction> newInstructions = Lists.newArrayList(
            cnode.getInstructions());

        newInstructions.retainAll(keptInstructions);

        if (!newInstructions.isEmpty()) {
          CCodeNode newNode;

          try {
            newNode =
                prunedView.getContent().createCodeNode(cnode.getParentFunction(), newInstructions);
          } catch (final MaybeNullException e) {
            newNode = prunedView.getContent().createCodeNode(null, newInstructions);
          }

          newNode.setBorderColor(node.getBorderColor());
          newNode.setColor(node.getColor());

          nodeMap.put(node, newNode);
        }
      } else if (node instanceof INaviFunctionNode) {
        final INaviFunction function = ((INaviFunctionNode) node).getFunction();

        final CFunctionNode newNode = prunedView.getContent().createFunctionNode(function);

        nodeMap.put(node, newNode);
      }
    }

    return nodeMap;
  }

  /**
   * Collects edge information about outgoing edges.
   *
   * @param edge The edge whose information is collected.
   * @param nodeMap Maps between nodes of the old view and nodes of the new view.
   * @param visited Already visited edges.
   *
   * @return The collected edge information.
   */
  private static Set<EdgeResult> getSources(final INaviEdge edge,
      final Map<INaviViewNode, INaviViewNode> nodeMap, final Set<INaviEdge> visited) {
    final INaviViewNode source = edge.getSource();

    visited.add(edge);

    final Set<EdgeResult> sources = new HashSet<EdgeResult>();

    if (nodeMap.containsKey(source)) {
      sources.add(new EdgeResult(nodeMap.get(source), edge.getType()));
    } else {
      for (final INaviEdge incomingEdge : source.getIncomingEdges()) {
        if (!visited.contains(incomingEdge)) {
          sources.addAll(getSources(incomingEdge, nodeMap, visited));
        }
      }
    }

    return sources;
  }

  /**
   * Collects edge information about incoming edges.
   *
   * @param edge The edge whose information is collected.
   * @param nodeMap Maps between nodes of the old view and nodes of the new view.
   * @param visited Already visited edges.
   *
   * @return The collected edge information.
   */
  private static Set<EdgeResult> getTargets(final INaviEdge edge,
      final Map<INaviViewNode, INaviViewNode> nodeMap, final Set<INaviEdge> visited) {
    final INaviViewNode target = edge.getTarget();

    visited.add(edge);

    final Set<EdgeResult> targets = new HashSet<EdgeResult>();

    if (nodeMap.containsKey(target)) {
      targets.add(new EdgeResult(nodeMap.get(target), edge.getType()));
    } else {
      for (final INaviEdge outgoingEdge : target.getOutgoingEdges()) {
        if (!visited.contains(outgoingEdge)) {
          targets.addAll(getTargets(outgoingEdge, nodeMap, visited));
        }
      }
    }

    return targets;
  }

  /**
   * Creates a new view from the results of an operand tracking operation.
   *
   * @param container Container where the new view is created.
   * @param view Input view that provides the data.
   * @param keptInstructions Instructions to add to the new view.
   *
   * @return The new view.
   */
  public static INaviView prune(final IViewContainer container, final INaviView view,
      final List<INaviInstruction> keptInstructions) {
    final INaviView prunedView = container.createView("Pruned View", "");

    final Map<INaviViewNode, INaviViewNode> nodeMap =
        convertNodes(view, prunedView, keptInstructions);

    convertEdges(view, prunedView, nodeMap);

    return prunedView;
  }

  /**
   * Small helper class for storing edge results.
   */
  private static class EdgeResult {
    /**
     * A node.
     */
    public final INaviViewNode m_node;

    /**
     * An edge type.
     */
    public final EdgeType m_type;

    /**
     * Creates a new edge result.
     *
     * @param node A node.
     * @param type An edge type.
     */
    private EdgeResult(final INaviViewNode node, final EdgeType type) {
      m_node = node;
      m_type = type;
    }
  }
}
