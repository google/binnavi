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
 * Translates CWDE instructions to REIL code.
 */
public class CdqeTranslator implements IInstructionTranslator {

  /**
   * Translates a CWDE instruction to REIL code.
   * 
   * @param environment A valid translation environment.
   * @param instruction The CWDE instruction to translate.
   * @param instructions The generated REIL code will be added to this list
   * 
   * @throws InternalTranslationException if any of the arguments are null the passed instruction is
   *         not an CWDE instruction
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {

    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "cdqe");

    // Do not bother to check the number of arguments here for we
    // are not using them anyway.

    final long baseOffset = instruction.getAddress().toLong() * 0x100;

    final String isolatedMsb = environment.getNextVariableString();
    final String shiftedMsb = environment.getNextVariableString();
    final String mask = environment.getNextVariableString();
    final String truncatedMask = environment.getNextVariableString();
    final String eax = environment.getNextVariableString();

    // Isolate the MSB of EAX
    instructions.add(ReilHelpers.createAnd(baseOffset, OperandSize.QWORD, "rax", OperandSize.QWORD,
        "2147483648", OperandSize.QWORD, isolatedMsb));

    // Shift the MSB into the LSB
    instructions.add(ReilHelpers.createBsh(baseOffset + 1, OperandSize.QWORD, isolatedMsb,
        OperandSize.QWORD, "-31", OperandSize.QWORD, shiftedMsb));

    // Create the mask
    instructions.add(ReilHelpers.createSub(baseOffset + 2, OperandSize.QWORD, "0",
        OperandSize.QWORD, shiftedMsb, OperandSize.QWORD, mask));

    // Truncate the mask to 0xFFFF0000 or 0x00000000
    instructions.add(ReilHelpers.createAnd(baseOffset + 3, OperandSize.QWORD, mask,
        OperandSize.QWORD, "18446744069414584320", OperandSize.QWORD, truncatedMask));

    // Save AX
    instructions.add(ReilHelpers.createAnd(baseOffset + 4, OperandSize.QWORD, "rax",
        OperandSize.QWORD, "4294967295", OperandSize.QWORD, eax));

    // Combine the mask with EAX and store it in rax
    instructions.add(ReilHelpers.createOr(baseOffset + 5, OperandSize.QWORD, truncatedMask,
        OperandSize.QWORD, eax, OperandSize.QWORD, "rax"));

  }

}
