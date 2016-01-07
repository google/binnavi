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


public class ARMSsat16Translator extends ARMBaseTranslator {
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
    final String sourceImmediate = (registerOperand2.getValue());
    final String sourceRegister = (registerOperand3.getValue());

    final OperandSize bt = OperandSize.BYTE;
    final OperandSize wd = OperandSize.WORD;
    final OperandSize dw = OperandSize.DWORD;

    long baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);

    final String signedDoesSat1 = environment.getNextVariableString();
    final String signedDoesSat2 = environment.getNextVariableString();
    final String tmpResultHigh = environment.getNextVariableString();
    final String tmpResultLow = environment.getNextVariableString();
    final String tmpVar1 = environment.getNextVariableString();
    final String tmpVar2 = environment.getNextVariableString();
    final String tmpVar3 = environment.getNextVariableString();

    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, sourceRegister, dw,
        String.valueOf(0xFFFFL), dw, tmpVar1));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, sourceRegister, wd,
        String.valueOf(-16L), dw, tmpVar2));

    Helpers.signedSat(baseOffset, environment, instruction, instructions, dw, tmpVar1, dw, tmpVar1,
        dw, tmpVar1, "", tmpResultLow, Integer.decode(sourceImmediate), signedDoesSat1);
    baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);

    Helpers.signedSat(baseOffset, environment, instruction, instructions, dw, tmpVar2, dw, tmpVar2,
        dw, tmpVar2, "", tmpResultHigh, Integer.decode(sourceImmediate), signedDoesSat2);
    baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, tmpResultHigh, dw,
        String.valueOf(0xFFFFL), dw, tmpResultHigh));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, tmpResultLow, dw,
        String.valueOf(0xFFFFL), dw, tmpResultLow));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpResultHigh, wd,
        String.valueOf(16L), dw, tmpVar3));
    instructions.add(ReilHelpers.createOr(baseOffset++, dw, tmpResultLow, dw, tmpVar3, dw,
        targetRegister));

    instructions.add(ReilHelpers.createOr(baseOffset++, bt, signedDoesSat1, bt, signedDoesSat2, bt,
        "Q"));
  }

  /**
   * SSAT16{<cond>} <Rd>, #<immed>, <Rm>
   * 
   * Operation:
   * 
   * if ConditionPassed(cond) then Rd[15:0] = SignedSat(Rm[15:0], sat_imm + 1) Rd[31:16] =
   * SignedSat(Rm[31:16], sat_imm + 1) if SignedDoesSat(Rm[15:0], sat_imm + 1) OR
   * SignedDoesSat(Rm[31:16], sat_imm + 1) then Q Flag = 1
   * 
   */

  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "SSAT16");
    translateAll(environment, instruction, "SSAT16", instructions);
  }
}
