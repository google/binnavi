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
import com.google.security.zynamics.reil.translators.IInstructionTranslator;
import com.google.security.zynamics.reil.translators.ITranslationEnvironment;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.TranslationHelpers;
import com.google.security.zynamics.zylib.disassembly.IInstruction;


/**
 * Translates LEAVE instructions to REIL code.
 */
public class LeaveTranslator implements IInstructionTranslator {

  /**
   * Translates a LEAVE instruction to REIL code.
   * 
   * @param environment A valid translation environment.
   * @param instruction The LEAVE instruction to translate.
   * @param instructions The generated REIL code will be added to this list
   * 
   * @throws InternalTranslationException if any of the arguments are null the passed instruction is
   *         not a LEAVE instruction
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "leave");
    Preconditions.checkArgument(instruction.getOperands().size() == 0,
        "Error: Argument instruction is not a leave instruction (invalid number of operands)");
    final long baseOffset = instruction.getAddress().toLong() * 0x100;

    final OperandSize archSize = environment.getArchitectureSize();
    final OperandSize resultSize = TranslationHelpers.getNextSize(archSize);

    final String truncateMask = String.valueOf(TranslationHelpers.getAllBitsMask(archSize));

    final String addResult = environment.getNextVariableString();

    instructions.add(ReilHelpers.createStr(baseOffset, archSize, "rbp", archSize, "rsp"));
    instructions.add(ReilHelpers.createLdm(baseOffset + 1, archSize, "rsp", archSize, "rbp"));
    instructions.add(ReilHelpers.createAdd(baseOffset + 2, archSize, "rsp", archSize, "8",
        resultSize, addResult));
    instructions.add(ReilHelpers.createAnd(baseOffset + 3, resultSize, addResult, archSize,
        truncateMask, archSize, "rsp"));
  }

}
