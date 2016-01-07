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


public class ARMSsub16Translator extends ARMBaseTranslator {
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

    final long baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);

    new Processor() {
      @Override
      protected int getResultShiftDelta() {
        return 0;
      }

      @Override
      protected String[] process(final long offset, final String[] firstTwo,
          final String[] secondTwo) {
        final String diff1 = environment.getNextVariableString();
        final String diff2 = environment.getNextVariableString();
        final String trueDiff1 = environment.getNextVariableString();
        final String trueDiff2 = environment.getNextVariableString();
        final String tmpVar1 = environment.getNextVariableString();
        final String tmpVar2 = environment.getNextVariableString();

        long baseOffset = offset;

        // do the subs
        Helpers.signedSub(baseOffset, environment, instruction, instructions, secondTwo[0],
            firstTwo[0], diff1, trueDiff1);
        baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);
        Helpers.signedSub(baseOffset, environment, instruction, instructions, secondTwo[1],
            firstTwo[1], diff2, trueDiff2);
        baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);
        
        // CPSR GE
        instructions.add(ReilHelpers.createXor(baseOffset++, dw, trueDiff1, dw,
            String.valueOf(0xFFFFL), dw, tmpVar1));
        instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpVar1, dw, String.valueOf(-15L),
            bt, "CPSR_GE_0"));
        instructions.add(ReilHelpers.createStr(baseOffset++, bt, "CPSR_GE_0", bt, "CPSR_GE_1"));

        instructions.add(ReilHelpers.createXor(baseOffset++, dw, trueDiff2, dw,
            String.valueOf(0xFFFFL), dw, tmpVar2));
        instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpVar2, dw, String.valueOf(-15L),
            bt, "CPSR_GE_2"));
        instructions.add(ReilHelpers.createStr(baseOffset++, bt, "CPSR_GE_2", bt, "CPSR_GE_3"));

        return new String[] {diff1, diff2};
      }
    }.generate(environment, baseOffset, 16, sourceRegister1, sourceRegister2, targetRegister,
        instructions);
  }

  /**
   * SSUB16{<cond>} <Rd>, <Rn>, <Rm>
   * 
   * Operation:
   * 
   * if ConditionPassed(cond) then diff = Rn[15:0] - Rm[15:0] // Signed subtraction Rd[15:0] =
   * diff[15:0] GE[1:0] = if diff >= 0 then 0b11 else 0 diff = Rn[31:16] - Rm[31:16] // Signed
   * subtraction Rd[31:16] = diff[15:0] GE[3:2] = if diff >= 0 then 0b11 else 0
   */

  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "SSUB16");
    translateAll(environment, instruction, "SSUB16", instructions);
  }
}
