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
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.disassembly.IOperandTree;

import java.util.List;

/**
 * Translates RETN instructions to REIL code.
 */
public class RetnTranslator implements IInstructionTranslator {
  /**
   * Translates a RETN instruction to REIL code.
   *
   * @param environment A valid translation environment.
   * @param instruction The RETN instruction to translate.
   * @param instructions The generated REIL code will be added to this list
   *
   * @throws InternalTranslationException if any of the arguments are null the passed instruction is
   *         not an RETN instruction
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "retn");

    if (instruction.getOperands().size() > 1) {
      throw new InternalTranslationException(
          "Error: Argument instruction is not a retn instruction (invalid number of operands)");
    }

    final long baseOffset = instruction.getAddress().toLong() * 0x100;
    final long offset = baseOffset;

    final OperandSize archSize = environment.getArchitectureSize();
    final OperandSize nextSize = TranslationHelpers.getNextSize(archSize);

    final String truncateMask = String.valueOf(TranslationHelpers.getAllBitsMask(archSize));

    final String returnAddress = environment.getNextVariableString();
    final String adjustedStack = environment.getNextVariableString();

    // Load the return address from the stack
    instructions.add(ReilHelpers.createLdm(offset, archSize, "esp", archSize, returnAddress));

    final List<? extends IOperandTree> operands = instruction.getOperands();

    // Find out how much the stack must be moved
    final String stackMovement =
        operands.size() == 0 ? String.valueOf(archSize.getByteSize()) : String.valueOf(archSize
            .getByteSize() + Long.valueOf(Helpers.getLeafValue(operands.get(0).getRootNode())));

    // Adjust the stack and truncate overflows
    instructions.add(ReilHelpers.createAdd(offset + 1, archSize, "esp", archSize, stackMovement,
        nextSize, adjustedStack));
    instructions.add(ReilHelpers.createAnd(offset + 2, nextSize, adjustedStack, nextSize,
        truncateMask, archSize, "esp"));

    // Return from the function.
    instructions.add(ReilHelpers.createJcc(offset + 3, archSize, "1", archSize, returnAddress));
  }
}
