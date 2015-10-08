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
package com.google.security.zynamics.reil.algorithms.mono2.common.instructiongraph.interfaces;


/**
 * The IInstructionGraph interface which is to be implemented for a sane implementation of an
 * instruction graph.
 */
public interface IInstructionGraph {
  /**
   * Returns the destination of the instruction graph edge.
   * 
   * @param edge The instruction graph edge where the destination node is determined.
   * @return The destination node of the instruction graph edge.
   */
  public IInstructionGraphNode getDestination(IInstructionGraphEdge edge);

  /**
   * Returns the entry node of the instruction graph.
   * 
   * @return The entry node of the instruction graph.
   */
  public IInstructionGraphNode getEntryNode();

  /**
   * Returns the exit node of the instruction graph.
   * 
   * @return The exit node of the instruction graph.
   */
  public IInstructionGraphNode getExitNode();

  /**
   * Returns the incoming edges of an instruction graph node.
   * 
   * @param node The instruction graph node to which the edges point to.
   * @return The edges incoming to the the instruction graph node.
   */
  public Iterable<IInstructionGraphEdge> getIncomingEdges(IInstructionGraphNode node);

  /**
   * Returns the outgoing edges of an instruction graph node.
   * 
   * @param node The instruction graph node from which the edges originate.
   * @return All edges outgoing from the instruction graph node.
   */
  public Iterable<IInstructionGraphEdge> getOutgoingEdges(IInstructionGraphNode node);

  /**
   * Returns the source node of the instruction graph edge.
   * 
   * @param edge The instruction graph edge where the source node is determined.
   * @return The source node of the instruction graph edge.
   */
  public IInstructionGraphNode getSource(IInstructionGraphEdge edge);
  
  /**
   * Returns the number of edges in this graph.
   * 
   * @return The number of edges in this graph.
   */
  public int size();
}
