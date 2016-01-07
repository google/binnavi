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
 * Translates LAHF instructions to REIL code.
 */
public class BswapTranslator implements IInstructionTranslator {

  /**
   * Translates a BSWAP instruction to REIL code.
   * 
   * @param environment A valid translation environment.
   * @param instruction The BSWAP instruction to translate.
   * @param instructions The generated REIL code will be added to this list
   * 
   * @throws InternalTranslationException if any of the arguments are null the passed instruction is
   *         not a BSWAP instruction
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {

    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "bswap");

    if (instruction.getOperands().size() != 1) {
      throw new InternalTranslationException(
          "Error: Argument instruction is not a lahf instruction (invalid number of operands)");
    }

    final long baseOffset = instruction.getAddress().toLong() * 0x100;

    final OperandSize archSize = environment.getArchitectureSize();

    final String operand =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0).getValue();

    final String masked1st = environment.getNextVariableString();
    final String masked2nd = environment.getNextVariableString();
    final String masked3rd = environment.getNextVariableString();
    final String masked4th = environment.getNextVariableString();
    final String shifted1st = environment.getNextVariableString();
    final String shifted2nd = environment.getNextVariableString();
    final String shifted3rd = environment.getNextVariableString();
    final String shifted4th = environment.getNextVariableString();
    final String combined1 = environment.getNextVariableString();
    final String combined2 = environment.getNextVariableString();

    instructions.add(ReilHelpers.createAnd(baseOffset + 0, archSize, operand, archSize, "255",
        archSize, masked1st));
    instructions.add(ReilHelpers.createAnd(baseOffset + 1, archSize, operand, archSize, "65280",
        archSize, masked2nd));
    instructions.add(ReilHelpers.createAnd(baseOffset + 2, archSize, operand, archSize, "16711680",
        archSize, masked3rd));
    instructions.add(ReilHelpers.createAnd(baseOffset + 3, archSize, operand, archSize,
        "4278190080", archSize, masked4th));

    instructions.add(ReilHelpers.createBsh(baseOffset + 4, archSize, masked1st, archSize, "24",
        archSize, shifted1st));
    instructions.add(ReilHelpers.createBsh(baseOffset + 5, archSize, masked2nd, archSize, "8",
        archSize, shifted2nd));
    instructions.add(ReilHelpers.createBsh(baseOffset + 6, archSize, masked3rd, archSize, "-8",
        archSize, shifted3rd));
    instructions.add(ReilHelpers.createBsh(baseOffset + 7, archSize, masked4th, archSize, "-24",
        archSize, shifted4th));

    instructions.add(ReilHelpers.createOr(baseOffset + 8, archSize, shifted1st, archSize,
        shifted2nd, archSize, combined1));
    instructions.add(ReilHelpers.createOr(baseOffset + 9, archSize, shifted3rd, archSize,
        shifted4th, archSize, combined2));

    instructions.add(ReilHelpers.createOr(baseOffset + 10, archSize, combined1, archSize,
        combined2, archSize, operand));
  }

}
