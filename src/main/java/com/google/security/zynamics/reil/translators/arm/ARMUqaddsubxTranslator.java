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


public class ARMUqaddsubxTranslator extends ARMBaseTranslator {
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

    final OperandSize dw = OperandSize.DWORD;

    long baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);

    final String andedRm = environment.getNextVariableString();
    final String andedRn = environment.getNextVariableString();
    final String highResult = environment.getNextVariableString();
    final String lowResult = environment.getNextVariableString();
    final String shiftedRm = environment.getNextVariableString();
    final String shiftedRn = environment.getNextVariableString();
    final String tmpHighResult = environment.getNextVariableString();
    final String tmpLowResult = environment.getNextVariableString();
    final String tmpVar1 = environment.getNextVariableString();

    // Rd[31:16] = UnsignedSat(Rn[31:16] + Rm[15:0], 16)
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, sourceRegister1, dw,
        String.valueOf(-16), dw, shiftedRn));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, sourceRegister2, dw,
        String.valueOf(0xFFFFL), dw, andedRm));
    instructions.add(ReilHelpers.createAdd(baseOffset++, dw, shiftedRn, dw, andedRm, dw,
        tmpHighResult));
    Helpers.unsignedSat(baseOffset, environment, instruction, instructions, shiftedRn, andedRm,
        tmpHighResult, "ADD", highResult, 16, "");
    baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);

    // Rd[15:0] = UnsignedSat(Rn[15:0] - Rm[31:16], 16)
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, sourceRegister1, dw,
        String.valueOf(0xFFFFL), dw, andedRn));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, sourceRegister2, dw,
        String.valueOf(-16), dw, shiftedRm));
    instructions.add(ReilHelpers.createSub(baseOffset++, dw, andedRn, dw, shiftedRm, dw,
        tmpLowResult));
    Helpers.unsignedSat(baseOffset, environment, instruction, instructions, andedRn, shiftedRm,
        tmpLowResult, "SUB", lowResult, 16, "");
    baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);

    // compute result
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, highResult, dw, String.valueOf(16),
        dw, tmpVar1));
    instructions.add(ReilHelpers.createOr(baseOffset++, dw, tmpVar1, dw, lowResult, dw,
        targetRegister));
  }

  /**
   * UQADDSUBX{<cond>} <Rd>, <Rn>, <Rm>
   * 
   * Operation:
   * 
   * if ConditionPassed(cond) then Rd[31:16] = UnsignedSat(Rn[31:16] + Rm[15:0], 16) Rd[15:0] =
   * UnsignedSat(Rn[15:0] - Rm[31:16], 16)
   * 
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    if (instruction.getMnemonic().startsWith("UQASX")) {
      TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "UQASX");
      translateAll(environment, instruction, "UQASX", instructions);
    } else {
      TranslationHelpers.checkTranslationArguments(environment, instruction, instructions,
          "UQADDSUBX");
      translateAll(environment, instruction, "UQADDSUBX", instructions);
    }
  }
}
