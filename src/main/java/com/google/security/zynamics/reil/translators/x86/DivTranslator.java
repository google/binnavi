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
 * Translates DIV instructions to REIL code.
 */
public class DivTranslator implements IInstructionTranslator {

  /**
   * Translates a DIV instruction to REIL code.
   *
   * @param environment A valid translation environment
   * @param instruction The DIV instruction to translate
   * @param instructions The generated REIL code will be added to this list
   *
   * @throws InternalTranslationException if any of the arguments are null the passed instruction is
   *         not an DIV instruction
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {

    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "div");

    if (instruction.getOperands().size() != 1) {
      throw new InternalTranslationException(
          "Error: Argument instruction is not a div instruction (invalid number of operands)");
    }

    final long baseOffset = instruction.getAddress().toLong() * 0x100;
    long offset = baseOffset;

    final List<? extends IOperandTree> operands = instruction.getOperands();

    final IOperandTree divisorOperand = operands.get(0);

    final OperandSize size = Helpers.getOperandSize(divisorOperand);

    // Load the dividend
    final TranslationResult resultDividend = Helpers.loadFirstDivOperand(environment, offset, size);
    instructions.addAll(resultDividend.getInstructions());

    // Adjust the offset of the next REIL instruction
    offset = baseOffset + instructions.size();

    final String dividend = resultDividend.getRegister();

    // Load the divisor
    final TranslationResult resultDivisor =
        Helpers.translateOperand(environment, offset, divisorOperand, true);
    instructions.addAll(resultDivisor.getInstructions());

    // Adjust the offset of the next REIL instruction
    offset = baseOffset + instructions.size();

    final String divisor = resultDivisor.getRegister();

    final String divResult = environment.getNextVariableString();
    final String modResult = environment.getNextVariableString();

    // Perform divison and modulo operation
    instructions.add(ReilHelpers.createDiv(offset++, size, dividend, size, divisor, size, divResult));
    instructions.add(ReilHelpers.createMod(offset++, size, dividend, size, divisor, size,
        modResult));

    // Write the result back and set the flags
    instructions
        .addAll(Helpers.writeDivResult(environment, offset++, divResult, modResult, size));
    offset = baseOffset + instructions.size();
    // Undefine flags
    instructions.add(ReilHelpers.createUndef(offset++, OperandSize.BYTE, Helpers.AUXILIARY_FLAG));
    instructions.add(ReilHelpers.createUndef(offset++, OperandSize.BYTE, Helpers.CARRY_FLAG));
    instructions.add(ReilHelpers.createUndef(offset++, OperandSize.BYTE, Helpers.OVERFLOW_FLAG));
    instructions.add(ReilHelpers.createUndef(offset++, OperandSize.BYTE, Helpers.PARITY_FLAG));
    instructions.add(ReilHelpers.createUndef(offset++, OperandSize.BYTE, Helpers.SIGN_FLAG));
    instructions.add(ReilHelpers.createUndef(offset++, OperandSize.BYTE, Helpers.ZERO_FLAG));
  }

}
