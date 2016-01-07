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
package com.google.security.zynamics.binnavi.API.disassembly;

import com.google.common.base.Preconditions;

// ! Offers convenience functions for working with flow graphs.
/**
 * Offers convenience functions for working with flow graphs. Please note that many convenience
 * functions are just straight-forward implementations of commonly used algorithms and therefore can
 * have significant runtime costs.
 */
public final class FlowGraphHelpers {
  /**
   * You are not supposed to instantiate this class.
   */
  private FlowGraphHelpers() {
  }

  // ! Finds an instruction with a given address.
  /**
   * Returns the instruction of a flow graph that starts at a given address. Since addresses do not
   * uniquely identify instructions it is possible that there is more than one instruction with the
   * given address in the graph. In case of multiple instructions that start at the given address it
   * is undefined exactly which of those instructions is returned.
   * 
   * This function is guaranteed to work in O(m + n) where m is the number of nodes in the graph and
   * n is the number of instructions in the graph.
   * 
   * @param flowgraph The graph to search through.
   * @param address The address to search for.
   * 
   * @return The instruction that starts at the given address or null if there is no such
   *         instruction.
   */
  public static Instruction getInstruction(final FlowGraph flowgraph, final Address address) {
    Preconditions.checkNotNull(flowgraph, "Error: Graph argument can not be null");
    Preconditions.checkNotNull(address, "Error: Address argument can not be null");

    for (final BasicBlock node : flowgraph) {
      for (final Instruction instruction : node.getInstructions()) {
        if (instruction.getAddress().equals(address)) {
          return instruction;
        }
      }
    }

    return null;
  }

  // ! Finds an instruction with a given address.
  /**
   * Returns the instruction of a Flow graph that starts at a given address. Since addresses do not
   * uniquely identify instructions it is possible that there is more than one instruction with the
   * given address in the graph. In case of multiple instructions that start at the given address it
   * is undefined exactly which of those instructions is returned.
   * 
   * This function is guaranteed to work in O(m + n) where m is the number of nodes in the graph and
   * n is the number of instructions in the graph.
   * 
   * @param graph The graph to search through.
   * @param address The address to search for.
   * 
   * @return The instruction that starts at the given address or null if there is no such
   *         instruction.
   */
  public static Instruction getInstruction(final FlowGraph graph, final long address) {
    return getInstruction(graph, new Address(address));
  }
}
