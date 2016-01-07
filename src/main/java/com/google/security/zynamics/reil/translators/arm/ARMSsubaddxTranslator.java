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


public class ARMSsubaddxTranslator extends ARMBaseTranslator {
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

    final long baseOffset = (instruction.getAddress().toLong() * 0x100) + instructions.size();

    new Processor() {
      @Override
      protected String[] process(final long offset, final String[] firstTwo,
          final String[] secondTwo) {
        final String tmpResult1Not = environment.getNextVariableString();
        final String tmpResult2Not = environment.getNextVariableString();
        final String diff1 = environment.getNextVariableString();
        final String sum1 = environment.getNextVariableString();
        final String trueDiff1 = environment.getNextVariableString();

        // sign extend the operands to reflect the signed operation
        Helpers.signExtend(baseOffset, environment, instruction, instructions, dw, firstTwo[0], dw,
            firstTwo[0], 16);
        Helpers.signExtend(baseOffset, environment, instruction, instructions, dw, firstTwo[1], dw,
            firstTwo[1], 16);
        Helpers.signExtend(baseOffset, environment, instruction, instructions, dw, secondTwo[0],
            dw, secondTwo[0], 16);
        Helpers.signExtend(baseOffset, environment, instruction, instructions, dw, secondTwo[1],
            dw, secondTwo[1], 16);

        long baseOffset = offset;

        // do the add
        instructions.add(ReilHelpers.createAdd(baseOffset++, dw, firstTwo[0], dw, secondTwo[1], dw,
            sum1));

        // do the sub
        Helpers.signedSub(baseOffset, environment, instruction, instructions, secondTwo[0],
            firstTwo[1], diff1, trueDiff1);

        // / CPSR GE
        instructions.add(ReilHelpers.createXor(baseOffset++, dw, sum1, dw, String.valueOf(0xFFFFL),
            dw, tmpResult1Not));
        instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpResult1Not, dw,
            String.valueOf(-15L), bt, "CPSR_GE_0"));
        instructions.add(ReilHelpers.createStr(baseOffset++, bt, "CPSR_GE_0", bt, "CPSR_GE_1"));

        instructions.add(ReilHelpers.createXor(baseOffset++, dw, trueDiff1, dw,
            String.valueOf(0xFFFFL), dw, tmpResult2Not));
        instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpResult2Not, dw,
            String.valueOf(-15L), bt, "CPSR_GE_2"));
        instructions.add(ReilHelpers.createStr(baseOffset++, bt, "CPSR_GE_2", bt, "CPSR_GE_3"));

        return new String[] {sum1, trueDiff1};
      }
    }.generate(environment, baseOffset, 16, sourceRegister1, sourceRegister2, targetRegister,
        instructions);
  }

  /**
   * SSUBADDX{<cond>} <Rd>, <Rn>, <Rm>
   * 
   * Operation:
   * 
   * if ConditionPassed(cond) then diff = Rn[31:16] - Rm[15:0] // Signed subtraction Rd[31:16] =
   * diff[15:0] GE[3:2] = if diff >= 0 then 0b11 else 0 sum = Rn[15:0] + Rm[31:16] // Signed
   * addition Rd[15:0] = sum[15:0] GE[1:0] = if sum >= 0 then 0b11 else 0
   */

  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    if (instruction.getMnemonic().startsWith("SSAX")) {
      TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "SSAX");
      translateAll(environment, instruction, "SSAX", instructions);
    } else {
      TranslationHelpers.checkTranslationArguments(environment, instruction, instructions,
          "SSUBADDX");
      translateAll(environment, instruction, "SSUBADDX", instructions);
    }
  }
}
