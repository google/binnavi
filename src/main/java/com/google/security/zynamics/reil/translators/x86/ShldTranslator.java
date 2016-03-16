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
 * Translates SHL instructions to REIL code.
 */
public class ShldTranslator implements IInstructionTranslator {

  private String getAllButMask(final OperandSize extendedSize, final OperandSize combinedSize) {
    if (extendedSize == OperandSize.QWORD) {
      return "18446744069414584320";
    } else {
      return String.valueOf(TranslationHelpers.getAllButMask(extendedSize, combinedSize));
    }
  }

  private String getCarryMask(final OperandSize size) {
    if (size == OperandSize.QWORD) {
      return "18446744073709551616";
    } else {
      return String.valueOf(Helpers.getCarryMask(size));
    }
  }

  /**
   * Translates a SHL instruction to REIL code.
   *
   * @param environment A valid translation environment.
   * @param instruction The SHL instruction to translate.
   * @param instructions The generated REIL code will be added to this list
   *
   * @throws InternalTranslationException if any of the arguments are null the passed instruction is
   *         not an SHL instruction
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "shld");

    if (instruction.getOperands().size() != 3) {
      throw new InternalTranslationException(
          "Error: Argument instruction is not a shl instruction (invalid number of operands)");
    }

    // Distinct behaviour:
    // 1. Shift value is 0
    // 2. Shift value is 1
    // 3. Shift value is too large
    // 4. Shift value has right size

    final long baseOffset = instruction.getAddress().toLong() * 0x100;
    long offset = baseOffset;

    final List<? extends IOperandTree> operands = instruction.getOperands();

    // Load target value
    final TranslationResult firstResult =
        Helpers.translateOperand(environment, offset, operands.get(0), true);
    instructions.addAll(firstResult.getInstructions());

    offset = baseOffset + instructions.size();

    final TranslationResult secondResult =
        Helpers.translateOperand(environment, offset, operands.get(1), true);
    instructions.addAll(secondResult.getInstructions());

    offset = baseOffset + instructions.size();

    // Load shift value
    final TranslationResult thirdResult =
        Helpers.translateOperand(environment, offset, operands.get(2), true);
    instructions.addAll(thirdResult.getInstructions());

    final String truncatedShiftValue = environment.getNextVariableString();
    final String truncatedShiftValueZero = environment.getNextVariableString();

    offset = baseOffset + instructions.size();

    // Test whether the shift value is zero (leads to no operation)

    instructions.add(ReilHelpers.createAnd(offset++, thirdResult.getSize(),
        thirdResult.getRegister(), OperandSize.BYTE, "31", OperandSize.BYTE, truncatedShiftValue));
    instructions.add(ReilHelpers.createBisz(offset++, OperandSize.BYTE, truncatedShiftValue,
        OperandSize.BYTE, truncatedShiftValueZero));

    final int jumpInsertIndex1 = (int) (offset - baseOffset);
    offset++;
    // Placeholder for jcc
    // instructions.add(ReilHelpers.createJcc(offset++, OperandSize.BYTE, truncatedShiftValue,
    // OperandSize.ADDRESS, endAddress));

    // If a shift happens, AF is undefined
    instructions.add(ReilHelpers.createUndef(offset++, OperandSize.BYTE, Helpers.AUXILIARY_FLAG));

    // Test whether the shift value is too large (leads to an undefined result)

    final String sizeSubtractionResult = environment.getNextVariableString();
    final String sizeMaskingResult = environment.getNextVariableString();

    instructions.add(ReilHelpers.createSub(offset++, OperandSize.BYTE,
        String.valueOf(firstResult.getSize().getBitSize()), OperandSize.BYTE, truncatedShiftValue,
        OperandSize.WORD, sizeSubtractionResult));
    instructions.add(ReilHelpers.createAnd(offset++, OperandSize.WORD, sizeSubtractionResult,
        OperandSize.WORD, String.valueOf(0x8000), OperandSize.WORD, sizeMaskingResult));
    final int jumpInsertIndex2 = (int) (offset - baseOffset);
    offset++;
    // Placeholder for createJcc(offset++, OperandSize.WORD, sizeMaskingResult, OperandSize.ADDRESS,
    // largeValueHandler));

    // From here on, we know that the shift value is valid

    final String shiftedFirstInput = environment.getNextVariableString();
    final String shiftMask = String.valueOf(firstResult.getSize().getBitSize());
    final OperandSize combinedSize = TranslationHelpers.getNextSize(firstResult.getSize());
    final OperandSize extendedSize = TranslationHelpers.getNextSize(combinedSize);
    final String combinedSource = environment.getNextVariableString();
    final String shiftedResult = environment.getNextVariableString();

    // Combine the operands into one operand

    instructions.add(ReilHelpers.createBsh(offset++, firstResult.getSize(),
        firstResult.getRegister(), firstResult.getSize(), shiftMask, combinedSize,
        shiftedFirstInput));
    instructions.add(ReilHelpers.createOr(offset++, combinedSize, shiftedFirstInput,
        secondResult.getSize(), secondResult.getRegister(), combinedSize, combinedSource));

    // Do the shift

    instructions.add(ReilHelpers.createBsh(offset++, combinedSize, combinedSource,
        OperandSize.BYTE, truncatedShiftValue, extendedSize, shiftedResult));

    // Isolate the result

    final String isolationMask = getAllButMask(combinedSize, firstResult.getSize());
    final String isolationResult = environment.getNextVariableString();
    final String shiftedIsolationResult = environment.getNextVariableString();

    instructions.add(ReilHelpers.createAnd(offset++, extendedSize, shiftedResult, combinedSize,
        isolationMask, combinedSize, isolationResult));
    instructions.add(ReilHelpers.createBsh(offset++, combinedSize, isolationResult,
        OperandSize.BYTE, "-" + shiftMask, firstResult.getSize(), shiftedIsolationResult));

    // Set the flags (TODO: Parity)
    instructions.add(ReilHelpers.createBisz(offset++, firstResult.getSize(),
        shiftedIsolationResult, OperandSize.BYTE, Helpers.ZERO_FLAG));
    Helpers.generateSignFlagCode(environment, offset, shiftedIsolationResult,
        firstResult.getSize(), instructions);

    offset = baseOffset + instructions.size() + 2/** JCC placeholder **/
    ;

    final String tempCf = environment.getNextVariableString();
    final String carryMask = String.valueOf(getCarryMask(combinedSize));
    final String shiftCarryLsb = String.valueOf(-combinedSize.getBitSize());

    instructions.add(ReilHelpers.createAnd(offset++, extendedSize, shiftedResult, extendedSize,
        carryMask, extendedSize, tempCf));
    instructions.add(ReilHelpers.createBsh(offset++, extendedSize, tempCf, extendedSize,
        shiftCarryLsb, OperandSize.BYTE, Helpers.CARRY_FLAG));

    // Store the original input value in a temp register for OF calculation

    final String tempInput = environment.getNextVariableString();

    instructions.add(ReilHelpers.createStr(offset++, firstResult.getSize(),
        firstResult.getRegister(), firstResult.getSize(), tempInput));

    // Write the result back
    Helpers.writeBack(environment, offset, operands.get(0), shiftedIsolationResult,
        firstResult.getSize(), firstResult.getAddress(), firstResult.getType(), instructions);

    offset = baseOffset + instructions.size() + 2/** JCC placeholder **/
    ;

    // Test whether the shift value is 1

    final String shiftValueOne = environment.getNextVariableString();

    instructions.add(ReilHelpers.createSub(offset++, OperandSize.BYTE, truncatedShiftValue,
        OperandSize.BYTE, "1", OperandSize.WORD, shiftValueOne));

    final int jumpInsertIndex3 = (int) (offset - baseOffset);
    offset++;
    // Placeholder for createJcc(offset++, OperandSize.WORD, shiftValueOne, OperandSize.ADDRESS,
    // notOneHandler));

    // The shift-value was 1 => Calculate the OF

    final String xoredMsb = environment.getNextVariableString();
    final String maskedMsb = environment.getNextVariableString();
    final long msbMask = TranslationHelpers.getMsbMask(firstResult.getSize());
    final long msbShift = TranslationHelpers.getShiftMsbLsbMask(firstResult.getSize());

    instructions.add(ReilHelpers.createXor(offset++, firstResult.getSize(), tempInput,
        firstResult.getSize(), shiftedIsolationResult, firstResult.getSize(), xoredMsb));
    instructions.add(ReilHelpers.createAnd(offset++, firstResult.getSize(), xoredMsb,
        firstResult.getSize(), String.valueOf(msbMask), firstResult.getSize(), maskedMsb));
    instructions.add(ReilHelpers.createBsh(offset++, firstResult.getSize(), maskedMsb,
        OperandSize.BYTE, String.valueOf(msbShift), OperandSize.BYTE, Helpers.OVERFLOW_FLAG));

    final int jumpInsertIndex4 = (int) (offset - baseOffset);
    offset++;
    // Placeholder for createJcc(offset++, OperandSize.BYTE, "1", OperandSize.ADDRESS, endAddress));

    // Handle mod-value too large

    final String largeValueHandler =
        String.format("%d.%d", instruction.getAddress().toLong(), offset - baseOffset);

    final IOperandTree inputOperand = operands.get(0);

    if (inputOperand.getRootNode().getChildren().get(0).getType() == com.google.security.zynamics.zylib.disassembly.ExpressionType.REGISTER) {
      final String operand = Helpers.getLeafValue(inputOperand.getRootNode());

      final String undefRegister =
          Helpers.getOperandSize(inputOperand) == environment.getArchitectureSize() ? operand
              : Helpers.getParentRegister(operand);

      instructions.add(ReilHelpers.createUndef(offset++, environment.getArchitectureSize(),
          undefRegister));
    }

    instructions.add(ReilHelpers.createUndef(offset++, OperandSize.BYTE, Helpers.CARRY_FLAG));
    instructions.add(ReilHelpers.createUndef(offset++, OperandSize.BYTE, Helpers.SIGN_FLAG));
    instructions.add(ReilHelpers.createUndef(offset++, OperandSize.BYTE, Helpers.ZERO_FLAG));
    instructions.add(ReilHelpers.createUndef(offset++, OperandSize.BYTE, Helpers.PARITY_FLAG));

    // Handle shift value > 1 || shift value too large

    final String notOneHandler =
        String.format("%d.%d", instruction.getAddress().toLong(), offset - baseOffset);
    instructions.add(ReilHelpers.createUndef(offset++, OperandSize.BYTE, Helpers.OVERFLOW_FLAG));

    // The End

    final String jmpGoalEnd =
        String.format("%d.%d", instruction.getAddress().toLong(), offset - baseOffset);

    instructions.add(ReilHelpers.createNop(offset++));

    instructions.add(jumpInsertIndex1, ReilHelpers.createJcc(baseOffset + jumpInsertIndex1,
        OperandSize.BYTE, truncatedShiftValueZero, OperandSize.ADDRESS, jmpGoalEnd));
    instructions.add(jumpInsertIndex2, ReilHelpers.createJcc(baseOffset + jumpInsertIndex2,
        OperandSize.WORD, sizeMaskingResult, OperandSize.ADDRESS, largeValueHandler));
    instructions.add(jumpInsertIndex3, ReilHelpers.createJcc(baseOffset + jumpInsertIndex3,
        OperandSize.WORD, shiftValueOne, OperandSize.ADDRESS, notOneHandler));
    instructions.add(jumpInsertIndex4, ReilHelpers.createJcc(baseOffset + jumpInsertIndex4,
        OperandSize.BYTE, "1", OperandSize.ADDRESS, jmpGoalEnd));
  }
}
