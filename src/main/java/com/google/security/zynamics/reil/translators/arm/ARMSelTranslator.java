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
package com.google.security.zynamics.reil.translators.arm;

import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.translators.ITranslationEnvironment;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.TranslationHelpers;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.disassembly.IOperandTreeNode;

import java.util.List;


public class ARMSelTranslator extends ARMBaseTranslator {
  @Override
  protected final void translateCore(final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions) {
    final IOperandTreeNode registerOperand1 =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0);
    final IOperandTreeNode registerOperand2 =
        instruction.getOperands().get(1).getRootNode().getChildren().get(0);
    final IOperandTreeNode registerOperand3 =
        instruction.getOperands().get(2).getRootNode().getChildren().get(0);

    final String targetRegister = (registerOperand1.getValue());
    final String sourceRegister1 = (registerOperand2.getValue());
    final String sourceRegister2 = (registerOperand3.getValue());

    final OperandSize byteSize = OperandSize.BYTE;
    final OperandSize dWordSize = OperandSize.DWORD;

    final long baseOffset = (instruction.getAddress().toLong() * 0x100) + instructions.size();

    new Processor() {
      @Override
      protected String[] process(final long offset, final String[] firstFour,
          final String[] secondFour) {
        final String firstFourMaskFour = environment.getNextVariableString();
        final String firstFourMaskOne = environment.getNextVariableString();
        final String firstFourMaskThree = environment.getNextVariableString();
        final String firstFourMaskTwo = environment.getNextVariableString();
        final String secondFourMaskFour = environment.getNextVariableString();
        final String secondFourMaskOne = environment.getNextVariableString();
        final String secondFourMaskThree = environment.getNextVariableString();
        final String secondFourMaskTwo = environment.getNextVariableString();
        final String tmpResult1 = environment.getNextVariableString();
        final String tmpResult2 = environment.getNextVariableString();
        final String tmpResult3 = environment.getNextVariableString();
        final String tmpResult4 = environment.getNextVariableString();
        final String tmpVal1 = environment.getNextVariableString();
        final String tmpVal2 = environment.getNextVariableString();
        final String tmpVal3 = environment.getNextVariableString();
        final String tmpVal4 = environment.getNextVariableString();
        final String tmpVal5 = environment.getNextVariableString();
        final String tmpVal6 = environment.getNextVariableString();
        final String tmpVal7 = environment.getNextVariableString();
        final String tmpVal8 = environment.getNextVariableString();

        long baseOffset = offset;

        // Generate masks
        instructions.add(ReilHelpers.createSub(baseOffset++, dWordSize, String.valueOf(0),
            byteSize, "CPSR_GE_0", dWordSize, firstFourMaskOne));
        instructions.add(ReilHelpers.createXor(baseOffset++, dWordSize, firstFourMaskOne,
            dWordSize, String.valueOf(0xFFFFFFFFL), dWordSize, secondFourMaskOne));
        instructions.add(ReilHelpers.createSub(baseOffset++, dWordSize, String.valueOf(0),
            byteSize, "CPSR_GE_1", dWordSize, firstFourMaskTwo));
        instructions.add(ReilHelpers.createXor(baseOffset++, dWordSize, firstFourMaskTwo,
            dWordSize, String.valueOf(0xFFFFFFFFL), dWordSize, secondFourMaskTwo));
        instructions.add(ReilHelpers.createSub(baseOffset++, dWordSize, String.valueOf(0),
            byteSize, "CPSR_GE_2", dWordSize, firstFourMaskThree));
        instructions.add(ReilHelpers.createXor(baseOffset++, dWordSize, firstFourMaskThree,
            dWordSize, String.valueOf(0xFFFFFFFFL), dWordSize, secondFourMaskThree));
        instructions.add(ReilHelpers.createSub(baseOffset++, dWordSize, String.valueOf(0),
            byteSize, "CPSR_GE_3", dWordSize, firstFourMaskFour));
        instructions.add(ReilHelpers.createXor(baseOffset++, dWordSize, firstFourMaskFour,
            dWordSize, String.valueOf(0xFFFFFFFFL), dWordSize, secondFourMaskFour));

        // apply masks
        instructions.add(ReilHelpers.createAnd(baseOffset++, dWordSize, firstFour[0], dWordSize,
            firstFourMaskOne, dWordSize, tmpVal1));
        instructions.add(ReilHelpers.createAnd(baseOffset++, dWordSize, secondFour[0], dWordSize,
            secondFourMaskOne, dWordSize, tmpVal2));

        instructions.add(ReilHelpers.createAnd(baseOffset++, dWordSize, firstFour[1], dWordSize,
            firstFourMaskOne, dWordSize, tmpVal3));
        instructions.add(ReilHelpers.createAnd(baseOffset++, dWordSize, secondFour[1], dWordSize,
            secondFourMaskOne, dWordSize, tmpVal4));

        instructions.add(ReilHelpers.createAnd(baseOffset++, dWordSize, firstFour[2], dWordSize,
            firstFourMaskOne, dWordSize, tmpVal5));
        instructions.add(ReilHelpers.createAnd(baseOffset++, dWordSize, secondFour[2], dWordSize,
            secondFourMaskOne, dWordSize, tmpVal6));

        instructions.add(ReilHelpers.createAnd(baseOffset++, dWordSize, firstFour[2], dWordSize,
            firstFourMaskOne, dWordSize, tmpVal7));
        instructions.add(ReilHelpers.createAnd(baseOffset++, dWordSize, secondFour[2], dWordSize,
            secondFourMaskOne, dWordSize, tmpVal8));

        // get results
        instructions.add(ReilHelpers.createOr(baseOffset++, dWordSize, tmpVal1, dWordSize, tmpVal2,
            dWordSize, tmpResult1));
        instructions.add(ReilHelpers.createOr(baseOffset++, dWordSize, tmpVal3, dWordSize, tmpVal4,
            dWordSize, tmpResult2));
        instructions.add(ReilHelpers.createOr(baseOffset++, dWordSize, tmpVal5, dWordSize, tmpVal6,
            dWordSize, tmpResult3));
        instructions.add(ReilHelpers.createOr(baseOffset++, dWordSize, tmpVal7, dWordSize, tmpVal8,
            dWordSize, tmpResult4));

        return new String[] {tmpResult1, tmpResult2, tmpResult3, tmpResult4};
      }
    }.generate(environment, baseOffset, 8, sourceRegister1, sourceRegister2, targetRegister,
        instructions);
  }

  /**
   * SEL{<cond>} <Rd>, <Rn>, <Rm>
   * 
   * Operation:
   * 
   * if ConditionPassed(cond) then Rd[7:0] = if GE[0] == 1 then Rn[7:0] else Rm[7:0] Rd[15:8] = if
   * GE[1] == 1 then Rn[15:8] else Rm[15:8] Rd[23:16] = if GE[2] == 1 then Rn[23:16] else Rm[23:16]
   * Rd[31:24] = if GE[3] == 1 then Rn[31:24] else Rm[31:24]
   */

  @Override
  public final void translate(final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions)
      throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "SEL");
    translateAll(environment, instruction, "SEL", instructions);
  }
}
