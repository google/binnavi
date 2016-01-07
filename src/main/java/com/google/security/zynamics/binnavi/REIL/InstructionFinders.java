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
package com.google.security.zynamics.binnavi.REIL;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.API.disassembly.BasicBlock;
import com.google.security.zynamics.binnavi.API.disassembly.CodeNode;
import com.google.security.zynamics.binnavi.API.disassembly.Function;
import com.google.security.zynamics.binnavi.API.disassembly.Instruction;
import com.google.security.zynamics.binnavi.API.disassembly.View;
import com.google.security.zynamics.binnavi.API.disassembly.ViewNode;
import com.google.security.zynamics.zylib.disassembly.IInstruction;

/**
 * Contains helper classes to search for an instruction in a list of instructions.
 */
public final class InstructionFinders {
  /**
   * You are not supposed to instantiate this class.
   */
  private InstructionFinders() {}

  /**
   * Searches for an instruction in a basic block.
   *
   * @param block The basic block to search through.
   * @param searchInstruction The instruction to search for.
   *
   * @return The API instruction object that wraps the search instruction.
   */
  public static Instruction findInstruction(final BasicBlock block,
      final IInstruction searchInstruction) {
    Preconditions.checkNotNull(block, "IE02005: Block argument can not be null");

    Preconditions.checkNotNull(searchInstruction, "IE02012: Instruction argument can not be null");

    for (final Instruction instruction : block.getInstructions()) {
      if (instruction.getNative() == searchInstruction) {
        return instruction;
      }
    }

    throw new IllegalStateException(
        "IE01272: Could not determine what instruction could not be translated");
  }

  /**
   * Searches for an instruction in a code node.
   *
   * @param codeNode The code node to search through.
   * @param searchInstruction The instruction to search for.
   *
   * @return The API instruction object that wraps the search instruction.
   */
  public static Instruction findInstruction(final CodeNode codeNode,
      final IInstruction searchInstruction) {
    Preconditions.checkNotNull(codeNode, "IE02027: Code node argument can not be null");

    Preconditions.checkNotNull(searchInstruction, "IE02033: Instruction argument can not be null");

    for (final Instruction instruction : codeNode.getInstructions()) {
      if (instruction.getNative() == searchInstruction) {
        return instruction;
      }
    }

    throw new IllegalStateException(
        "IE01273: Could not determine what instruction could not be translated");
  }

  /**
   * Searches for an instruction in a function.
   *
   * @param function The function to search through.
   * @param searchInstruction The instruction to search for.
   *
   * @return The API instruction object that wraps the search instruction.
   */
  public static Instruction findInstruction(final Function function,
      final IInstruction searchInstruction) {
    Preconditions.checkNotNull(function, "IE02034: Function argument can not be null");

    Preconditions.checkNotNull(searchInstruction, "IE02052: Instruction argument can not be null");

    for (final BasicBlock block : function.getGraph().getNodes()) {
      for (final Instruction instruction : block.getInstructions()) {
        if (instruction.getNative() == searchInstruction) {
          return instruction;
        }
      }
    }

    throw new IllegalStateException(
        "IE01274: Could not determine what instruction could not be translated");
  }

  /**
   * Searches for an instruction in a view.
   *
   * @param view The view to search through.
   * @param searchInstruction The instruction to search for.
   *
   * @return The API instruction object that wraps the search instruction.
   */
  public static Instruction findInstruction(final View view, final IInstruction searchInstruction) {
    Preconditions.checkNotNull(view, "IE02056: View argument can not be null");

    Preconditions.checkNotNull(searchInstruction, "IE02060: Instruction argument can not be null");

    for (final ViewNode node : view.getGraph().getNodes()) {
      if (node instanceof CodeNode) {
        final CodeNode codeNode = (CodeNode) node;

        for (final Instruction instruction : codeNode.getInstructions()) {
          if (instruction.getNative() == searchInstruction) {
            return instruction;
          }
        }
      }
    }

    throw new IllegalStateException(
        "IE01275: Could not determine what instruction could not be translated");
  }
}
