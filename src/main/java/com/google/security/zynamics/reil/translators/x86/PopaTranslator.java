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
 * Translates POPA instructions to REIL code.
 */
public class PopaTranslator implements IInstructionTranslator {

  /**
   * Translates a POPA instruction to REIL code.
   * 
   * @param environment A valid translation environment.
   * @param instruction The POPA instruction to translate.
   * @param instructions The generated REIL code will be added to this list
   * 
   * @throws InternalTranslationException if any of the arguments are null the passed instruction is
   *         not an POPA instruction
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "popa");

    if (instruction.getOperands().size() != 0) {
      throw new InternalTranslationException(
          "Error: Argument instruction is not a popa instruction (invalid number of operands)");
    }

    final long baseOffset = instruction.getAddress().toLong() * 0x100;
    long offset = baseOffset;

    Helpers.generateLoadFromStack(environment, offset, OperandSize.DWORD, "edi", instructions);
    offset = baseOffset + instructions.size();

    Helpers.generateLoadFromStack(environment, offset, OperandSize.DWORD, "esi", instructions);
    offset = baseOffset + instructions.size();

    Helpers.generateLoadFromStack(environment, offset, OperandSize.DWORD, "ebp", instructions);
    offset = baseOffset + instructions.size();

    final String newEsp = environment.getNextVariableString();

    Helpers.generateLoadFromStack(environment, offset, OperandSize.DWORD, newEsp, instructions);
    offset = baseOffset + instructions.size();

    Helpers.generateLoadFromStack(environment, offset, OperandSize.DWORD, "ebx", instructions);
    offset = baseOffset + instructions.size();

    Helpers.generateLoadFromStack(environment, offset, OperandSize.DWORD, "edx", instructions);
    offset = baseOffset + instructions.size();

    Helpers.generateLoadFromStack(environment, offset, OperandSize.DWORD, "ecx", instructions);
    offset = baseOffset + instructions.size();

    Helpers.generateLoadFromStack(environment, offset, OperandSize.DWORD, "eax", instructions);
    offset = baseOffset + instructions.size();

    instructions.add(ReilHelpers.createStr(offset, OperandSize.DWORD, newEsp, OperandSize.DWORD,
        "esp"));
  }
}
