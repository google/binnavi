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
package com.google.security.zynamics.zylib.gml;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.types.graphs.IDirectedGraph;
import com.google.security.zynamics.zylib.types.graphs.IGraphEdge;
import com.google.security.zynamics.zylib.types.trees.ITree;
import com.google.security.zynamics.zylib.types.trees.ITreeNode;

/**
 * This class can be used to generate GML files from input objects.
 */
public final class GmlConverter {
  /**
   * Creates GML code that represents a given directed graph.
   * 
   * @param graph The input graph.
   * 
   * @return The code generated for the input graph.
   */
  public static String toGml(final IDirectedGraph<?, ? extends IGraphEdge<?>> graph) {
    Preconditions.checkNotNull(graph, "Graph argument can not be null");

    final StringBuilder sb = new StringBuilder();

    sb.append("graph\n" + "[\n");

    int currentId = 0;

    final Map<Object, Integer> nodeMap = new HashMap<>();

    for (final Object node : graph.getNodes()) {
      sb.append("\tnode\n" + "\t[\n" + "\tid " + "\n");
      sb.append(currentId);
      sb.append("\tlabel \"");
      sb.append(node);
      sb.append("\"\n" + "\t]\n");

      nodeMap.put(node, currentId);

      ++currentId;
    }

    for (final IGraphEdge<?> edge : graph.getEdges()) {
      sb.append("\tedge\n" + "\t[\n" + "\tsource ");
      sb.append(nodeMap.get(edge.getSource()));
      sb.append("\n" + "\ttarget ");
      sb.append(nodeMap.get(edge.getTarget()));
      sb.append("\n" + "\tgraphics\n" + "\t[\n" + "\t\tfill \"#000000\"\n"
        + "\t\ttargetArrow \"standard\"\n" + "\t]\n" + "\t]\n");
    }

    sb.append("]\n");

    return sb.toString();
  }

  public static String toGml(final ITree<?> graph) {
    Preconditions.checkNotNull(graph, "Graph argument can not be null");

    final StringBuilder sb = new StringBuilder();

    sb.append("graph\n" + "[\n");

    int currentId = 0;

    final Map<Object, Integer> nodeMap = new HashMap<>();

    final Stack<ITreeNode<?>> stack = new Stack<>();

    stack.push(graph.getRootNode());

    while (!stack.isEmpty()) {
      final ITreeNode<?> node = stack.pop();

      sb.append("\tnode\n" + "\t[\n" + "\tid ");
      sb.append(currentId);
      sb.append("\n" + "\tlabel \"" + "\"\n");
      sb.append(node);
      sb.append("\t]\n");

      nodeMap.put(node, currentId);

      ++currentId;

      final ITreeNode<?> parent = node.getParent();

      if (parent != null) {
        sb.append("\tedge\n" + "\t[\n" + "\tsource ");
        sb.append(nodeMap.get(parent));
        sb.append("\n" + "\ttarget ");
        sb.append(nodeMap.get(node));
        sb.append("\n" + "\tgraphics\n" + "\t[\n" + "\t\tfill \"#000000\"\n" 
          + "\t\ttargetArrow \"standard\"\n" + "\t]\n" + "\t]\n");
      }

      for (final ITreeNode<?> treeNode : node.getChildren()) {
        stack.push(treeNode);
      }
    }

    sb.append("]\n");

    return sb.toString();
  }
}
