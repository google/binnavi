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
package com.google.security.zynamics.binnavi.disassembly;

import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.reil.ReilFunction;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.zylib.disassembly.GraphType;
import com.google.security.zynamics.zylib.disassembly.ICodeEdge;
import com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType;
import com.google.security.zynamics.zylib.types.graphs.IDirectedGraph;

import java.util.Collection;
import java.util.List;

/**
 * Interface for all objects that want to serve as view content objects.
 */
public interface IViewContent {
  /**
   * Creates a new code node in the view.
   *
   * @param parentFunction Parent function of the code node. This argument can be null.
   * @param instructions Instructions of the code node.
   *
   * @return The created code node.
   */
  CCodeNode createCodeNode(INaviFunction parentFunction,
      final List<? extends INaviInstruction> instructions);

  /**
   * Creates a new edge in the view.
   *
   * @param source Source node of the edge.
   * @param target Target node of the edge-
   * @param edgeType Type of the edge.
   *
   * @return The created edge.
   */
  CNaviViewEdge createEdge(final INaviViewNode source, final INaviViewNode target,
      final EdgeType edgeType);

  /**
   * Creates a function node in the view.
   *
   * @param function The function represented by the function node.
   *
   * @return The created function node.
   */
  CFunctionNode createFunctionNode(INaviFunction function);

  /**
   * Creates a new group node in the view.
   *
   * @param nodes The elements of the group node.
   *
   * @return The created group node.
   */
  CGroupNode createGroupNode(Collection<INaviViewNode> nodes);

  /**
   * Creates a new text node in the view.
   *
   * @param comments The comments shown in the text node.
   *
   * @return The created text node.
   */
  CTextNode createTextNode(List<IComment> comments);

  /**
   * Deletes an edge from the view.
   *
   * @param edge The edge to delete.
   */
  void deleteEdge(INaviEdge edge);

  /**
   * Deletes a node from the view.
   *
   * @param node The node to delete.
   */
  void deleteNode(INaviViewNode node);

  /**
   * Deletes nodes from the view.
   *
   * @param nodes The nodes to delete.
   */
  void deleteNodes(Collection<INaviViewNode> nodes);

  /**
   * Returns the code edges of the view.
   *
   * @return The code edges of the view.
   */
  List<? extends ICodeEdge<?>> getBasicBlockEdges();

  /**
   * Returns the basic blocks of the view.
   *
   * @return The basic blocks of the view.
   */
  List<CCodeNode> getBasicBlocks();

  /**
   * Returns the number of edges in the view.
   *
   * @return The number of edges in the view.
   */
  int getEdgeCount();

  /**
   * Returns the graph of the view.
   *
   * @return The graph of the view.
   */
  IDirectedGraph<INaviViewNode, INaviEdge> getGraph();

  /**
   * Returns the graph type of the view.
   *
   * @return The graph type of the view.
   */
  GraphType getGraphType();

  /**
   * Returns the number of nodes in the view.
   *
   * @return The number of nodes in the view.
   */
  int getNodeCount();

  /**
   * Returns the REIL code of the view.
   *
   * @return The REIL code of the view.
   *
   * @throws InternalTranslationException Thrown if the REIL code could not be created.
   */
  ReilFunction getReilCode() throws InternalTranslationException;

}
