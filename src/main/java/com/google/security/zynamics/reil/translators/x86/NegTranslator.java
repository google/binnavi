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
 * Translates NEG instructions to REIL code.
 */
public class NegTranslator implements IInstructionTranslator {
  /**
   * Translates an NEG instruction to REIL code.
   *
   * @param environment A valid translation environment.
   * @param instruction The NEG instruction to translate.
   * @param instructions The generated REIL code will be added to this list
   *
   * @throws InternalTranslationException if any of the arguments are null the passed instruction
   *         is not a NEG instruction
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "neg");

    if (instruction.getOperands().size() != 1) {
      throw new InternalTranslationException(
         "Error: Argument instruction is not an neg instruction (invalid number of operands)");
    }

    final long baseOffset = instruction.getAddress().toLong() * 0x100;
    long offset = baseOffset;

    // NEG instructions have exactly one operand.
    final IOperandTree operand = instruction.getOperands().get(0);

    // Load the operand.
    final TranslationResult result = Helpers.translateOperand(environment, offset, operand, true);

   // Adjust the offset of the next REIL instruction.
   offset = baseOffset + instructions.size();

    final String operandRegister = result.getRegister();

    final OperandSize size = result.getSize();
    final OperandSize resultSize = TranslationHelpers.getNextSize(size);

    final String msbMask = String.valueOf(TranslationHelpers.getMsbMask(size));
    final String truncateMask = String.valueOf(TranslationHelpers.getAllBitsMask(size));
    final String shiftValue = String.valueOf(TranslationHelpers.getShiftMsbLsbMask(size));

    final String targetIsZero = environment.getNextVariableString();
    final String msbTarget = environment.getNextVariableString();
    final String negResult = environment.getNextVariableString();
    final String msbResult = environment.getNextVariableString();
    final String tempOf = environment.getNextVariableString();
    final String truncatedResult = environment.getNextVariableString();

    // CF = ( original value == 0 ? 0 : 1 )
    instructions.add(
        ReilHelpers.createBisz(offset, size, operandRegister, OperandSize.BYTE, targetIsZero));
    instructions.add(
        ReilHelpers.createBisz(offset + 1, OperandSize.BYTE, targetIsZero, OperandSize.BYTE,
                               Helpers.CARRY_FLAG));

    // Isolate the MSB of the original value
    instructions.add(
        ReilHelpers.createAnd(offset + 2, size, operandRegister, size, msbMask, size, msbTarget));

    // Negate the value
    instructions.add(
        ReilHelpers.createSub(offset + 3, size, "0", size, operandRegister, resultSize, negResult));

    // Isolate the MSB of the result and write it into SF
    instructions.add(
        ReilHelpers.createAnd(offset + 4, resultSize, negResult, size, msbMask, size, msbResult));
    instructions.add(
        ReilHelpers.createBsh(offset + 5, size, msbResult, size, shiftValue, OperandSize.BYTE,
                              Helpers.SIGN_FLAG));

    // The OF is set is the original value was the lowest negative value of the target
    // Example: EAX => OF is set if value was 0x80000000
    // If that happens, the MSB of the operand and the result must both be set.
    instructions.add(
        ReilHelpers.createAnd(offset + 6, size, msbTarget, size, msbResult, size, tempOf));
    instructions.add(
        ReilHelpers.createBsh(offset + 7, size, tempOf, size, shiftValue, OperandSize.BYTE,
                              Helpers.OVERFLOW_FLAG));

    // Make sure the result does not overflow
    instructions.add(
        ReilHelpers.createAnd(offset + 8, resultSize, negResult, size, truncateMask, size,
                              truncatedResult));

    // Set the ZF according to the result
    instructions.add(
        ReilHelpers.createBisz(offset + 9, size, truncatedResult, OperandSize.BYTE,
                               Helpers.ZERO_FLAG));

    // Write the truncated result back to the operand
    Helpers.writeBack(environment, offset + 10, operand, truncatedResult, size,
                      result.getAddress(), result.getType(), instructions);
  }
}
