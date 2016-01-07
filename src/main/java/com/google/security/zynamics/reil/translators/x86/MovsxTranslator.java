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
 * Translates MOVSX instructions to REIL code.
 */
public class MovsxTranslator implements IInstructionTranslator {

  /**
   * Translates a MOVSX instruction to REIL code.
   * 
   * @param environment A valid translation environment.
   * @param instruction The MOVSX instruction to translate.
   * @param instructions The generated REIL code will be added to this list
   * 
   * @throws InternalTranslationException if any of the arguments are null the passed instruction is
   *         not a MOVSX instruction
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "movsx");

    if (instruction.getOperands().size() != 2) {
      throw new InternalTranslationException(
          "Error: Argument instruction is not a movsx instruction (invalid number of operands)");
    }

    final long baseOffset = instruction.getAddress().toLong() * 0x100;
    long offset = baseOffset;

    final List<? extends IOperandTree> operands = instruction.getOperands();

    // Load source operand.
    final TranslationResult sourceResult =
        Helpers.translateOperand(environment, offset, operands.get(1), true);
    instructions.addAll(sourceResult.getInstructions());

    // Adjust the offset of the next REIL instruction.
    offset = baseOffset + instructions.size();

    final String sourceOperand = sourceResult.getRegister();

    // Load destination operand (must be a register).
    final String destinationOperand = Helpers.getLeafValue(operands.get(0).getRootNode());

    final OperandSize destSize = Helpers.getRegisterSize(destinationOperand);
    final OperandSize sourceSize = sourceResult.getSize();

    final TranslationResult extendedSign =
        Helpers.extendSign(environment, offset, sourceOperand, sourceSize, destSize);
    instructions.addAll(extendedSign.getInstructions());

    // Adjust the offset of the next REIL instruction.
    offset = baseOffset + instructions.size();

    if (destSize == environment.getArchitectureSize()) {
      instructions.add(ReilHelpers.createStr(offset, destSize, extendedSign.getRegister(),
          destSize, destinationOperand));
    } else {
      Helpers.moveAndMask(environment, offset, extendedSign.getSize(), extendedSign.getRegister(),
          destinationOperand, instructions);
    }
  }

}
