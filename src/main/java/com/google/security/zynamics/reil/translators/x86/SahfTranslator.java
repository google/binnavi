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

import java.util.List;


/**
 * Translates SAHF instructions to REIL code.
 */
public class SahfTranslator implements IInstructionTranslator {
  /**
   * Translates a SAHF instruction to REIL code.
   * 
   * @param environment A valid translation environment.
   * @param instruction The SAHF instruction to translate.
   * @param instructions The generated REIL code will be added to this list
   * 
   * @throws InternalTranslationException if any of the arguments are null the passed instruction is
   *         not an SAHF instruction
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "sahf");

    if (instruction.getOperands().size() != 0) {
      throw new InternalTranslationException(
          "Error: Argument instruction is not a sahf instruction (invalid number of operands)");
    }

    final long baseOffset = instruction.getAddress().toLong() * 0x100;

    final OperandSize archSize = environment.getArchitectureSize();

    final String shiftedEaxToCf = environment.getNextVariableString();
    final String shiftedEaxToPf = environment.getNextVariableString();
    final String shiftedEaxToAf = environment.getNextVariableString();
    final String shiftedEaxToZf = environment.getNextVariableString();
    final String shiftedEaxToSf = environment.getNextVariableString();

    // Shift the future CF into the lowest byte and put it into the CF
    instructions.add(ReilHelpers.createBsh(baseOffset, archSize, "eax", archSize, "-8", archSize,
        shiftedEaxToCf));
    instructions.add(ReilHelpers.createAnd(baseOffset + 1, archSize, shiftedEaxToCf, archSize, "1",
        OperandSize.BYTE, Helpers.CARRY_FLAG));

    // Shift the future PF into the lowest byte and put it into the PF
    instructions.add(ReilHelpers.createBsh(baseOffset + 2, archSize, "eax", archSize, "-10",
        archSize, shiftedEaxToPf));
    instructions.add(ReilHelpers.createAnd(baseOffset + 3, archSize, shiftedEaxToPf, archSize, "1",
        OperandSize.BYTE, Helpers.PARITY_FLAG));

    // Shift the future AF into the lowest byte and put it into the AF
    instructions.add(ReilHelpers.createBsh(baseOffset + 4, archSize, "eax", archSize, "-12",
        archSize, shiftedEaxToAf));
    instructions.add(ReilHelpers.createAnd(baseOffset + 5, archSize, shiftedEaxToAf, archSize, "1",
        OperandSize.BYTE, Helpers.AUXILIARY_FLAG));

    // Shift the future ZF into the lowest byte and put it into the ZF
    instructions.add(ReilHelpers.createBsh(baseOffset + 6, archSize, "eax", archSize, "-14",
        archSize, shiftedEaxToZf));
    instructions.add(ReilHelpers.createAnd(baseOffset + 7, archSize, shiftedEaxToZf, archSize, "1",
        OperandSize.BYTE, Helpers.ZERO_FLAG));

    // Shift the future SF into the lowest byte and put it into the SF
    instructions.add(ReilHelpers.createBsh(baseOffset + 8, archSize, "eax", archSize, "-15",
        archSize, shiftedEaxToSf));
    instructions.add(ReilHelpers.createAnd(baseOffset + 9, archSize, shiftedEaxToSf, archSize, "1",
        OperandSize.BYTE, Helpers.SIGN_FLAG));
  }
}
