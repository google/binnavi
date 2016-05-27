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

/**
 * Translates SCAS instructions to REIL code.
 */
public class ScasGenerator implements IStringInstructionGenerator {

  /**
   * Translates a SCAS instruction to REIL code.
   *
   * @param environment A valid translation environment.
   * @param operandSize Size of the operands of the SCAS instruction.
   * @param instructions The generated REIL code will be added to this list
   *
   * @throws InternalTranslationException if any of the arguments are null the passed instruction is
   *         not an SCAS instruction
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

    String maskedEax = null;
    final String ediChange = String.valueOf(operandSize.getByteSize());

    final String mask = String.valueOf(TranslationHelpers.getAllBitsMask(operandSize));
    final String truncateMask = String.valueOf(TranslationHelpers.getAllBitsMask(archSize));

    final String result = environment.getNextVariableString();

    // Load the value from EDI
    instructions.add(ReilHelpers.createLdm(offset++, archSize, "edi", operandSize, result));

    if (operandSize != archSize) {
      maskedEax = environment.getNextVariableString();
      instructions.add(
          ReilHelpers.createAnd(offset++, archSize, "eax", archSize, mask, archSize, maskedEax));
    } else {
      maskedEax = "eax";
    }

    final String addResult = environment.getNextVariableString();
    final String subResult = environment.getNextVariableString();

    // Update EDI depending on the value of the DF
    final String jmpGoal = String.format("%d.%d",
        ReilHelpers.toNativeAddress(new CAddress(baseOffset)).toLong(),
        previousInstructions + 5 + (operandSize != archSize ? 1 : 0));
    instructions.add(ReilHelpers.createJcc(offset++, OperandSize.BYTE, Helpers.DIRECTION_FLAG,
        OperandSize.ADDRESS, jmpGoal));
    instructions.add(ReilHelpers.createAdd(offset++, archSize, "edi", archSize, ediChange,
        resultSize, addResult));
    instructions.add(ReilHelpers.createAnd(offset++, resultSize, addResult, resultSize,
        truncateMask, resultSize, "edi"));

    final String jmpGoal2 = String.format("%d.%d",
        ReilHelpers.toNativeAddress(new CAddress(baseOffset)).toLong(),
        previousInstructions + 7 + (operandSize != archSize ? 1 : 0));
    instructions
        .add(ReilHelpers.createJcc(offset++, OperandSize.BYTE, "1", OperandSize.ADDRESS, jmpGoal2));
    instructions.add(ReilHelpers.createSub(offset++, archSize, "edi", archSize, ediChange,
        resultSize, subResult));
    instructions.add(ReilHelpers.createAnd(offset++, resultSize, subResult, resultSize,
        truncateMask, resultSize, "edi"));

    instructions.add(ReilHelpers.createNop(offset++));

    Helpers.generateSub(environment, offset++, operandSize, maskedEax, result, instructions);
  }
}
