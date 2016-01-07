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


public class ARMUsat16Translator extends ARMBaseTranslator {
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

    final String doesUnsignedSat1 = environment.getNextVariableString();
    final String doesUnsignedSat2 = environment.getNextVariableString();
    final String highResult = environment.getNextVariableString();
    final String lowResult = environment.getNextVariableString();
    final String tmpHighResult = environment.getNextVariableString();
    final String tmpLowResult = environment.getNextVariableString();
    final String tmpRm15to0 = environment.getNextVariableString();
    final String tmpRm32to16 = environment.getNextVariableString();

    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, sourceRegister, dw,
        String.valueOf(0xFFFFL), wd, tmpRm15to0));

    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, sourceRegister, wd,
        String.valueOf(-16L), wd, tmpRm32to16));

    Helpers.unsignedSat(baseOffset, environment, instruction, instructions, tmpRm15to0, tmpRm15to0,
        tmpRm15to0, "", tmpLowResult, Integer.decode(sourceImmediate), doesUnsignedSat1);
    baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);
    Helpers.unsignedSat(baseOffset, environment, instruction, instructions, tmpRm32to16,
        tmpRm32to16, tmpRm32to16, "", tmpHighResult, Integer.decode(sourceImmediate),
        doesUnsignedSat2);
    baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);

    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, tmpLowResult, dw,
        String.valueOf(0xFFFFL), dw, lowResult));

    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, tmpHighResult, dw,
        String.valueOf(0xFFFFL), dw, tmpHighResult));
    instructions.add(ReilHelpers.createBsh(baseOffset++, wd, tmpHighResult, wd, String.valueOf(16),
        dw, highResult));

    instructions.add(ReilHelpers.createOr(baseOffset++, dw, highResult, dw, lowResult, dw,
        targetRegister));
    instructions.add(ReilHelpers.createOr(baseOffset++, bt, doesUnsignedSat1, bt, doesUnsignedSat2,
        bt, "Q"));
  }

  /**
   * USAT16{<cond>} <Rd>, #<immed>, <Rm>
   * 
   * Operation:
   * 
   * if ConditionPassed(cond) then Rd[15:0] = UnsignedSat(Rm[15:0], sat_imm) // Rm[15:0] treated as
   * signed Rd[31:16] = UnsignedSat(Rm[31:16], sat_imm) // Rm[31:16] treated as signed if
   * UnsignedDoesSat(Rm[15:0], sat_imm) OR UnsignedDoesSat(Rm[31:16], sat_imm) then Q Flag = 1
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "USAT16");
    translateAll(environment, instruction, "USAT16", instructions);
  }
}
