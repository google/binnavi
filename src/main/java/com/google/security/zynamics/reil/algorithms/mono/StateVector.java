/*
Copyright 2015 Google Inc. All Rights Reserved.

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
package com.google.security.zynamics.reil.algorithms.mono;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.reil.algorithms.mono.interfaces.ILatticeElementMono1;

/**
 * Used to keep track of the currently known program state during a run of the monotone framework.
 * 
 * @param <LatticeElement> Type of the elements in the lattice.
 */
public final class StateVector<GraphNode, LatticeElement extends ILatticeElementMono1<LatticeElement>>
    implements IStateVector<GraphNode, LatticeElement>, Iterable<GraphNode> {
  /**
   * Keeps track of the current states of all nodes.
   */
  private final Map<GraphNode, LatticeElement> mapping = new LinkedHashMap<>();

  /**
   * Returns the state of a node.
   * 
   * @param node The node in question.
   * 
   * @return The state of the node.
   */
  @Override
  public LatticeElement getState(final GraphNode node) {
    return mapping.get(Preconditions.checkNotNull(node, "Error: node argument can not be null"));
  }

  /**
   * Determines whether a given node has a known state.
   * 
   * @param node The node in question.
   * 
   * @return True, if the node has a known state. False, otherwise.
   */
  @Override
  public boolean hasState(final GraphNode node) {
    return mapping.containsKey(Preconditions.checkNotNull(node,
        "Error: node argument can not be null"));
  }

  @Override
  public Iterator<GraphNode> iterator() {
    return mapping.keySet().iterator();
  }

  /**
   * Sets the state of a node.
   * 
   * @param node The node in question.
   * @param element The new state of the node.
   */
  @Override
  public void setState(final GraphNode node, final LatticeElement element) {
    Preconditions.checkNotNull(node, "Error: node argument can not be null");
    Preconditions.checkNotNull(element, "Error: element argument can not be null");
    mapping.put(node, element);
  }

  /**
   * Returns the number of elements in the vector.
   * 
   * @return The number of elements in the vector.
   */
  @Override
  public int size() {
    return mapping.size();
  }

  @Override
  public String toString() {

    return mapping.entrySet()
            .stream()
            .map(entry -> entry.getKey() + " -> " + entry.getValue())
            .collect(Collectors.joining("\n", "[\n", "\n]"));
  }
}
