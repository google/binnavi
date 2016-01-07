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
 * Translates CDQ instructions to REIL code.
 */
public class CdqTranslator implements IInstructionTranslator {

  /**
   * Translates a CDQ instruction to REIL code.
   * 
   * @param environment A valid translation environment
   * @param instruction The CDQ instruction to translate
   * @param instructions The generated REIL code will be added to this list
   * 
   * @throws InternalTranslationException if any of the arguments are null the passed instruction is
   *         not a CDQ instruction
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {

    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "cdq");

    // Skip the argument number check because we do not access the arguments anyway

    final long baseOffset = instruction.getAddress().toLong() * 0x100;

    final String isolatedMsb = environment.getNextVariableString();
    final String shiftedMsb = environment.getNextVariableString();

    // Isolate the MSB of EAX
    instructions.add(ReilHelpers.createAnd(baseOffset, OperandSize.DWORD, "eax", OperandSize.DWORD,
        "2147483648", OperandSize.DWORD, isolatedMsb));

    // Shift the MSB into the LSB
    instructions.add(ReilHelpers.createBsh(baseOffset + 1, OperandSize.DWORD, isolatedMsb,
        OperandSize.DWORD, "-31", OperandSize.DWORD, shiftedMsb));

    // Set the new value of EDX to 0 (= 0 - 0) or -1 (= 0 - 1)
    instructions.add(ReilHelpers.createSub(baseOffset + 2, OperandSize.DWORD, "0",
        OperandSize.DWORD, shiftedMsb, OperandSize.DWORD, "edx"));

  }

}
