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
 * Translates MUL instructions to REIL code.
 */
public class MulTranslator implements IInstructionTranslator {
  /**
   * Translates a MUL instruction to REIL code.
   * 
   * @param environment A valid translation environment.
   * @param instruction The MUL instruction to translate.
   * @param instructions The generated REIL code will be added to this list
   * 
   * @throws InternalTranslationException if any of the arguments are null the passed instruction is
   *         not a MUL instruction
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "mul");

    if (instruction.getOperands().size() != 1) {
      throw new InternalTranslationException(
          "Error: Argument instruction is not a mul instruction (invalid number of operand)");
    }

    final long baseOffset = instruction.getAddress().toLong() * 0x100;
    long offset = baseOffset;

    final List<? extends IOperandTree> operands = instruction.getOperands();

    // Load source operand.
    final TranslationResult firstResult =
        Helpers.translateOperand(environment, offset, operands.get(0), true);
    instructions.addAll(firstResult.getInstructions());

    // Adjust the offset of the next REIL instruction.
    offset = baseOffset + instructions.size();

    final String operand1 = firstResult.getRegister();
    final String operand2 = "eax";

    final OperandSize size1 = firstResult.getSize();
    final OperandSize size2 = environment.getArchitectureSize();
    final OperandSize resultSize = TranslationHelpers.getNextSize(size2);

    final String result = environment.getNextVariableString();
    final String upperHalf = environment.getNextVariableString();
    final String upperHalfZero = environment.getNextVariableString();

    final String maskUpper = String.valueOf(TranslationHelpers.getAllButMask(resultSize, size1));

    // Multiply the operands
    instructions.add(ReilHelpers.createMul(offset, size1, operand1, size2, operand2, resultSize,
        result));

    // Clear the lower half of the result
    instructions.add(ReilHelpers.createAnd(offset + 1, resultSize, result, resultSize, maskUpper,
        resultSize, upperHalf));

    // Check whether upper half is zero
    instructions.add(ReilHelpers.createBisz(offset + 2, resultSize, upperHalf, OperandSize.BYTE,
        upperHalfZero));

    // CF = Upper half is zero
    instructions.add(ReilHelpers.createBisz(offset + 3, resultSize, upperHalfZero,
        OperandSize.BYTE, Helpers.CARRY_FLAG));

    // OF = Upper half is zero
    instructions.add(ReilHelpers.createBisz(offset + 4, resultSize, upperHalfZero,
        OperandSize.BYTE, Helpers.OVERFLOW_FLAG));

    // SF, ZF, AF and PF are undefined
    instructions.add(ReilHelpers.createUndef(offset + 5, OperandSize.BYTE, Helpers.SIGN_FLAG));
    instructions.add(ReilHelpers.createUndef(offset + 6, OperandSize.BYTE, Helpers.ZERO_FLAG));
    instructions.add(ReilHelpers.createUndef(offset + 7, OperandSize.BYTE, Helpers.AUXILIARY_FLAG));
    instructions.add(ReilHelpers.createUndef(offset + 8, OperandSize.BYTE, Helpers.PARITY_FLAG));

    instructions.addAll(Helpers.writeMulResult(environment, offset + 9, result, size1));
  }
}
