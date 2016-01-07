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


public class ARMUsubaddxTranslator extends ARMBaseTranslator {
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

    final OperandSize bt = OperandSize.BYTE;
    final OperandSize wd = OperandSize.WORD;

    final long baseOffset = (instruction.getAddress().toLong() * 0x100) + instructions.size();

    new Processor() {
      @Override
      protected String[] process(final long offset, final String[] firstTwo,
          final String[] secondTwo) {
        final String sum1 = environment.getNextVariableString();
        final String diff1 = environment.getNextVariableString();
        final String tmpVar1 = environment.getNextVariableString();
        final String tmpVar2 = environment.getNextVariableString();

        long baseOffset = offset;

        // do the add
        instructions.add(ReilHelpers.createAdd(baseOffset++, dw, firstTwo[0], dw, secondTwo[1], dw,
            sum1));

        // do the sub
        instructions.add(ReilHelpers.createSub(baseOffset++, dw, firstTwo[1], dw, secondTwo[0], dw,
            diff1));

        // CPSR GE
        // borrow
        instructions.add(ReilHelpers.createBsh(baseOffset++, dw, diff1, wd, String.valueOf(-15L),
            bt, tmpVar1));
        instructions.add(ReilHelpers.createAnd(baseOffset++, bt, tmpVar1, bt, String.valueOf(1L),
            bt, "CPSR_GE_2"));
        instructions.add(ReilHelpers.createStr(baseOffset++, bt, "CPSR_GE_2", bt, "CPSR_GE_3"));
        // carry
        instructions.add(ReilHelpers.createBsh(baseOffset++, dw, sum1, wd, String.valueOf(-16L),
            bt, tmpVar2));
        instructions.add(ReilHelpers.createAnd(baseOffset++, bt, tmpVar2, bt, String.valueOf(1L),
            bt, "CPSR_GE_0"));
        instructions.add(ReilHelpers.createStr(baseOffset++, bt, "CPSR_GE_0", bt, "CPSR_GE_1"));

        return new String[] {sum1, diff1};
      }
    }.generate(environment, baseOffset, 16, sourceRegister1, sourceRegister2, targetRegister,
        instructions);
  }

  /**
   * USUBADDX{<cond>} <Rd>, <Rn>, <Rm>
   * 
   * Operation:
   * 
   * if ConditionPassed(cond) then diff = Rn[31:16] - Rm[15:0] // unsigned subtraction Rd[31:16] =
   * diff[15:0] GE[3:2] = if BorrowFrom(Rn[31:16] - Rm[15:0]) then 0b11 else 0 sum = Rn[15:0] +
   * Rm[31:16] // unsigned addition Rd[15:0] = sum[15:0] GE[1:0] = if CarryFrom16(Rn[15:0] +
   * Rm[31:16]) then 0b11 else 0
   */
  @Override
  public final void translate(final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions)
      throws InternalTranslationException {
    if (instruction.getMnemonic().startsWith("USAX")) {
      TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "USAX");
      translateAll(environment, instruction, "USAX", instructions);
    } else {
      TranslationHelpers.checkTranslationArguments(environment, instruction, instructions,
          "USUBADDX");
      translateAll(environment, instruction, "USUBADDX", instructions);
    }
  }
}
