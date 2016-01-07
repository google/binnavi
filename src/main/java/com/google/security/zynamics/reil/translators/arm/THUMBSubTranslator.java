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
import com.google.security.zynamics.zylib.disassembly.ExpressionType;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.disassembly.IOperandTreeNode;

import java.util.List;


public class THUMBSubTranslator extends ARMBaseTranslator {
  @Override
  protected void translateCore(final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions) {
    final IOperandTreeNode operand1 =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0);
    final IOperandTreeNode operand2 =
        instruction.getOperands().get(1).getRootNode().getChildren().get(0);
    final IOperandTreeNode operand3 =
        instruction.getOperands().size() == 3 ? instruction.getOperands().get(2).getRootNode()
            .getChildren().get(0) : null;

    final String sourceRegister1 = (operand1.getValue());
    final String sourceRegister2 = (operand2.getValue());
    final String sourceRegister3 =
        instruction.getOperands().size() == 3 ? (operand3.getValue()) : null;

    final OperandSize bt = OperandSize.BYTE;
    final OperandSize wd = OperandSize.WORD;
    final OperandSize dw = OperandSize.DWORD;
    final OperandSize qw = OperandSize.QWORD;

    long baseOffset = (instruction.getAddress().toLong() * 0x100) + instructions.size();

    final String tmpVar1 = environment.getNextVariableString();
    final String tmpVar2 = environment.getNextVariableString();
    final String tmpVar3 = environment.getNextVariableString();

    if (instruction.getOperands().size() == 3) {
      if ((operand1.getType() == ExpressionType.REGISTER)
          && (operand2.getType() == ExpressionType.REGISTER)
          && (operand3.getType() == ExpressionType.REGISTER)) {
        /**
         * SUB <Rd>, <Rn>, <Rm>
         * 
         * Operation:
         * 
         * Rd = Rn - Rm N Flag = Rd[31] Z Flag = if Rd == 0 then 1 else 0 C Flag = NOT BorrowFrom(Rn
         * - Rm) V Flag = OverflowFrom(Rn - Rm)
         */
        instructions.add(ReilHelpers.createSub(baseOffset++, dw, sourceRegister2, dw,
            sourceRegister3, qw, tmpVar1));
        instructions.add(ReilHelpers.createAnd(baseOffset++, qw, tmpVar1, dw,
            String.valueOf(0xFFFFFFFFL), dw, sourceRegister1));
        if (instruction.getMnemonic().contains("SUBS")) {
          // N
          instructions.add(ReilHelpers.createBsh(baseOffset++, dw, sourceRegister1, wd,
              String.valueOf(-31L), bt, "N"));

          // Z
          instructions.add(ReilHelpers.createBisz(baseOffset++, dw, sourceRegister1, bt, "Z"));

          // C Flag NOT BorrowFrom(Rn - shifter_operand)
          instructions.add(ReilHelpers.createBsh(baseOffset++, qw, tmpVar1, wd,
              String.valueOf(-31L), bt, tmpVar2));
          instructions.add(ReilHelpers.createAnd(baseOffset++, bt, tmpVar2, bt, String.valueOf(1L),
              bt, tmpVar3));
          instructions.add(ReilHelpers.createBisz(baseOffset++, bt, tmpVar3, bt, "C"));

          // V
          Helpers.subOverflow(baseOffset, environment, instruction, instructions, dw,
              sourceRegister2, dw, sourceRegister3, bt, tmpVar2, "V", 32);
        }
      } else if ((operand1.getType() == ExpressionType.REGISTER)
          && (operand2.getType() == ExpressionType.REGISTER)
          && (operand3.getType() == ExpressionType.IMMEDIATE_INTEGER)) {
        /**
         * SUB <Rd>, <Rn>, #<immed_3>
         * 
         * Operation:
         * 
         * Rd = Rn - immed_3 N Flag = Rd[31] Z Flag = if Rd == 0 then 1 else 0 C Flag = NOT
         * BorrowFrom(Rn - immed_3) V Flag = OverflowFrom(Rn - immed_3)
         */

        instructions.add(ReilHelpers.createSub(baseOffset++, dw, sourceRegister2, dw,
            sourceRegister3, qw, tmpVar1));
        instructions.add(ReilHelpers.createAnd(baseOffset++, qw, tmpVar1, dw,
            String.valueOf(0xFFFFFFFFL), dw, sourceRegister1));
        if (instruction.getMnemonic().contains("SUBS")) {
          // N
          instructions.add(ReilHelpers.createBsh(baseOffset++, dw, sourceRegister1, wd,
              String.valueOf(-31L), bt, "N"));

          // Z
          instructions.add(ReilHelpers.createBisz(baseOffset++, dw, sourceRegister1, bt, "Z"));

          // C Flag NOT BorrowFrom(Rn - shifter_operand)
          instructions.add(ReilHelpers.createBsh(baseOffset++, qw, tmpVar1, wd,
              String.valueOf(-31L), bt, tmpVar2));
          instructions.add(ReilHelpers.createAnd(baseOffset++, bt, tmpVar2, bt, String.valueOf(1L),
              bt, tmpVar3));
          instructions.add(ReilHelpers.createBisz(baseOffset++, bt, tmpVar3, bt, "C"));

          // V
          Helpers.subOverflow(baseOffset, environment, instruction, instructions, dw,
              sourceRegister2, dw, sourceRegister3, bt, tmpVar2, "V", 32);
        }
      }
    } else if (instruction.getOperands().size() == 2) {
      if ((sourceRegister2).equalsIgnoreCase("SP")) {
        /**
         * SUB SP, #<immed_7> * 4
         * 
         * Operation:
         * 
         * SP = SP - (immed_7 << 2)
         */

        instructions.add(ReilHelpers.createSub(baseOffset++, dw, sourceRegister1, dw,
            sourceRegister2, qw, tmpVar1));
        instructions.add(ReilHelpers.createAnd(baseOffset++, qw, tmpVar1, dw,
            String.valueOf(0xFFFFFFFFL), dw, sourceRegister1));

      } else {
        /**
         * SUB <Rd>, #<immed_8>
         * 
         * Operation:
         * 
         * Rd = Rd - immed_8 N Flag = Rd[31] Z Flag = if Rd == 0 then 1 else 0 C Flag = NOT
         * BorrowFrom(Rd - immed_8) V Flag = OverflowFrom(Rd - immed_8)
         */

        instructions.add(ReilHelpers.createSub(baseOffset++, dw, sourceRegister1, dw,
            sourceRegister2, qw, tmpVar1));
        instructions.add(ReilHelpers.createAnd(baseOffset++, qw, tmpVar1, dw,
            String.valueOf(0xFFFFFFFFL), dw, sourceRegister1));
        if (instruction.getMnemonic().contains("SUBS")) {
          // N
          instructions.add(ReilHelpers.createBsh(baseOffset++, dw, sourceRegister1, wd,
              String.valueOf(-31L), bt, "N"));

          // Z
          instructions.add(ReilHelpers.createBisz(baseOffset++, dw, sourceRegister1, bt, "Z"));

          // C Flag NOT BorrowFrom(Rn - shifter_operand)
          instructions.add(ReilHelpers.createBsh(baseOffset++, qw, tmpVar1, wd,
              String.valueOf(-31L), bt, tmpVar2));
          instructions.add(ReilHelpers.createAnd(baseOffset++, bt, tmpVar2, bt, String.valueOf(1L),
              bt, tmpVar3));
          instructions.add(ReilHelpers.createBisz(baseOffset++, bt, tmpVar3, bt, "C"));

          // V++
          Helpers.subOverflow(baseOffset, environment, instruction, instructions, dw,
              sourceRegister1, dw, sourceRegister2, bt, tmpVar2, "V", 32);
        }
      }
    }
  }

  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "SUB");
    translateAll(environment, instruction, "SUB", instructions);
  }
}
