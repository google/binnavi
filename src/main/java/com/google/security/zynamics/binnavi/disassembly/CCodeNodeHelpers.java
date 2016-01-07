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

import java.util.HashMap;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.disassembly.algorithms.CReferenceFinder;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.disassembly.IInstruction;

/**
 * Contains helper classes for working with code nodes.
 */
public final class CCodeNodeHelpers {
  /**
   * You are not supposed to instantiate this class.
   */
  private CCodeNodeHelpers() {
  }

  private static int getInstructionLineCount(final INaviCodeNode codeNode,
      final INaviInstruction instruction, final INaviFunction function) {

    int count = 0;

    if (codeNode.getComments().getLocalInstructionComment(instruction) != null) {
      for (final IComment comment : codeNode.getComments().getLocalInstructionComment(instruction)) {
        count += comment.getNumberOfCommentLines();
      }
    }
    if (instruction.getGlobalComment() != null) {
      for (final IComment comment : instruction.getGlobalComment()) {
        count += comment.getNumberOfCommentLines();
      }
    }
    if (function != null && function.getGlobalComment() != null) {
      for (final IComment comment : function.getGlobalComment()) {
        count += comment.getNumberOfCommentLines();
      }
    }

    return count == 0 ? 1 : count;
  }

  /**
   * Determines the index of the first instruction line in a code node.
   * 
   * @param codeNode The code node.
   * 
   * @return The index of the first instruction line in a code node.
   */
  private static int getInitialLineCounter(final INaviCodeNode codeNode) {
    try {
      codeNode.getParentFunction();
      return 1;
    } catch (final MaybeNullException e) {
      return 0;
    }
  }

  /**
   * Checks whether a given code node has an instruction at the given address.
   * 
   * @param codeNode The code node that provides the instructions.
   * @param offset The address to look for.
   * 
   * @return True, if the code node has an instruction at the given address. False, otherwise.
   */
  public static boolean containsAddress(final INaviCodeNode codeNode, final IAddress offset) {
    for (final IInstruction instruction : codeNode.getInstructions()) {
      if (instruction.getAddress().equals(offset)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Looks for an instruction by address.
   * 
   * @param codeNode The code node whose instructions are considered.
   * @param address The address to look for.
   * 
   * @return The index of the instruction or -1 if there is no such instruction.
   */
  public static int getInstruction(final INaviCodeNode codeNode, final IAddress address) {
    int counter = 0;

    for (final INaviInstruction instruction : codeNode.getInstructions()) {
      if (instruction.getAddress().equals(address)) {
        return counter;
      }

      ++counter;
    }

    return -1;
  }

  /**
   * Returns the line index of the line where a given instruction of a given node is shown in a
   * graph.
   * 
   * @param codeNode The code node that provides the instructions.
   * @param instruction The instruction whose line index is returned.
   * @return The line index of the instruction in the code node.
   */
  public static int instructionToLine(final INaviCodeNode codeNode,
      final INaviInstruction instruction) {
    Preconditions.checkNotNull(instruction, "IE00059: Instruction argument can not be null");
    Preconditions.checkNotNull(codeNode, "IE02530: codeNode argument can not be null");

    int lineCounter = getInitialLineCounter(codeNode);

    final HashMap<INaviInstruction, INaviFunction> functionMap =
        CReferenceFinder.getCodeReferenceMap(codeNode);

    for (final INaviInstruction inst : codeNode.getInstructions()) {
      if (inst == instruction) {
        return lineCounter;
      }

      lineCounter += getInstructionLineCount(codeNode, inst, functionMap.get(inst));
    }

    throw new IllegalArgumentException("IE00060: Instruction is not part of the node");
  }

  /**
   * Calculates the instruction at a given line in a code node.
   * 
   * @param codeNode The code node that provides the instructions.
   * @param line The line whose instruction is found.
   * 
   * @return The instruction that belongs to the line.
   */
  public static INaviInstruction lineToInstruction(final INaviCodeNode codeNode, final int line) {
    Preconditions.checkArgument(line >= 0, "IE00061: Line number can not be negative");
    Preconditions.checkNotNull(codeNode, "IE01784: Code node argument can not be null");

    int lineCounter = getInitialLineCounter(codeNode);

    final HashMap<INaviInstruction, INaviFunction> functionMap =
        CReferenceFinder.getCodeReferenceMap(codeNode);

    for (final INaviInstruction instruction : codeNode.getInstructions()) {
      final int instructionLines =
          getInstructionLineCount(codeNode, instruction, functionMap.get(instruction));

      if ((line >= lineCounter) && (line < (lineCounter + instructionLines))) {
        return instruction;
      }

      lineCounter += instructionLines;
    }

    return null;
  }
}
