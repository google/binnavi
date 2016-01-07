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
import com.google.security.zynamics.zylib.general.Pair;

import java.util.List;


/**
 * Translates IDIV instructions to REIL code.
 */
public class IdivTranslator implements IInstructionTranslator {

  /**
   * Translates a IDIV instruction to REIL code.
   * 
   * @param environment A valid translation environment.
   * @param instruction The IDIV instruction to translate.
   * @param instructions The generated REIL code will be added to this list
   * 
   * @throws InternalTranslationException if any of the arguments are null the passed instruction is
   *         not an IDIV instruction
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "idiv");

    if (instruction.getOperands().size() != 1) {
      throw new InternalTranslationException(
          "Error: Argument instruction is not a idiv instruction (invalid number of operands)");
    }

    final List<? extends IOperandTree> operands = instruction.getOperands();

    final IOperandTree divisorOperand = operands.get(0);

    final long baseOffset = instruction.getAddress().toLong() * 0x100;
    long offset = baseOffset;

    final OperandSize size = Helpers.getOperandSize(operands.get(0));

    // Load the dividend
    final TranslationResult resultDividend = Helpers.loadFirstDivOperand(environment, offset, size);
    instructions.addAll(resultDividend.getInstructions());

    // Adjust the offset of the next REIL instruction.
    offset = baseOffset + instructions.size();

    final String dividend = resultDividend.getRegister();

    // Load the divisor
    final TranslationResult resultDivisor =
        Helpers.translateOperand(environment, offset, divisorOperand, true);
    instructions.addAll(resultDivisor.getInstructions());

    // Adjust the offset of the next REIL instruction.
    offset = baseOffset + instructions.size();

    final String divisor = resultDivisor.getRegister();

    // Here's how to express signed division using unsigned division:
    // 1. Get the absolute value of both operands
    // 2. Divide unsigned
    // 3. Change the sign of the result if the signs of the operands were different

    // Get the absolute value of the two factors for unsigned multiplication
    final Pair<String, String> absDividend =
        Helpers.generateAbs(environment, offset, dividend, size, instructions);

    // Adjust the offset of the next REIL instruction.
    offset = baseOffset + instructions.size();

    final Pair<String, String> absDivisor =
        Helpers.generateAbs(environment, offset, divisor, size, instructions);

    // Adjust the offset of the next REIL instruction.
    offset = baseOffset + instructions.size();

    // Perform division and modulo operation
    final String divResult = environment.getNextVariableString();
    final String modResult = environment.getNextVariableString();

    instructions.add(ReilHelpers.createDiv(offset, size, absDividend.second(), size,
        absDivisor.second(), size, divResult));
    instructions.add(ReilHelpers.createMod(offset + 1, size, absDividend.second(), size,
        absDivisor.second(), size, modResult));

    // Find out if the two operands had different signs and create a sign mask
    final String xoredSigns = environment.getNextVariableString();
    final String toggleMask = environment.getNextVariableString();

    instructions.add(ReilHelpers.createXor(offset + 2, size, absDividend.first(), size,
        absDividend.second(), size, xoredSigns));
    instructions.add(ReilHelpers.createSub(offset + 3, size, "0", size, xoredSigns, size,
        toggleMask));

    // Adjust the div result
    final String decDivResult = environment.getNextVariableString();
    final String realDivResult = environment.getNextVariableString();

    instructions.add(ReilHelpers.createSub(offset + 4, size, divResult, size, xoredSigns, size,
        decDivResult));
    instructions.add(ReilHelpers.createXor(offset + 5, size, decDivResult, size, toggleMask, size,
        realDivResult));

    // Adjust the mod result (the sign of the mod result is the sign of the first operand)
    final String modToggleMask = environment.getNextVariableString();
    final String decModResult = environment.getNextVariableString();
    final String realModResult = environment.getNextVariableString();

    instructions.add(ReilHelpers.createSub(offset + 6, size, "0", size, absDividend.first(), size,
        modToggleMask));

    instructions.add(ReilHelpers.createSub(offset + 7, size, modResult, size, absDividend.first(),
        size, decModResult));
    instructions.add(ReilHelpers.createXor(offset + 8, size, decModResult, size, modToggleMask,
        size, realModResult));

    // Write the result back and set the flags
    instructions.addAll(Helpers.writeDivResult(environment, offset + 9, realDivResult,
        realModResult, size));
  }

}
