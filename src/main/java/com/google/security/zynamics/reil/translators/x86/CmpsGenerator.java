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
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.translators.ITranslationEnvironment;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.TranslationHelpers;
import com.google.security.zynamics.zylib.disassembly.CAddress;

import java.util.List;


/**
 * Translates CMPS instructions to REIL code.
 */
public class CmpsGenerator implements IStringInstructionGenerator {

  /**
   * Translates a CMPS instruction to REIL code.
   *
   * @param environment A valid translation environment.
   * @param operandSize Size of the operands of the CMPS instruction.
   * @param instructions The generated REIL code will be added to this list
   *
   * @throws InternalTranslationException if any of the arguments are null the passed instruction is
   *         not a CMPS instruction
   */
  @Override
  public void generate(final ITranslationEnvironment environment, final long baseOffset,
      final OperandSize operandSize, final List<ReilInstruction> instructions)
      throws InternalTranslationException {
    Preconditions.checkNotNull(environment, "Error: Argument environment can't be null");
    Preconditions.checkNotNull(instructions, "Error: Argument instructions can't be null");

    final long offset = baseOffset;
    final int previousInstructions = (int) (baseOffset % 0x100);

    final OperandSize archSize = environment.getArchitectureSize();
    final OperandSize resultSize = TranslationHelpers.getNextSize(archSize);

    final String ediChange = String.valueOf(operandSize.getByteSize());

    final String truncateMask = String.valueOf(TranslationHelpers.getAllBitsMask(archSize));

    final String firstVar = environment.getNextVariableString();
    final String secondVar = environment.getNextVariableString();
    final String tempEsi1 = environment.getNextVariableString();
    final String tempEdi1 = environment.getNextVariableString();
    final String tempEsi2 = environment.getNextVariableString();
    final String tempEdi2 = environment.getNextVariableString();

    // Load the value at [ESI] and store it into [EDI]
    instructions.add(ReilHelpers.createLdm(offset, archSize, "esi", operandSize, firstVar));
    instructions.add(ReilHelpers.createLdm(offset + 1, archSize, "edi", operandSize, secondVar));

    // Change the value of ESI and EDI depending on the DF
    final String jmpGoal =
        String.format("%d.%d", ReilHelpers.toNativeAddress(new CAddress(baseOffset)).toLong(),
            previousInstructions + 8);
    instructions.add(ReilHelpers.createJcc(offset + 2, OperandSize.BYTE, Helpers.DIRECTION_FLAG,
        OperandSize.ADDRESS, jmpGoal));
    instructions.add(ReilHelpers.createAdd(offset + 3, archSize, "esi", archSize, ediChange,
        resultSize, tempEsi1));
    instructions.add(ReilHelpers.createAnd(offset + 4, resultSize, tempEsi1, resultSize,
        truncateMask, archSize, "esi"));
    instructions.add(ReilHelpers.createAdd(offset + 5, archSize, "edi", archSize, ediChange,
        resultSize, tempEdi1));
    instructions.add(ReilHelpers.createAnd(offset + 6, resultSize, tempEdi1, resultSize,
        truncateMask, archSize, "edi"));

    final String jmpGoal2 =
        String.format("%d.%d", ReilHelpers.toNativeAddress(new CAddress(baseOffset)).toLong(),
            previousInstructions + 12);
    instructions.add(ReilHelpers.createJcc(offset + 7, OperandSize.BYTE, "1", OperandSize.ADDRESS,
        jmpGoal2));
    instructions.add(ReilHelpers.createSub(offset + 8, archSize, "esi", archSize, ediChange,
        resultSize, tempEsi2));
    instructions.add(ReilHelpers.createAnd(offset + 9, resultSize, tempEsi2, resultSize,
        truncateMask, archSize, "esi"));
    instructions.add(ReilHelpers.createSub(offset + 10, archSize, "edi", archSize, ediChange,
        resultSize, tempEdi2));
    instructions.add(ReilHelpers.createAnd(offset + 11, resultSize, tempEdi2, resultSize,
        truncateMask, archSize, "edi"));

    Helpers.generateSub(environment, offset + 12, operandSize, firstVar, secondVar, instructions);
  }

}
