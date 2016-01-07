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
 * Translates CALL instructions to REIL code.
 */
public class CallTranslator implements IInstructionTranslator {

  /**
   * Translates a CALL instruction to REIL code.
   * 
   * @param environment A valid translation environment
   * @param instruction The CALL instruction to translate
   * @param instructions The generated REIL code will be added to this list
   * 
   * @throws InternalTranslationException if any of the arguments are null the passed instruction is
   *         not a CALL instruction
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {

    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "call");

    if (instruction.getOperands().size() != 1) {
      throw new InternalTranslationException(
          "Error: Argument instruction is not a call instruction (invalid number of operands)");
    }

    final long baseOffset = instruction.getAddress().toLong() * 0x100;
    long offset = baseOffset;

    final OperandSize archSize = environment.getArchitectureSize();
    final OperandSize nextSize = TranslationHelpers.getNextSize(archSize);

    final String truncateMask = String.valueOf(TranslationHelpers.getAllBitsMask(archSize));

    final String tempEsp = environment.getNextVariableString();
    final String returnAddress =
        String.valueOf(instruction.getAddress().toLong() + instruction.getLength());

    // Move the stack, make sure to truncate potential overflows
    instructions.add(ReilHelpers.createSub(offset, archSize, "esp", archSize, "4", nextSize,
        tempEsp));
    instructions.add(ReilHelpers.createAnd(offset + 1, nextSize, tempEsp, archSize, truncateMask,
        archSize, "esp"));

    // Push the return address onto the stack
    instructions.add(ReilHelpers.createStm(offset + 2, archSize, returnAddress, archSize, "esp"));

    // CALL instructions have exactly one operand
    final IOperandTree targetOperand = instruction.getOperands().get(0);

    // Load the operand.
    final TranslationResult result =
        Helpers.translateOperand(environment, offset + 3, targetOperand, true);
    instructions.addAll(result.getInstructions());

    // Adjust the offset of the next REIL instruction
    offset = baseOffset + instructions.size();

    // Add the unconditional jump to the target
    instructions.add(ReilHelpers.createJcc(offset, OperandSize.DWORD, "1", result.getSize(),
        result.getRegister(), "isCall", "true"));
  }

}
