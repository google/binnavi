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
 * Translates CWD instructions to REIL code.
 */
public class CwdTranslator implements IInstructionTranslator {

  /**
   * Translates a CWD instruction to REIL code.
   * 
   * @param environment A valid translation environment.
   * @param instruction The CWD instruction to translate.
   * @param instructions The generated REIL code will be added to this list
   * 
   * @throws InternalTranslationException if any of the arguments are null the passed instruction is
   *         not an CDW instruction
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {

    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "cwd");

    // Do not bother checking the number of arguments here
    // because we are not using them anyway.

    final long baseOffset = instruction.getAddress().toLong() * 0x100;

    final String isolatedMsb = environment.getNextVariableString();
    final String shiftedMsb = environment.getNextVariableString();
    final String mask = environment.getNextVariableString();
    final String truncatedMask = environment.getNextVariableString();
    final String maskedEdx = environment.getNextVariableString();

    // Isolate the MSB of AX
    instructions.add(ReilHelpers.createAnd(baseOffset, OperandSize.DWORD, "eax", OperandSize.DWORD,
        "32768", OperandSize.DWORD, isolatedMsb));

    // Shift the MSB into the LSB
    instructions.add(ReilHelpers.createBsh(baseOffset + 1, OperandSize.DWORD, isolatedMsb,
        OperandSize.DWORD, "-15", OperandSize.DWORD, shiftedMsb));

    // Create the mask
    instructions.add(ReilHelpers.createSub(baseOffset + 2, OperandSize.DWORD, "0",
        OperandSize.DWORD, shiftedMsb, OperandSize.DWORD, mask));

    // Truncate the mask to 0x0000 or 0xFFFF
    instructions.add(ReilHelpers.createAnd(baseOffset + 3, OperandSize.DWORD, mask,
        OperandSize.DWORD, "65535", OperandSize.WORD, truncatedMask));

    // Clear DX
    instructions.add(ReilHelpers.createAnd(baseOffset + 4, OperandSize.DWORD, "edx",
        OperandSize.DWORD, "4294901760", OperandSize.DWORD, maskedEdx));

    // Put the mask into DX
    instructions.add(ReilHelpers.createOr(baseOffset + 5, OperandSize.WORD, truncatedMask,
        OperandSize.DWORD, maskedEdx, OperandSize.DWORD, "edx"));
  }

}
