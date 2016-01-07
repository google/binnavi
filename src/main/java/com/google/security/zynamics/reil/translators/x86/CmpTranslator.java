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

import com.google.common.base.Preconditions;
import com.google.security.zynamics.reil.OperandSize;
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
 * Translates CMP instructions to REIL code.
 */
public class CmpTranslator implements IInstructionTranslator {

  /**
   * Translates a CMP instruction to REIL code.
   *
   * @param environment A valid translation environment.
   * @param instruction The CMP instruction to translate.
   * @param instructions The generated REIL code will be added to this list
   *
   * @throws InternalTranslationException if any of the arguments are null the passed instruction is
   *         not a CMP instruction
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "cmp");
    Preconditions.checkArgument(instruction.getOperands().size() == 2,
        "Error: Argument instruction is not a cmp instruction (invalid number of operands)");

    final long baseOffset = instruction.getAddress().toLong() * 0x100;
    long offset = baseOffset;

    final List<? extends IOperandTree> operands = instruction.getOperands();

    final IOperandTree targetOperand = operands.get(0);
    final IOperandTree sourceOperand = operands.get(1);

    // Load first operand.
    final TranslationResult firstResult =
        Helpers.translateOperand(environment, offset, targetOperand, true);
    instructions.addAll(firstResult.getInstructions());

    // Adjust the offset of the next REIL instruction.
    offset = baseOffset + instructions.size();

    // Load second operand.
    final TranslationResult secondResult =
        Helpers.translateOperand(environment, offset, sourceOperand, true);
    instructions.addAll(secondResult.getInstructions());

    // Adjust the offset of the next REIL instruction.
    offset = baseOffset + instructions.size();

    final String firstRegister = firstResult.getRegister();
    final String secondRegister = secondResult.getRegister();

    final OperandSize size = firstResult.getSize();

    // CMP = SUB without writing the result back into the target operand
    Helpers.generateSub(environment, offset, size, firstRegister, secondRegister, instructions);
  }
}
