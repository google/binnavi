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
package com.google.security.zynamics.binnavi.Gui.Debug.ToolbarPanel.Implementations;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.disassembly.CCodeNodeHelpers;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.INodeCallback;
import com.google.security.zynamics.zylib.types.common.IterationMode;

/**
 * Provides helper functions for stepping over an instruction with the debugger.
 */
public final class CStepOverHelper {
  /**
   * You are not supposed to instantiate this class.
   */
  private CStepOverHelper() {
  }

  /**
   * Adds the addresses of a instructions that follow another instruction to a list when
   * single-stepping in a code node.
   * 
   * @param node The code node where the step over operation happens.
   * @param address The address in question.
   * @param instructions List to extend with the successor instructions.
   */
  private static void stepInCodeNode(final INaviCodeNode node, final UnrelocatedAddress address,
      final Set<BreakpointAddress> instructions) {
    final int instructionIndex = CCodeNodeHelpers.getInstruction(node, address.getAddress());

    if (instructionIndex != -1) {
      // The instruction to search for is in the current basic block ...

      if (instructionIndex < node.instructionCount() - 1) {
        // ... and the basic block is large enough that the following
        // instruction is also part of the basic block.

        final INaviInstruction instruction =
            Iterables.get(node.getInstructions(), instructionIndex + 1);

        instructions.add(new BreakpointAddress(instruction.getModule(), new UnrelocatedAddress(
            instruction.getAddress())));
      } else {
        // ... but the instruction is the last instruction of the basic block,
        // so we have to look into the child nodes of the basic block.

        instructions.addAll(CSteppingHelper.getSuccessors(node));
      }
    }
  }

  /**
   * Adds the addresses of a instructions that follow another instruction to a list when
   * single-stepping in a function node.
   * 
   * @param node The function node where the step over operation happens.
   * @param address The address in question.
   * @param instructions List to extend with the successor instructions.
   */
  private static void stepInFunctionNode(final INaviFunctionNode node,
      final UnrelocatedAddress address, final Set<BreakpointAddress> instructions) {
    if (address.getAddress().equals(node.getFunction().getAddress())) {
      instructions.addAll(CSteppingHelper.getSuccessors(node));
    }
  }

  /**
   * Determines all the instructions that follow the instructions at the given address.
   * 
   * @param graph The graph where the step operation happens.
   * @param address The address to start searching from.
   * 
   * @return The list of instructions that follow the address.
   */
  public static Set<BreakpointAddress> getNextInstructions(final ZyGraph graph,
      final UnrelocatedAddress address) {
    // This function returns a list of instructions because due to the nature
    // of our graphs, the instruction that follows an instruction is not
    // unique. There can be more than one instruction following another instruction
    // because code nodes can be created arbitrarily.

    final Set<BreakpointAddress> instructions = new HashSet<BreakpointAddress>();

    graph.iterate(new INodeCallback<NaviNode>() {
      @Override
      public IterationMode next(final NaviNode node) {
        if (node.getRawNode() instanceof INaviCodeNode) {
          stepInCodeNode((INaviCodeNode) node.getRawNode(), address, instructions);
        } else if (node.getRawNode() instanceof INaviFunctionNode) {
          stepInFunctionNode((INaviFunctionNode) node.getRawNode(), address, instructions);
        }

        return IterationMode.CONTINUE;
      }
    });

    return instructions;
  }
}
