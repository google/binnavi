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
// TODO(thomasdullien): After some reading of this code, it is pretty safe to
// say that the entire logic for un-inlining is extremely brittle and broken;
// this code will need a completely rewrite eventually.

package com.google.security.zynamics.binnavi.disassembly.algorithms;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations.CGraphLayouter;
import com.google.security.zynamics.binnavi.Log.NaviLogger;
import com.google.security.zynamics.binnavi.disassembly.CCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviTextNode;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType;
import com.google.security.zynamics.zylib.types.graphs.GraphAlgorithms;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;


/**
 * Class that contains functions used for uninlining a function in a graph.
 */
public class CUnInliner {
  /**
   * You are not supposed to instantiate this class.
   */
  private CUnInliner() {
  }

  /**
   * Starting from the given node, this function finds the node that is the target node of the
   * LeaveInlined edge that ends the inlined function.
   *
   * @param node The node where the search starts.
   *
   * @return The node that is the target of the LeaveInlined edge or null.
   */
  private static INaviCodeNode getEndNode(final INaviCodeNode node) {
    try {
      return getEndNode(node, node.getParentFunction(), new HashSet<INaviCodeNode>());
    } catch (final MaybeNullException e) {
      return null;
    }
  }

  /**
   * Starting from the given node, this function recursively attempts to find
   * the node that is the target node of the LeaveInlined edge that leads from
   * the inlined function to the parent function (into which the inlining was
   * done).
   *
   * @param node The node where the search starts.
   * @param originalFunction The function to be uninlined.
   * @param visited Keeps track of already visited nodes.
   *
   * @return The node that is the target of the LeaveInlined edge or null.
   */
  private static INaviCodeNode getEndNode(final INaviCodeNode node,
      final INaviFunction originalFunction, final Set<INaviCodeNode> visited) {
    if (visited.contains(node)) {
      return null;
    }

    visited.add(node);

    for (final INaviEdge outgoingEdge : node.getOutgoingEdges()) {
      if (outgoingEdge.getType() == EdgeType.LEAVE_INLINED_FUNCTION) {
        try {
          if (node.getParentFunction() == originalFunction) {
            if (outgoingEdge.getTarget() instanceof INaviCodeNode) {
              return (INaviCodeNode) outgoingEdge.getTarget();
            } else {
              return null;
            }
          }
        } catch (final MaybeNullException e) {
        }
      }
    }

    for (final INaviEdge outgoingEdge : node.getOutgoingEdges()) {
      if (outgoingEdge.getTarget() instanceof INaviCodeNode) {
        final INaviCodeNode endNode =
            getEndNode((INaviCodeNode) outgoingEdge.getTarget(), originalFunction, visited);

        if (endNode != null) {
          return endNode;
        }
      }
    }

    return null;
  }

  /**
   * Returns the parent function of a node.
   *
   * @param node The node whose parent function is returned.
   *
   * @return The parent function of the node or null.
   */
  private static INaviFunction getParentFunction(final INaviCodeNode node) {
    try {
      return node.getParentFunction();
    } catch (final MaybeNullException e) {
      return null;
    }
  }

  /**
   * Starting from the given node, this function finds the node that is the source node of the
   * EnterInlined edge that starts the inlined function.
   *
   * @param node The node where the search starts.
   *
   * @return The node that is the source of the EnterInlined edge or null.
   */
  private static INaviCodeNode searchForSourceNodeOfEnterInlinedEdge(final INaviCodeNode node) {
    try {
      return searchForSourceNodeOfEnterInlinedEdge(node, node.getParentFunction(), new HashSet<INaviCodeNode>());
    } catch (final MaybeNullException e) {
      return null;
    }
  }

  /**
   * Starting from the given node, this function iterates over all incoming
   * edges of the node and checks if any of these edges is of type
   * ENTER_INLINED_FUNCTION. If this is the case, it returns the source node
   * of this edge. The purpose of this is to return the node "from which" the
   * node under consideration was inlined.
   *
   * If none of the incoming edges of the current node are of the type
   * ENTER_INLINED_FUNCTION, recurse into the parent nodes until we find the
   * relevant node.
   *
   * This function may run out of Java stack space on very deeply inlined
   * flowgraphs; we might wish to consider rewriting it in non-recursive. The
   * function is also prone to break unpredictably in functions that have in-
   * lined the same function multiple times and contain a loop. Pretty much
   * the entire uninline-logic needs to be rewritten eventually.
   *
   * @param node The node where the search starts.
   * @param originalFunction The function to uninline.
   * @param visited The visited nodes.
   *
   * @return The node that is the source of the EnterInlined edge or null.
   */
  private static INaviCodeNode searchForSourceNodeOfEnterInlinedEdge(final INaviCodeNode node,
      final INaviFunction originalFunction, final Set<INaviCodeNode> visited) {
    if (visited.contains(node)) {
      return null;
    }

    visited.add(node);

    // Check all incoming edges if one is ENTER_INLINED_FUNCTION.
    for (final INaviEdge incomingEdge : node.getIncomingEdges()) {
      if (incomingEdge.getType() == EdgeType.ENTER_INLINED_FUNCTION) {
        if (incomingEdge.getSource() instanceof INaviCodeNode) {
          return (INaviCodeNode) incomingEdge.getSource();
        } else {
          return null;
        }
      }
    }

    // No proper edge found, recurse into the parents.
    for (final INaviEdge incomingEdge : node.getIncomingEdges()) {
      if (incomingEdge.getSource() instanceof INaviCodeNode) {
        final INaviCodeNode startNode =
            searchForSourceNodeOfEnterInlinedEdge((INaviCodeNode) incomingEdge.getSource(), originalFunction, visited);
        if (startNode != null) {
          return startNode;
        }
      }
    }

    return null;
  }

  /**
   * Determines whether two given code nodes have different parent functions.
   *
   * @param firstNode The first node.
   * @param secondNode The second node.
   *
   * @return True, if the nodes have different parent functions. False, otherwise.
   */
  private static boolean hasDifferentParentFunctions(final INaviCodeNode firstNode,
      final INaviCodeNode secondNode) {
    try {
      final INaviFunction startFunction = firstNode.getParentFunction();

      try {
        final INaviFunction endFunction = secondNode.getParentFunction();

        return !startFunction.equals(endFunction);
      } catch (final MaybeNullException e) {
        return true;
      }
    } catch (final MaybeNullException e) {
      try {
        secondNode.getParentFunction();

        return true;
      } catch (final MaybeNullException e1) {
        return false;
      }
    }
  }

  /**
   * Removes text nodes from the deleted nodes.
   *
   * @param view The view from which the nodes are deleted.
   * @param nodes The nodes whose attached text nodes are removed from.
   */
  private static void removeTextNodes(final INaviView view, final ImmutableList<INaviViewNode> nodes) {
    final Set<INaviViewNode> toDelete = new HashSet<INaviViewNode>();

    for (final INaviViewNode node : nodes) {
      for (final INaviViewNode parent : node.getParents()) {
        if (parent instanceof INaviTextNode) {
          toDelete.add(parent);
        }
      }
    }

    view.getContent().deleteNodes(toDelete);
  }

  /**
   * Determines inlining information starting with a given node.
   * TODO(thomasdullien): This code is pretty thoroughly broken for any form of
   * nested inlining and needs to be rewritten.
   *
   * @param node The start node.
   *
   * @return Information about the inlining boundaries.
   */
  public static CInlinedNodes getInlinedNodes(final INaviCodeNode node) {
    Preconditions.checkNotNull(node, "IE02750: node argument can not be null");
    // Performs a recursive backward search upward in the graph until it finds
    // an 'enter inlined function' edge, then returns it's source node. This
    // will usually be "a function layer up" from the function that is about to
    // be uninlined. Please note that if, while searching up, it enters a
    // different inlined subfunction, this function will return a wrong node.
    final INaviCodeNode startNode = searchForSourceNodeOfEnterInlinedEdge(node);
    // Performs a recursive forward search downward in the graph until it finds
    // a 'leave inlined function' edge whose source node is in the same function
    // as 'node'. Returns the target of this edge. Please note that if, while
    // searching down a second nested inlined version of the current function is
    // found, this code will return the wrong node.
    final INaviCodeNode endNode = getEndNode(node);

    if (startNode == null || endNode == null) {
      return null;
    }

    // The following check used to throw an exception when the above broken code
    // misbehaved, rendering things inconsistent thereafter. It is much better
    // to simply carry on for now until the above functions can be fixed.
    if (hasDifferentParentFunctions(startNode, endNode)) {
      NaviLogger.info("Uninlining yielded almost certainly incorrect results.");
    }

    final Set<INaviViewNode> preds = GraphAlgorithms.getPredecessorsUpToNode(endNode, startNode);
    final Set<INaviViewNode> succs = GraphAlgorithms.getSuccessorsDownToNode(startNode, endNode);

    preds.retainAll(succs);

    return new CInlinedNodes(startNode, endNode, ImmutableList.copyOf(preds));
  }

  /**
   * Uninlines the inlined function a given node belongs to.
   *
   * @param view The view where the uninline operation takes place.
   * @param node The start node.
   *
   * @return True, if the operation was successful. False, if it was not.
   */
  public static boolean unInline(final INaviView view, final INaviCodeNode node) {
    try {
      final CInlinedNodes inlinedNodes = getInlinedNodes(node);
      if (inlinedNodes == null) {
        return false;
      }

      final List<INaviInstruction> instructions =
          Lists.newArrayList(inlinedNodes.getStartNode().getInstructions());

      final boolean mergeBlocks = inlinedNodes.getEndNode().getIncomingEdges().size() == 1;

      if (mergeBlocks) {
        instructions.addAll(Lists.newArrayList(inlinedNodes.getEndNode().getInstructions()));
      }

      final CCodeNode combinedNode =
          view.getContent().createCodeNode(getParentFunction(inlinedNodes.getStartNode()),
              instructions);

      combinedNode.setColor(inlinedNodes.getStartNode().getColor());
      combinedNode.setBorderColor(inlinedNodes.getStartNode().getBorderColor());

      removeTextNodes(view, inlinedNodes.getInlinedNodes());

      view.getContent().deleteNodes(inlinedNodes.getInlinedNodes());

      for (final INaviEdge incomingEdge : inlinedNodes.getStartNode().getIncomingEdges()) {
        view.getContent()
            .createEdge(incomingEdge.getSource(), combinedNode, incomingEdge.getType());
      }

      if (mergeBlocks) {
        for (final INaviEdge outgoingEdge : inlinedNodes.getEndNode().getOutgoingEdges()) {
          view.getContent().createEdge(combinedNode, outgoingEdge.getTarget(),
              outgoingEdge.getType());
        }
      } else {
        view.getContent().createEdge(combinedNode, inlinedNodes.getEndNode(),
            EdgeType.JUMP_UNCONDITIONAL);
      }

      view.getContent().deleteNode(inlinedNodes.getStartNode());

      if (mergeBlocks) {
        view.getContent().deleteNode(inlinedNodes.getEndNode());
      }

      return true;
    } catch (final IllegalStateException exception) {
      CUtilityFunctions.logException(exception);
      return false;
    }
  }

  /**
   * Uninlines the function a given node belongs to.
   *
   * @param parent Parent window used for dialogs.
   * @param graph Graph where the uninline operation takes place.
   * @param node The start node.
   */
  public static void unInline(final JFrame parent, final ZyGraph graph, final INaviCodeNode node) {
    unInline(graph.getRawView(), node);

    if (graph.getSettings().getLayoutSettings().getAutomaticLayouting()) {
      CGraphLayouter.refreshLayout(parent, graph);
    }
  }

  /**
   * Encapsulates function inlining boundaries.
   */
  private static class CInlinedNodes {
    /**
     * Start node with the outgoing EnterInlined edge.
     */
    private final INaviCodeNode m_startNode;

    /**
     * End node with the incoming LeaveInlined node.
     */
    private final INaviCodeNode m_endNode;

    /**
     * Inlined nodes to be removed.
     */
    private final ImmutableList<INaviViewNode> m_inlinedNodes;

    /**
     * Creates a new inlined nodes object.
     *
     * @param startNode Start node with the outgoing EnterInlined edge.
     * @param endNode End node with the incoming LeaveInlined node.
     * @param inlinedNodes Inlined nodes to be removed.
     */
    private CInlinedNodes(final INaviCodeNode startNode, final INaviCodeNode endNode,
        final ImmutableList<INaviViewNode> inlinedNodes) {
      m_startNode =
          Preconditions.checkNotNull(startNode, "IE02752: startNode argument can not be null");
      m_endNode = Preconditions.checkNotNull(endNode, "IE02753: endNode argument can not be null");
      m_inlinedNodes =
          Preconditions.checkNotNull(inlinedNodes, "IE02754: inlinedNodes argument can not be null");
    }

    /**
     * Returns the end node that has the incoming LeaveInlined edge.
     *
     * @return The end node.
     */
    public INaviCodeNode getEndNode() {
      return m_endNode;
    }

    /**
     * Returns the inlined nodes to be removed.
     *
     * @return The inlined nodes to be removed.
     */
    public ImmutableList<INaviViewNode> getInlinedNodes() {
      return m_inlinedNodes;
    }

    /**
     * Returns the start node that has the outgoing EnterInlined edge.
     *
     * @return The start node.
     */
    public INaviCodeNode getStartNode() {
      return m_startNode;
    }
  }
}
