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
 * Translates XLAT instructions to REIL code.
 */
public class XlatTranslator implements IInstructionTranslator {
  /**
   * Translates a XLAT instruction to REIL code.
   * 
   * @param environment A valid translation environment.
   * @param instruction The XLAT instruction to translate.
   * @param instructions The generated REIL code will be added to this list
   * 
   * @throws InternalTranslationException if any of the arguments are null the passed instruction is
   *         not an XLAT instruction
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "xlat");

    if (instruction.getOperands().size() != 0) {
      throw new InternalTranslationException(
          "Error: Argument instruction is not a xlat instruction (invalid number of operands)");
    }

    final long baseOffset = instruction.getAddress().toLong() * 0x100;
    final long offset = baseOffset;

    final String isolatedAl = environment.getNextVariableString();
    final String address = environment.getNextVariableString();
    final String truncatedAddress = environment.getNextVariableString();
    final String value = environment.getNextVariableString();
    final String maskedEax = environment.getNextVariableString();

    // Get the position of the entry in the table
    instructions.add(ReilHelpers.createAnd(offset, OperandSize.DWORD, "eax", OperandSize.DWORD,
        "255", OperandSize.DWORD, isolatedAl));
    instructions.add(ReilHelpers.createAdd(offset + 1, OperandSize.DWORD, isolatedAl,
        OperandSize.DWORD, "ebx", OperandSize.QWORD, address));
    instructions.add(ReilHelpers.createAnd(offset + 2, OperandSize.QWORD, address,
        OperandSize.DWORD, "4294967295", OperandSize.DWORD, truncatedAddress));

    // Load the value from the table
    instructions.add(ReilHelpers.createLdm(offset + 3, OperandSize.DWORD, address,
        OperandSize.BYTE, value));

    // Store the value in AL
    instructions.add(ReilHelpers.createAnd(offset + 4, OperandSize.DWORD, "eax", OperandSize.DWORD,
        "4294967040", OperandSize.DWORD, maskedEax));
    instructions.add(ReilHelpers.createOr(offset + 5, OperandSize.BYTE, value, OperandSize.DWORD,
        maskedEax, OperandSize.DWORD, "eax"));
  }
}
