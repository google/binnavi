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
 * Translates LAHF instructions to REIL code.
 */
public class LahfTranslator implements IInstructionTranslator {

  /**
   * Translates a LAHF instruction to REIL code.
   * 
   * @param environment A valid translation environment.
   * @param instruction The LAHF instruction to translate.
   * @param instructions The generated REIL code will be added to this list
   * 
   * @throws InternalTranslationException if any of the arguments are null the passed instruction is
   *         not a LAHF instruction
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "lahf");

    if (instruction.getOperands().size() != 0) {
      throw new InternalTranslationException(
          "Error: Argument instruction is not a lahf instruction (invalid number of operands)");
    }

    final long baseOffset = instruction.getAddress().toLong() * 0x100;

    final OperandSize archSize = environment.getArchitectureSize();

    final String clearedEax = environment.getNextVariableString();
    final String shiftedSf = environment.getNextVariableString();
    final String clearedEaxSf = environment.getNextVariableString();
    final String shiftedZf = environment.getNextVariableString();
    final String clearedEaxZf = environment.getNextVariableString();
    final String shiftedAf = environment.getNextVariableString();
    final String clearedEaxAf = environment.getNextVariableString();
    final String shiftedPf = environment.getNextVariableString();
    final String clearedEaxPf = environment.getNextVariableString();
    final String shiftedCf = environment.getNextVariableString();
    final String clearedEaxCf = environment.getNextVariableString();

    // Clear AH
    instructions.add(ReilHelpers.createAnd(baseOffset, archSize, "eax", archSize, "4294902015",
        archSize, clearedEax));

    // Move the SF into the highest bit of AH
    instructions.add(ReilHelpers.createBsh(baseOffset + 1, OperandSize.BYTE, Helpers.SIGN_FLAG,
        OperandSize.BYTE, "15", OperandSize.WORD, shiftedSf));
    instructions.add(ReilHelpers.createOr(baseOffset + 2, archSize, clearedEax, OperandSize.WORD,
        shiftedSf, archSize, clearedEaxSf));

    // Move the ZF into the 6th bit of AH
    instructions.add(ReilHelpers.createBsh(baseOffset + 3, OperandSize.BYTE, Helpers.ZERO_FLAG,
        OperandSize.BYTE, "14", OperandSize.WORD, shiftedZf));
    instructions.add(ReilHelpers.createOr(baseOffset + 4, archSize, clearedEaxSf, OperandSize.WORD,
        shiftedZf, archSize, clearedEaxZf));

    // Move the AF into the 4th bit of AH
    instructions.add(ReilHelpers.createBsh(baseOffset + 5, OperandSize.BYTE,
        Helpers.AUXILIARY_FLAG, OperandSize.BYTE, "12", OperandSize.WORD, shiftedAf));
    instructions.add(ReilHelpers.createOr(baseOffset + 6, archSize, clearedEaxZf, OperandSize.WORD,
        shiftedAf, archSize, clearedEaxAf));

    // Move the PF into the 2nd bit of AH
    instructions.add(ReilHelpers.createBsh(baseOffset + 7, OperandSize.BYTE, Helpers.PARITY_FLAG,
        OperandSize.BYTE, "10", OperandSize.WORD, shiftedPf));
    instructions.add(ReilHelpers.createOr(baseOffset + 8, archSize, clearedEaxAf, OperandSize.WORD,
        shiftedPf, archSize, clearedEaxPf));

    // Move the CF into the LSB of AH
    instructions.add(ReilHelpers.createBsh(baseOffset + 9, OperandSize.BYTE, Helpers.CARRY_FLAG,
        OperandSize.BYTE, "8", OperandSize.WORD, shiftedCf));
    instructions.add(ReilHelpers.createOr(baseOffset + 10, archSize, clearedEaxPf,
        OperandSize.WORD, shiftedCf, archSize, clearedEaxCf));

    // The 1st bit of AH ( = 8th bit of EAX) is set to 1
    instructions.add(ReilHelpers.createOr(baseOffset + 11, archSize, clearedEaxCf, archSize, "512",
        archSize, "eax"));
  }

}
