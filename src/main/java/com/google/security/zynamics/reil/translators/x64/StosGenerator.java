/*
Copyright 2015 Google Inc. All Rights Reserved.

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
package com.google.security.zynamics.reil.translators.x64;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.translators.ITranslationEnvironment;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.TranslationHelpers;
import com.google.security.zynamics.zylib.disassembly.CAddress;

/**
 * Translates STOS instructions to REIL code.
 */
public class StosGenerator implements IStringInstructionGenerator {

  /**
   * Translates a STOS instruction to REIL code.
   *
   * @param environment A valid translation environment.
   * @param operandSize Size of the operands of the STOS instruction.
   * @param instructions The generated REIL code will be added to this list
   *
   * @throws InternalTranslationException if any of the arguments are null the passed instruction is
   *         not an STOS instruction
   */
  @Override
  public void generate(final ITranslationEnvironment environment, final long baseOffset,
      final OperandSize operandSize, final List<ReilInstruction> instructions)
      throws InternalTranslationException {

    Preconditions.checkNotNull(environment, "Error: Argument environment can't be null");
    Preconditions.checkNotNull(instructions, "Error: Argument instructions can't be null");
    Preconditions.checkArgument(baseOffset >= 0, "Error: Argument offset can't be less than 0");

    long offset = baseOffset;
    final int previousInstructions = (int) (baseOffset % 0x100);

    final OperandSize archSize = environment.getArchitectureSize();
    final OperandSize resultSize = TranslationHelpers.getNextSize(archSize);

    String maskedEax = null;
    final String ediChange = String.valueOf(operandSize.getByteSize());

    final String mask = String.valueOf(TranslationHelpers.getAllBitsMask(operandSize));
    final String truncateMask = String.valueOf(TranslationHelpers.getAllBitsMask(archSize));

    if (operandSize != archSize) {
      maskedEax = environment.getNextVariableString();
      instructions.add(ReilHelpers.createAnd(offset, archSize, "rax", archSize, mask, archSize,
          maskedEax));
      offset++;
    } else {
      maskedEax = "rax";
    }

    final String addResult = environment.getNextVariableString();
    final String subResult = environment.getNextVariableString();

    // Store EAX to [EDI]
    instructions.add(ReilHelpers.createStm(offset, operandSize, maskedEax, archSize, "rdi"));

    // Update EDI depending on the value of the DF
    final String jmpGoal =
        String.format("%d.%d", ReilHelpers.toNativeAddress(new CAddress(baseOffset)).toLong(),
            previousInstructions + 5 + (operandSize != archSize ? 1 : 0));
    final String jmpGoal2 =
        String.format("%d.%d", ReilHelpers.toNativeAddress(new CAddress(baseOffset)).toLong(),
            previousInstructions + 7 + (operandSize != archSize ? 1 : 0));
    instructions.add(ReilHelpers.createJcc(offset + 1, OperandSize.BYTE, Helpers.DIRECTION_FLAG,
        OperandSize.ADDRESS, jmpGoal));
    instructions.add(ReilHelpers.createAdd(offset + 2, archSize, "rdi", archSize, ediChange,
        resultSize, addResult));
    instructions.add(ReilHelpers.createAnd(offset + 3, resultSize, addResult, archSize,
        truncateMask, archSize, "rdi"));
    instructions.add(ReilHelpers.createJcc(offset + 4, OperandSize.BYTE, "1", OperandSize.ADDRESS,
        jmpGoal2));

    instructions.add(ReilHelpers.createSub(offset + 5, archSize, "rdi", archSize, ediChange,
        resultSize, subResult));
    instructions.add(ReilHelpers.createAnd(offset + 6, resultSize, subResult, archSize,
        truncateMask, archSize, "rdi"));

    instructions.add(ReilHelpers.createNop(offset + 7));
  }
}
