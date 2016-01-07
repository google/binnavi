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
package com.google.security.zynamics.binnavi.disassembly.algorithms;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.disassembly.CCodeNode;
import com.google.security.zynamics.binnavi.disassembly.CFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.CGroupNode;
import com.google.security.zynamics.binnavi.disassembly.CNaviViewEdge;
import com.google.security.zynamics.binnavi.disassembly.CNaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviGroupNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviTextNode;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.gui.zygraph.edges.CBend;
import com.google.security.zynamics.zylib.types.common.CollectionHelpers;
import com.google.security.zynamics.zylib.types.common.ICollectionMapper;
import com.google.security.zynamics.zylib.types.graphs.IDirectedGraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class used to insert one view into another view.
 */
public final class CViewInserter {
  /**
   * Do not instantiate this class; use {@link #insertView} instead.
   */
  private CViewInserter() {
    // You are not supposed to instantiate this class
  }

  /**
   * Clones the edges of the source view and inserts them into the target view.
   *
   * @param target The target view where the cloned edges are inserted.
   * @param edges The source edges that are cloned.
   * @param nodeMap Maps between the source nodes and their cloned counterparts.
   */
  private static void createEdges(final INaviView target, final List<INaviEdge> edges,
      final Map<INaviViewNode, INaviViewNode> nodeMap) {
    for (final INaviEdge edge : edges) {
      final INaviViewNode sourceNode = nodeMap.get(edge.getSource());
      final INaviViewNode targetNode = nodeMap.get(edge.getTarget());

      final CNaviViewEdge newEdge =
          target.getContent().createEdge(sourceNode, targetNode, edge.getType());

      newEdge.setColor(edge.getColor());
      newEdge.setX1(edge.getX1());
      newEdge.setY1(edge.getY1());
      newEdge.setX2(edge.getX2());
      newEdge.setY2(edge.getY2());

      for (final CBend bend : edge.getBends()) {
        newEdge.addBend(bend.getX(), bend.getY());
      }
    }
  }

  /**
   * Clones the group nodes of the source view and inserts them into the target view.
   *
   * @param target The target view where the cloned group nodes are inserted.
   * @param sourceNodes The nodes of the source view.
   * @param nodeMap Maps between the source nodes and their cloned counterparts.
   */
  private static void createGroupNodes(final INaviView target,
      final Collection<INaviViewNode> sourceNodes,
      final Map<INaviViewNode, INaviViewNode> nodeMap) {
    for (final INaviViewNode blockNode : sourceNodes) {
      if (blockNode instanceof INaviGroupNode) {
        final INaviGroupNode gnode = (INaviGroupNode) blockNode;

        final CGroupNode newGroupNode =
            target.getContent().createGroupNode(getNodes(gnode.getElements(), nodeMap));

        newGroupNode.initializeComment(gnode.getComments());
      }
    }
  }

  /**
   * Clones a list of source nodes and inserts them into the target view.
   *
   * @param target The target view where the cloned nodes are inserted.
   * @param nodes The source nodes to be cloned and inserted into the target view.
   *
   * @return A map that maps between the source nodes and their cloned counterparts.
   */
  private static Map<INaviViewNode, INaviViewNode> createNodes(
      final INaviView target, final Collection<INaviViewNode> nodes) {
    final HashMap<INaviViewNode, INaviViewNode> map = new HashMap<INaviViewNode, INaviViewNode>();

    for (final INaviViewNode blockNode : nodes) {
      createNodes(target, blockNode, map);
    }

    return map;
  }

  /**
   * Clones a node of the source view and inserts it into the target view.
   *
   * @param target The target view where the cloned view is inserted.
   * @param sourceNode The source node that is cloned and inserted into the target view.
   * @param nodeMap Maps nodes of the source view to their cloned counterparts.
   */
  private static void createNodes(final INaviView target, final INaviViewNode sourceNode,
      final Map<INaviViewNode, INaviViewNode> nodeMap) {
    final INaviViewNode newNode =
        CNodeTypeSwitcher.switchNode(sourceNode, new INodeTypeCallback<INaviViewNode>() {
          @Override
          public INaviViewNode handle(final INaviCodeNode node) {
            return insertCodeNode(target, node);
          }

          @Override
          public INaviViewNode handle(final INaviFunctionNode node) {
            return insertFunctionNode(target, node);
          }

          @Override
          public INaviViewNode handle(final INaviGroupNode node) {
            // Skip now, create later
            return null;
          }

          @Override
          public INaviViewNode handle(final INaviTextNode node) {
            return insertTextNode(target, node);
          }
        });

    if (newNode != null) {
      newNode.setBorderColor(sourceNode.getBorderColor());
      newNode.setColor(sourceNode.getColor());
      newNode.setHeight(sourceNode.getHeight());
      newNode.setSelected(sourceNode.isSelected());
      newNode.setVisible(sourceNode.isVisible());
      newNode.setWidth(sourceNode.getWidth());
      newNode.setX(sourceNode.getX());
      newNode.setY(sourceNode.getY());

      nodeMap.put(sourceNode, newNode);
    }
  }

  /**
   * Maps a list of source nodes to their cloned counterparts.
   *
   * @param sourceNodes The list of source nodes to look up.
   * @param nodeMap Maps nodes of the source view to their cloned counterparts.
   *
   * @return The cloned nodes that correspond to the source nodes.
   */
  private static Collection<INaviViewNode> getNodes(
      final List<INaviViewNode> sourceNodes, final Map<INaviViewNode, INaviViewNode> nodeMap) {
    return CollectionHelpers.map(
        sourceNodes, new ICollectionMapper<INaviViewNode, INaviViewNode>() {
          @Override
          public INaviViewNode map(final INaviViewNode item) {
            return nodeMap.get(item);
          }
        });
  }

  /**
   * Clones a source node and inserts the cloned node into the target view.
   *
   * @param target The target view where the node is inserted.
   * @param node Node from the source view that is cloned.
   *
   * @return The cloned code node.
   */
  private static INaviCodeNode insertCodeNode(final INaviView target, final INaviCodeNode node) {
    // TODO: cloning the node is a bad solution since this just fixes the symptoms: instructions are
    // closed two times
    final INaviCodeNode sourceNode = (INaviCodeNode) node.cloneNode();
    final Iterable<INaviInstruction> instructions = sourceNode.getInstructions();
    final ArrayList<INaviInstruction> instructionList = Lists.newArrayList(instructions);

    CCodeNode codeNode;

    try {
      codeNode =
          target.getContent().createCodeNode(sourceNode.getParentFunction(), instructionList);
    } catch (final MaybeNullException e) {
      codeNode = target.getContent().createCodeNode(null, instructionList);
    }

    if (sourceNode.getComments().getGlobalCodeNodeComment() != null) {
      codeNode.getComments()
          .initializeGlobalCodeNodeComment(sourceNode.getComments().getGlobalCodeNodeComment());
    }
    if (sourceNode.getComments().getLocalCodeNodeComment() != null) {
      codeNode.getComments()
          .initializeLocalCodeNodeComment(sourceNode.getComments().getLocalCodeNodeComment());
    }

    final Iterable<INaviInstruction> newInstructions = codeNode.getInstructions();
    for (int i = 0; i < Iterables.size(instructions); i++) {
      codeNode.getComments().initializeLocalInstructionComment(Iterables.get(newInstructions, i),
          sourceNode.getComments().getLocalInstructionComment(Iterables.get(instructions, i)));
    }

    return codeNode;
  }

  /**
   * Clones a source function node and inserts the cloned node into the target view.
   *
   * @param target The target view where the node is inserted.
   * @param sourceNode Node from the source view that is cloned.
   *
   * @return The cloned code node.
   */
  private static CNaviViewNode insertFunctionNode(
      final INaviView target, final INaviFunctionNode sourceNode) {
    final CFunctionNode node = target.getContent().createFunctionNode(sourceNode.getFunction());

    if (sourceNode.getLocalFunctionComment() != null) {
      node.initializeLocalFunctionComment(sourceNode.getLocalFunctionComment());
    }

    return node;
  }

  /**
   * Clones a source text node and inserts the cloned node into the target view.
   *
   * @param target The target view where the node is inserted.
   * @param sourceNode Node from the source view that is cloned.
   *
   * @return The cloned code node.
   */
  private static CNaviViewNode insertTextNode(
      final INaviView target, final INaviTextNode sourceNode) {
    return target.getContent().createTextNode(sourceNode.getComments());
  }

  /**
   * Inserts the source view into the target view.
   *
   * @param source The view to insert into the target view.
   * @param target The target view where the source view is inserted.
   */
  public static void insertView(final INaviView source, final INaviView target) {
    Preconditions.checkNotNull(source, "IE00007: Source argument can not be null");
    Preconditions.checkNotNull(target, "IE00020: Target argument can not be null");

    Preconditions.checkArgument(source.isLoaded(), "IE00974: Source view must be loaded");
    Preconditions.checkArgument(target.isLoaded(), "IE00978: Target view must be loaded");

    final IDirectedGraph<INaviViewNode, INaviEdge> graph = source.getGraph();

    final Map<INaviViewNode, INaviViewNode> map = createNodes(target, graph.getNodes());

    createEdges(target, graph.getEdges(), map);

    createGroupNodes(target, graph.getNodes(), map);
  }
}
