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


public class ARMUsub8Translator extends ARMBaseTranslator {
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
    final OperandSize dw = OperandSize.DWORD;

    long baseOffset = (instruction.getAddress().toLong() * 0x100) + instructions.size();

    final String diff1 = environment.getNextVariableString();
    final String diff2 = environment.getNextVariableString();
    final String diff3 = environment.getNextVariableString();
    final String diff4 = environment.getNextVariableString();
    final String tmpRm15to8 = environment.getNextVariableString();
    final String tmpRm23to16 = environment.getNextVariableString();
    final String tmpRm31to24 = environment.getNextVariableString();
    final String tmpRm7to0 = environment.getNextVariableString();
    final String tmpRn15to8 = environment.getNextVariableString();
    final String tmpRn23to16 = environment.getNextVariableString();
    final String tmpRn31to24 = environment.getNextVariableString();
    final String tmpRn7to0 = environment.getNextVariableString();
    final String tmpVar1 = environment.getNextVariableString();
    final String tmpVar2 = environment.getNextVariableString();
    final String tmpVar3 = environment.getNextVariableString();
    final String tmpVar4 = environment.getNextVariableString();
    final String tmpVar5 = environment.getNextVariableString();

    // Rn
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, sourceRegister1, dw,
        String.valueOf(0x000000FFL), dw, tmpRn7to0));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, sourceRegister1, dw,
        String.valueOf(0x0000FF00L), dw, tmpVar1));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpVar1, dw, String.valueOf(-8), dw,
        tmpRn15to8));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, sourceRegister1, dw,
        String.valueOf(0x00FF0000L), dw, tmpVar2));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpVar2, dw, String.valueOf(-16), dw,
        tmpRn23to16));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, sourceRegister1, dw,
        String.valueOf(0xFF000000L), dw, tmpVar3));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpVar3, dw, String.valueOf(-24), dw,
        tmpRn31to24));

    // Rm
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, sourceRegister2, dw,
        String.valueOf(0x000000FFL), dw, tmpRm7to0));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, sourceRegister2, dw,
        String.valueOf(0x0000FF00L), dw, tmpVar1));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpVar1, dw, String.valueOf(-8), dw,
        tmpRm15to8));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, sourceRegister2, dw,
        String.valueOf(0x00FF0000L), dw, tmpVar2));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpVar2, dw, String.valueOf(-16), dw,
        tmpRm23to16));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, sourceRegister2, dw,
        String.valueOf(0xFF000000L), dw, tmpVar3));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpVar3, dw, String.valueOf(-24), dw,
        tmpRm31to24));

    // Do the Subs
    instructions.add(ReilHelpers.createSub(baseOffset++, dw, tmpRn7to0, dw, tmpRm7to0, dw, diff1));
    instructions
        .add(ReilHelpers.createSub(baseOffset++, dw, tmpRn15to8, dw, tmpRm15to8, dw, diff2));
    instructions.add(ReilHelpers.createSub(baseOffset++, dw, tmpRn23to16, dw, tmpRm23to16, dw,
        diff3));
    instructions.add(ReilHelpers.createSub(baseOffset++, dw, tmpRn31to24, dw, tmpRm31to24, dw,
        diff4));

    // CPSR GE
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, diff1, wd, String.valueOf(-7), bt,
        "CPSR_GE_0"));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, diff2, wd, String.valueOf(-7), bt,
        "CPSR_GE_1"));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, diff3, wd, String.valueOf(-7), bt,
        "CPSR_GE_2"));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, diff4, wd, String.valueOf(-7), bt,
        "CPSR_GE_3"));

    // get the results together
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, diff2, dw, String.valueOf(8), dw,
        diff2));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, diff3, dw, String.valueOf(16), dw,
        diff3));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, diff4, dw, String.valueOf(24), dw,
        diff4));

    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, diff1, dw,
        String.valueOf(0x000000FFL), dw, diff1));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, diff2, dw,
        String.valueOf(0x0000FF00L), dw, diff2));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, diff3, dw,
        String.valueOf(0x00FF0000L), dw, diff3));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, diff4, dw,
        String.valueOf(0xFF000000L), dw, diff4));

    instructions.add(ReilHelpers.createOr(baseOffset++, dw, diff1, dw, diff2, dw, tmpVar4));
    instructions.add(ReilHelpers.createOr(baseOffset++, dw, diff3, dw, diff4, dw, tmpVar5));
    instructions.add(ReilHelpers.createOr(baseOffset++, dw, tmpVar4, dw, tmpVar5, dw,
        targetRegister));
  }

  /**
   * USUB8{<cond>} <Rd>, <Rn>, <Rm>
   * 
   * Operation:
   * 
   * if ConditionPassed(cond) then Rd[7:0] = Rn[7:0] - Rm[7:0] GE[0] = NOT BorrowFrom(Rn[7:0] -
   * Rm[7:0]) Rd[15:8] = Rn[15:8] - Rm[15:8] GE[1] = NOT BorrowFrom(Rn[15:8] - Rm[15:8]) Rd[23:16] =
   * Rn[23:16] - Rm[23:16] GE[2] = NOT BorrowFrom(Rn[23:16] - Rm[23:16]) Rd[31:24] = Rn[31:24] -
   * Rm[31:24] GE[3] = NOT BorrowFrom(Rn[31:24] - Rm[31:24])
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "USUB8");
    translateAll(environment, instruction, "USUB8", instructions);
  }
}
