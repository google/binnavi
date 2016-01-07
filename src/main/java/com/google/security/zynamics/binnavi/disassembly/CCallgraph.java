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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.security.zynamics.zylib.types.graphs.DirectedGraph;

/**
 * Represents the Call graph of a module.
 */
public final class CCallgraph extends DirectedGraph<ICallgraphNode, ICallgraphEdge> {
  /**
   * Cache that maps what functions are represented by what node.
   */
  private ImmutableMap<INaviFunction, ICallgraphNode> m_functionMap;

  /**
   * Creates a new Call graph object.
   * 
   * @param nodes The nodes of the Call graph.
   * @param edges The edges of the Call graph.
   */
  public CCallgraph(final List<ICallgraphNode> nodes, final List<ICallgraphEdge> edges) {
    super(nodes, edges);

    m_functionMap = Maps.uniqueIndex(nodes, new Function<ICallgraphNode, INaviFunction>() {
      @Override
      public INaviFunction apply(final ICallgraphNode input) {
        return input.getFunction();
      }
    });
  }

  /**
   * Determines what functions are represented by a list of Call graph nodes.
   * 
   * @param nodes The list of nodes.
   * 
   * @return The functions represented by the given nodes.
   */
  private Set<INaviFunction> getFunctions(final Iterable<? extends ICallgraphNode> nodes) {
    final Set<INaviFunction> functions = new HashSet<INaviFunction>();

    for (final ICallgraphNode callgraphNode : nodes) {
      functions.add(callgraphNode.getFunction());
    }

    return functions;
  }

  /**
   * Returns the functions that call a given function.
   * 
   * @param calledFunction The function whose callers are returned.
   * 
   * @return The callers of the given functions.
   */
  public Set<INaviFunction> getCallers(final INaviFunction calledFunction) {
    final ICallgraphNode node = m_functionMap.get(calledFunction);

    return getFunctions(node.getParents());
  }
}
