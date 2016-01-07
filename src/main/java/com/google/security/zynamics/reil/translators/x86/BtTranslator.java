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
package com.google.security.zynamics.reil.translators.x86;

import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.translators.IInstructionTranslator;
import com.google.security.zynamics.reil.translators.ITranslationEnvironment;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.TranslationHelpers;
import com.google.security.zynamics.reil.translators.TranslationResult;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.disassembly.IOperandTree;

import java.util.List;


/**
 * Translates BT instructions to REIL code.
 */
public class BtTranslator implements IInstructionTranslator {

  /**
   * Translates a BT instruction to REIL code.
   * 
   * @param environment A valid translation environment
   * @param instruction The BT instruction to translate
   * @param instructions The generated REIL code will be added to this list
   * 
   * @throws InternalTranslationException if any of the arguments are null the passed instruction is
   *         not a BT instruction
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {

    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "bt");

    if (instruction.getOperands().size() != 2) {
      throw new InternalTranslationException(
          "Error: Argument instruction is not a bt instruction (invalid number of operands)");
    }

    final long baseOffset = instruction.getAddress().toLong() * 0x100;
    long offset = baseOffset;

    final IOperandTree targetOperand = instruction.getOperands().get(0);
    final IOperandTree sourceOperand = instruction.getOperands().get(1);

    // Load the target operand.
    final TranslationResult targetResult =
        Helpers.translateOperand(environment, offset, targetOperand, true);
    instructions.addAll(targetResult.getInstructions());

    offset = baseOffset + instructions.size();

    // Load the source operand.
    final TranslationResult sourceResult =
        Helpers.translateOperand(environment, offset, sourceOperand, true);
    instructions.addAll(sourceResult.getInstructions());

    offset = baseOffset + instructions.size();

    final String negatedIndex = environment.getNextVariableString();
    // final String truncatedNegatedIndex = environment.getNextVariableString();
    final String shiftedTarget = environment.getNextVariableString();

    // TODO: Due to a bug in the REIL BSH specification we can not truncate the result
    // of the subtraction here. See the tests for an example of what goes wrong.

    instructions.add(ReilHelpers.createSub(offset++, OperandSize.BYTE, "0", sourceResult.getSize(),
        sourceResult.getRegister(), OperandSize.WORD, negatedIndex));
    // instructions.add(ReilHelpers.createAnd(offset++, OperandSize.WORD, negatedIndex,
    // OperandSize.BYTE, "255", OperandSize.BYTE, truncatedNegatedIndex));
    instructions.add(ReilHelpers.createBsh(offset++, targetResult.getSize(),
        targetResult.getRegister(), OperandSize.WORD, negatedIndex, targetResult.getSize(),
        shiftedTarget));

    instructions.add(ReilHelpers.createAnd(offset++, targetResult.getSize(), shiftedTarget,
        OperandSize.BYTE, "1", OperandSize.BYTE, Helpers.CARRY_FLAG));
  }
}
