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

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.translators.ITranslationEnvironment;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.TranslationHelpers;
import com.google.security.zynamics.zylib.disassembly.CAddress;

public class LodsGenerator implements IStringInstructionGenerator {

  /**
   * Translates a LODS instruction to REIL code.
   *
   * @param environment A valid translation environment.
   * @param operandSize Size of the operands of the lods instruction.
   * @param instructions The generated REIL code will be added to this list
   *
   * @throws InternalTranslationException if any of the arguments are null the passed instruction is
   *         not a LODS instruction
   */
  @Override
  public void generate(final ITranslationEnvironment environment, final long baseOffset,
      final OperandSize operandSize, final List<ReilInstruction> instructions)
      throws InternalTranslationException {
    Preconditions.checkNotNull(environment, "Error: Argument environment can't be null");
    Preconditions.checkNotNull(instructions, "Error: Argument instructions can't be null");

    long offset = baseOffset;
    final int previousInstructions = (int) (baseOffset % 0x100);

    final OperandSize archSize = environment.getArchitectureSize();
    final OperandSize resultSize = TranslationHelpers.getNextSize(operandSize);

    final String ediChange = String.valueOf(operandSize.getByteSize());

    final String truncateMask = String.valueOf(TranslationHelpers.getAllBitsMask(archSize));

    if (operandSize == archSize) {

      // If the size is DWORD load the value directly into EAX
      instructions.add(ReilHelpers.createLdm(offset++, archSize, "esi", archSize, "eax"));

    } else {

      // If the size is not DWORD load the value into a temp register first.

      final String tempLoad = environment.getNextVariableString();
      final String maskedEax = environment.getNextVariableString();

      final String mask = String.valueOf(TranslationHelpers.getAllButMask(archSize, operandSize));

      instructions.add(ReilHelpers.createLdm(offset++, archSize, "esi", operandSize, tempLoad));
      instructions.add(ReilHelpers.createAnd(offset++, archSize, "eax", archSize, mask, archSize,
          maskedEax));
      instructions.add(ReilHelpers.createOr(offset++, operandSize, tempLoad, archSize, maskedEax,
          archSize, "eax"));
    }

    final String tempEsi1 = environment.getNextVariableString();
    final String tempEsi2 = environment.getNextVariableString();

    final int linesBefore = instructions.size();

    // Change the value of ESI depending on the DF
    final String jmpGoal =
        String.format("%d.%d", ReilHelpers.toNativeAddress(new CAddress(baseOffset)).toLong(),
            previousInstructions + linesBefore + 4);
    instructions.add(ReilHelpers.createJcc(offset++, OperandSize.BYTE, Helpers.DIRECTION_FLAG,
        OperandSize.ADDRESS, jmpGoal));

    instructions.add(ReilHelpers.createAdd(offset++, archSize, "esi", archSize, ediChange,
        resultSize, tempEsi1));
    instructions.add(ReilHelpers.createAnd(offset++, resultSize, tempEsi1, archSize,
        truncateMask, archSize, "esi"));

    final String jmpGoal2 =
        String.format("%d.%d", ReilHelpers.toNativeAddress(new CAddress(baseOffset)).toLong(),
            previousInstructions + linesBefore + 6);
    instructions.add(ReilHelpers.createJcc(offset++, OperandSize.BYTE, "1", OperandSize.ADDRESS,
        jmpGoal2));

    instructions.add(ReilHelpers.createSub(offset++, archSize, "esi", archSize, ediChange,
        resultSize, tempEsi2));
    instructions.add(ReilHelpers.createAnd(offset++, resultSize, tempEsi2, archSize,
        truncateMask, archSize, "esi"));

    instructions.add(ReilHelpers.createNop(offset));
  }
}
