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
import com.google.security.zynamics.reil.translators.TranslationResultType;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.disassembly.IOperandTree;
import com.google.security.zynamics.zylib.general.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Translates IMUL instructions to REIL code.
 */
public class ImulTranslator implements IInstructionTranslator {

  private TranslationResult generateImul(final ITranslationEnvironment environment,
      final long offset, final String operand1, final String operand2, final OperandSize size1,
      final OperandSize size2) {

    // The three steps to simulate signed multiplication using unsigned multiplication:
    // 1. Get the absolute values of the two operands
    // 2. Multiply the absolute values
    // 3. Change the sign of the result if the two operands had different signs.

    final long baseOffset = offset;
    long newOffset = baseOffset;

    final OperandSize resultSize = TranslationHelpers.getNextSize(size1);

    final ArrayList<ReilInstruction> instructions = new ArrayList<ReilInstruction>();

    final Pair<String, String> abs1 =
        Helpers.generateAbs(environment, newOffset, operand1, size1, instructions);
    newOffset = baseOffset + instructions.size();

    final Pair<String, String> abs2 =
        Helpers.generateAbs(environment, newOffset, operand2, size2, instructions);
    newOffset = baseOffset + instructions.size();

    final String lowerHalfMask = String.valueOf(TranslationHelpers.getAllBitsMask(size1));

    final String multResult = environment.getNextVariableString();
    final String xoredSigns = environment.getNextVariableString();
    final String toggleMask = environment.getNextVariableString();
    final String decResult = environment.getNextVariableString();
    final String realResult = environment.getNextVariableString();
    final String maskedLowerHalf = environment.getNextVariableString();

    // Multiply the two operands
    instructions.add(ReilHelpers.createMul(newOffset, size1, abs1.second(), size2, abs2.second(),
        resultSize, multResult));

    // Find out if the two operands had different signs and adjust the result accordingly
    instructions.add(ReilHelpers.createXor(newOffset + 1, size1, abs1.first(), size2, abs2.first(),
        size1, xoredSigns));
    instructions.add(ReilHelpers.createSub(newOffset + 2, size1, "0", size1, xoredSigns,
        resultSize, toggleMask));
    instructions.add(ReilHelpers.createSub(newOffset + 3, resultSize, multResult, size1,
        xoredSigns, resultSize, decResult));
    instructions.add(ReilHelpers.createXor(newOffset + 4, resultSize, toggleMask, resultSize,
        decResult, resultSize, realResult));

    // Extract lower half of the result
    instructions.add(ReilHelpers.createAnd(newOffset + 5, resultSize, realResult, size1,
        lowerHalfMask, size1, maskedLowerHalf));

    // Extend the sign of the lower half of the result
    final TranslationResult foo =
        Helpers.extendSign(environment, newOffset + 6, maskedLowerHalf, size1, resultSize);
    instructions.addAll(foo.getInstructions());
    newOffset = newOffset + 6 + foo.getInstructions().size();

    final String cmpResult = environment.getNextVariableString();
    final String resultsEqual = environment.getNextVariableString();

    // Compare result to sign extension of lower half
    instructions.add(ReilHelpers.createSub(newOffset, resultSize, realResult, resultSize,
        foo.getRegister(), resultSize, cmpResult));
    instructions.add(ReilHelpers.createBisz(newOffset + 1, resultSize, cmpResult, OperandSize.BYTE,
        resultsEqual));

    // Set the flags according to the result
    instructions.add(ReilHelpers.createBisz(newOffset + 2, OperandSize.BYTE, resultsEqual,
        OperandSize.BYTE, Helpers.CARRY_FLAG));
    instructions.add(ReilHelpers.createBisz(newOffset + 3, OperandSize.BYTE, resultsEqual,
        OperandSize.BYTE, Helpers.OVERFLOW_FLAG));

    instructions.add(ReilHelpers.createUndef(newOffset + 4, OperandSize.BYTE, Helpers.ZERO_FLAG));
    instructions.add(ReilHelpers.createUndef(newOffset + 5, OperandSize.BYTE,
        Helpers.AUXILIARY_FLAG));
    instructions.add(ReilHelpers.createUndef(newOffset + 6, OperandSize.BYTE, Helpers.PARITY_FLAG));

    return new TranslationResult(realResult, resultSize, TranslationResultType.REGISTER, null,
        instructions, offset);
  }

  private void generateImul3(final ITranslationEnvironment environment, final long offset,
      final OperandSize size1, final String register1, final OperandSize size2,
      final String register2, final OperandSize size3, final String register3,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    final TranslationResult result =
        generateImul(environment, offset, register2, register3, size2, size3);
    instructions.addAll(result.getInstructions());

    final String resultRegister = result.getRegister();

    writeSingleRegisterMulResult(environment, offset + result.getInstructions().size(),
        resultRegister, register1, size1, instructions);
  }

  private void translate_1(final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions)
      throws InternalTranslationException {

    final List<? extends IOperandTree> operands = instruction.getOperands();

    final long baseOffset = instruction.getAddress().toLong() * 0x100;
    long offset = baseOffset;

    // Load source operand.
    final TranslationResult firstResult =
        Helpers.translateOperand(environment, offset, operands.get(0), true);
    instructions.addAll(firstResult.getInstructions());
    offset = baseOffset + instructions.size();

    // Generate multiplication code
    final TranslationResult multResult =
        generateImul(environment, offset, "eax", firstResult.getRegister(), OperandSize.DWORD,
            firstResult.getSize());
    instructions.addAll(multResult.getInstructions());
    offset = baseOffset + instructions.size();

    final OperandSize size = Helpers.getOperandSize(operands.get(0));

    // Write the result of the multiplication back to the proper registers
    instructions
        .addAll(Helpers.writeMulResult(environment, offset, multResult.getRegister(), size));
  }

  private void translate_2(final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions)
      throws InternalTranslationException {

    final List<? extends IOperandTree> operands = instruction.getOperands();

    final long baseOffset = instruction.getAddress().toLong() * 0x100;
    long offset = baseOffset;

    // Load source operand.
    final TranslationResult firstResult =
        Helpers.translateOperand(environment, offset, operands.get(0), true);
    instructions.addAll(firstResult.getInstructions());
    offset = baseOffset + instructions.size();

    // Load second operand.
    final TranslationResult secondResult =
        Helpers.translateOperand(environment, offset, operands.get(1), true);
    instructions.addAll(secondResult.getInstructions());
    offset = baseOffset + instructions.size();

    // IMUL instructions with 2 or 3 operands must have an output register
    final OperandSize resultSize =
        OperandSize.sizeStringToValue(operands.get(0).getRootNode().getValue());
    final String resultRegister = operands.get(0).getRootNode().getChildren().get(0).getValue();

    generateImul3(environment, offset, resultSize, resultRegister, firstResult.getSize(),
        firstResult.getRegister(), secondResult.getSize(), secondResult.getRegister(), instructions);
  }

  private void translate_3(final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions)
      throws InternalTranslationException {
    final List<? extends IOperandTree> operands = instruction.getOperands();

    final long baseOffset = instruction.getAddress().toLong() * 0x100;
    long offset = baseOffset;

    // Load source operand.
    final TranslationResult firstResult =
        Helpers.translateOperand(environment, offset, operands.get(1), true);
    instructions.addAll(firstResult.getInstructions());
    offset = baseOffset + instructions.size();

    // Load second operand.
    final TranslationResult secondResult =
        Helpers.translateOperand(environment, offset, operands.get(2), true);
    instructions.addAll(secondResult.getInstructions());
    offset = baseOffset + instructions.size();

    // Load target operand.
    final TranslationResult targetResult =
        Helpers.translateOperand(environment, offset, operands.get(0), true);
    instructions.addAll(targetResult.getInstructions());
    offset = baseOffset + instructions.size();

    // IMUL instructions with 2 or 3 operands must have an output register
    final OperandSize resultSize =
        OperandSize.sizeStringToValue(operands.get(0).getRootNode().getValue());
    final String resultRegister = operands.get(0).getRootNode().getChildren().get(0).getValue();

    generateImul3(environment, offset, resultSize, resultRegister, firstResult.getSize(),
        firstResult.getRegister(), secondResult.getSize(), secondResult.getRegister(), instructions);
  }

  private void writeSingleRegisterMulResult(final ITranslationEnvironment environment,
      final long offset, final String resultRegister, final String register1,
      final OperandSize size1, final List<ReilInstruction> instructions)
      throws InternalTranslationException {

    final OperandSize archSize = environment.getArchitectureSize();

    if (size1 == archSize) {
      // The destination is a DWORD => Just write the result

      instructions.add(ReilHelpers.createAnd(offset, OperandSize.QWORD, resultRegister,
          OperandSize.DWORD, "4294967295", OperandSize.DWORD, register1));
    } else {
      Helpers.moveAndMask(environment, offset, size1, resultRegister, register1, instructions);
    }
  }

  /**
   * Translates a IMUL instruction to REIL code.
   * 
   * @param environment A valid translation environment.
   * @param instruction The IMUL instruction to translate.
   * @param instructions The generated REIL code will be added to this list
   * 
   * @throws InternalTranslationException if any of the arguments are null the passed instruction is
   *         not an IMUL instruction
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "imul");

    if ((instruction.getOperands().size() < 1) || (instruction.getOperands().size() > 3)) {
      throw new InternalTranslationException(
          "Error: Argument instruction is not a imul instruction (invalid number of operand)");
    }

    final List<? extends IOperandTree> operands = instruction.getOperands();

    if (operands.size() == 1) {
      translate_1(environment, instruction, instructions);
    } else if (operands.size() == 2) {
      translate_2(environment, instruction, instructions);
    } else {
      translate_3(environment, instruction, instructions);
    }

  }
}
