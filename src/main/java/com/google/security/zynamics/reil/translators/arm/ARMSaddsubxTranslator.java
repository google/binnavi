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


public class ARMSaddsubxTranslator extends ARMBaseTranslator {
  @Override
  protected void translateCore(final ITranslationEnvironment environment,
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

    final OperandSize bt = OperandSize.BYTE;
    final OperandSize wd = OperandSize.WORD;

    final long baseOffset = (instruction.getAddress().toLong() * 0x100) + instructions.size();

    new Processor() {
      @Override
      protected String[] process(final long offset, final String[] firstTwo,
          final String[] secondTwo) {
        final String sum1 = environment.getNextVariableString();
        final String diff1 = environment.getNextVariableString();
        final String trueDiff1 = environment.getNextVariableString();
        final String tmpVar1 = environment.getNextVariableString();
        final String tmpVar2 = environment.getNextVariableString();

        long baseOffset = offset;

        // do the adds
        instructions.add(ReilHelpers.createAdd(baseOffset++, dw, firstTwo[1], dw, secondTwo[0], dw,
            sum1));
        Helpers.signedSub(baseOffset, environment, instruction, instructions, secondTwo[1],
            firstTwo[0], diff1, trueDiff1);

        // GE[3:2] = if sum >= 0 then 0b11 else 0
        instructions.add(ReilHelpers.createXor(baseOffset++, dw, sum1, dw,
            String.valueOf(0xFFFFFFFFL), dw, tmpVar1));
        instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpVar1, wd, String.valueOf(-31),
            bt, "CPSR_GE_2"));
        instructions.add(ReilHelpers.createStr(baseOffset++, bt, "CPSR_GE_2", bt, "CPSR_GE_3"));

        // GE[1:0] = if diff >= 0 then 0b11 else 0
        instructions.add(ReilHelpers.createXor(baseOffset++, dw, diff1, dw,
            String.valueOf(0xFFFFFFFFL), dw, tmpVar2));
        instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpVar2, wd, String.valueOf(-31),
            bt, "CPSR_GE_0"));
        instructions.add(ReilHelpers.createStr(baseOffset++, bt, "CPSR_GE_0", bt, "CPSR_GE_1"));

        return new String[] {diff1, sum1};
      }
    }.generate(environment, baseOffset, 16, sourceRegister1, sourceRegister2, targetRegister,
        instructions);
  }

  /**
   * SADDSUBX{<cond>} <Rd>, <Rn>, <Rm>
   * 
   * Operation:
   * 
   * if ConditionPassed(cond) then sum = Rn[31:16] + Rm[15:0] // Signed addition Rd[31:16] =
   * sum[15:0] GE[3:2] = if sum >= 0 then 0b11 else 0 diff = Rn[15:0] - Rm[31:16] Signed subtraction
   * Rd[15:0] = diff[15:0] GE[1:0] = if diff >= 0 then 0b11 else 0
   */

  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    if (instruction.getMnemonic().startsWith("SASX")) {
      TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "SASX");
      translateAll(environment, instruction, "SASX", instructions);
    } else {
      TranslationHelpers.checkTranslationArguments(environment, instruction, instructions,
          "SADDSUBX");
      translateAll(environment, instruction, "SADDSUBX", instructions);
    }
  }
}
