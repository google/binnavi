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
 * Translates CLD instructions to REIL code.
 */
public class CldTranslator implements IInstructionTranslator {

  /**
   * Translates a CLD instruction to REIL code.
   * 
   * @param environment A valid translation environment
   * @param instruction The CLD instruction to translate
   * @param instructions The generated REIL code will be added to this list
   * 
   * @throws InternalTranslationException if any of the arguments are null the passed instruction is
   *         not a CLD instruction
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {

    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "cld");

    if (instruction.getOperands().size() != 0) {
      throw new InternalTranslationException(
          "Error: Argument instruction is not a cld instruction (invalid number of operand)");
    }

    final long baseOffset = instruction.getAddress().toLong() * 0x100;

    // Clear the DF
    instructions.add(ReilHelpers.createStr(baseOffset, OperandSize.BYTE, "0", OperandSize.BYTE,
        Helpers.DIRECTION_FLAG));
  }

}
