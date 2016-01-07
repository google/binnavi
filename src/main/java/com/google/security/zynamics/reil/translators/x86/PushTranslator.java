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
import com.google.security.zynamics.reil.translators.TranslationResult;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.disassembly.IOperandTree;

import java.util.List;

/**
 * Translates PUSH instructions to REIL code.
 */
public class PushTranslator implements IInstructionTranslator {
  /**
   * Translates a PUSH instruction to REIL code.
   * 
   * @param environment A valid translation environment.
   * @param instruction The PUSH instruction to translate.
   * @param instructions The generated REIL code will be added to this list
   * 
   * @throws InternalTranslationException if any of the arguments are null the passed instruction is
   *         not an PUSH instruction
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {

    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "push");
    if (instruction.getOperands().size() != 1) {
      throw new InternalTranslationException(
          "Error: Argument instruction is not a push instruction (invalid number of operands)");
    }

    final long baseOffset = instruction.getAddress().toLong() * 0x100;
    long offset = baseOffset;

    // PUSH instructions have exactly one operand.
    final IOperandTree operand = instruction.getOperands().get(0);

    // Load the operand.
    final TranslationResult result = Helpers.translateOperand(environment, offset, operand, true);

    instructions.addAll(result.getInstructions());

    // Adjust the offset of the next REIL instruction.
    offset = baseOffset + instructions.size();

    // A push with an 8-bit immediate is treated like a 32-bit push.    
    final OperandSize resultSize =
        (result.getSize() == OperandSize.BYTE) ? OperandSize.DWORD : result.getSize();
    final String loadedValue = result.getRegister();

    final String tempRegister;

    if (result.getInstructions().isEmpty() && loadedValue.equalsIgnoreCase("esp")) {
      // push esp must be handled separately

      tempRegister = environment.getNextVariableString();

      instructions.add(ReilHelpers.createStr(offset++, resultSize, loadedValue, resultSize,
          tempRegister));
    } else {
      tempRegister = loadedValue;
    }

    Helpers.generatePush(environment, offset, tempRegister, resultSize, instructions);
  }
}
