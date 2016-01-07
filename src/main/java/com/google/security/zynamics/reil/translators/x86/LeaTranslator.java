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
import com.google.security.zynamics.reil.OperandType;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.translators.IInstructionTranslator;
import com.google.security.zynamics.reil.translators.ITranslationEnvironment;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.TranslationHelpers;
import com.google.security.zynamics.reil.translators.TranslationResult;
import com.google.security.zynamics.reil.translators.TranslationResultType;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.disassembly.IOperandTree;

import java.util.List;

/**
 * Translates LEA instructions to REIL code.
 */
public class LeaTranslator implements IInstructionTranslator {

  // TODO: Check the code again

  /**
   * Translates a LEA instruction to REIL code.
   * 
   * @param environment A valid translation environment.
   * @param instruction The LEA instruction to translate.
   * @param instructions The generated REIL code will be added to this list
   * 
   * @throws InternalTranslationException if any of the arguments are null the passed instruction is
   *         not a LAHF instruction
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "lea");

    if (instruction.getOperands().size() != 2) {
      throw new InternalTranslationException(
          "Error: Argument instruction is not a lea instruction (invalid number of operands)");
    }

    final long baseOffset = instruction.getAddress().toLong() * 0x100;
    long offset = baseOffset;

    final List<? extends IOperandTree> operands = instruction.getOperands();

    final IOperandTree targetOperand = operands.get(0);
    final IOperandTree sourceOperand = operands.get(1);

    // The first operand must be a register.
    final String destination = Helpers.getLeafValue(targetOperand.getRootNode());

    final OperandSize size = Helpers.getOperandSize(targetOperand);

    // Load the operand.
    final TranslationResult sourceResult =
        Helpers.translateOperand(environment, offset, sourceOperand, false);

    String sourceRegister =
        sourceResult.getRegister() != null ? sourceResult.getRegister() : sourceResult.getAddress();

    sourceResult.getType();
    final List<ReilInstruction> sourceInstructions = sourceResult.getInstructions();

    // The source operand must always be loaded.
    instructions.addAll(sourceInstructions);

    // Adjust the offset of the next REIL instruction
    offset = baseOffset + instructions.size();

    if (size == OperandSize.WORD) {

      // Destination size is a sub-register

      final OperandType operandType = OperandType.getOperandType(sourceRegister);

      // Source operand of WORD size must be truncated.
      // Example: lea ax, 0x401000 (truncate to 0x1000)

      if (operandType == OperandType.INTEGER_LITERAL) {

        // Integer literals can be truncated directly.
        sourceRegister = String.valueOf(Long.valueOf(sourceRegister) & 0xFFFF);

      } else if (operandType == OperandType.REGISTER) {

        // Registers must be truncated later
        // => Add an AND instruction that truncates.

        final String truncatedValue = environment.getNextVariableString();

        final OperandSize registerSize =
            sourceInstructions.size() == 0 ? Helpers.getRegisterSize(sourceRegister) : environment
                .getArchitectureSize();

        // Add the truncating instruction
        instructions.add(ReilHelpers.createAnd(offset, registerSize, sourceRegister,
            OperandSize.WORD, "65535", OperandSize.WORD, truncatedValue));
        offset++;

        sourceRegister = truncatedValue;
      } else {
        // Shouldn't be possible.
        assert false;
      }

      // Write the loaded value into the destination register.
      Helpers.writeBack(environment, offset, targetOperand, sourceRegister, size, null,
          TranslationResultType.REGISTER, instructions);
    } else if (size == OperandSize.DWORD) {

      // Destination is a DWORD register

      // Handling DWORD values is easier. Just add a STR
      // instruction that writes the loaded source value
      // into the destination register.

      instructions.add(ReilHelpers.createStr(offset, size, sourceRegister, size, destination));

      // instructions.addAll(Helpers.writeBack(environment, offset, targetOperand, sourceRegister,
      // size, null, TranslationResultType.REGISTER));
    } else {
      assert false;
    }
  }

}
