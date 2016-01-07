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
package com.google.security.zynamics.binnavi.API.reil.mono;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Preconditions;

// ! Default state vector that can be used in MonoREIL.
/**
 * Used to keep track of the currently known program state during a run of the monotone framework.
 * 
 * @param <GraphNode> Type of the nodes in the walked graph.
 * @param <LatticeElement> Type of the elements in the lattice.
 */
public class DefaultStateVector<GraphNode, LatticeElement extends ILatticeElement<LatticeElement>>
    implements IStateVector<GraphNode, LatticeElement>, Iterable<GraphNode> {
  /**
   * Keeps track of the current states of all nodes.
   */
  private final Map<GraphNode, LatticeElement> mapping = new HashMap<GraphNode, LatticeElement>();

  @Override
  public final LatticeElement getState(final GraphNode node) {
    Preconditions.checkNotNull(node, "Error: node argument can not be null");
    return mapping.get(node);
  }

  @Override
  public final boolean hasState(final GraphNode node) {
    Preconditions.checkNotNull(node, "Error: node argument can not be null");
    return mapping.containsKey(node);
  }

  @Override
  public final Iterator<GraphNode> iterator() {
    return mapping.keySet().iterator();
  }

  @Override
  public final void setState(final GraphNode node, final LatticeElement element) {
    Preconditions.checkNotNull(node, "Error: node argument can not be null");
    Preconditions.checkNotNull(element, "Error: element argument can not be null");
    mapping.put(node, element);
  }

  @Override
  public final int size() {
    return mapping.size();
  }

  // ! Printable representation of the state vector.
  /**
   * Returns a string representation of the state vector.
   * 
   * @return A string representation of the state vector.
   */
  @Override
  public String toString() {
    final StringBuilder stringBuilder = new StringBuilder();

    stringBuilder.append("[\n");

    for (final Entry<GraphNode, LatticeElement> entry : mapping.entrySet()) {
      stringBuilder.append(entry.getKey() + " -> " + entry.getValue());
      stringBuilder.append('\n');
    }

    stringBuilder.append(']');

    return stringBuilder.toString();
  }
}
