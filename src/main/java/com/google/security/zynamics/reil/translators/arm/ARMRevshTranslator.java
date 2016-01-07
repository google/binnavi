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


public class ARMRevshTranslator extends ARMBaseTranslator {
  @Override
  protected void translateCore(final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions) {
    final IOperandTreeNode registerOperand1 =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0);
    final IOperandTreeNode registerOperand2 =
        instruction.getOperands().get(1).getRootNode().getChildren().get(0);

    final String targetRegister = (registerOperand1.getValue());
    final String sourceRegister = (registerOperand2.getValue());

    final OperandSize dw = OperandSize.DWORD;

    long baseOffset = (instruction.getAddress().toLong() * 0x100) + instructions.size();

    final String tmpRm15to8 = environment.getNextVariableString();
    final String tmpRm7to0 = environment.getNextVariableString();
    final String tmpVar1 = environment.getNextVariableString();
    final String tmpVar2 = environment.getNextVariableString();
    final String tmpVar3 = environment.getNextVariableString();
    final String tmpVar4 = environment.getNextVariableString();
    final String tmpVar5 = environment.getNextVariableString();

    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, sourceRegister, dw,
        String.valueOf(0x000000FFL), dw, tmpRm7to0));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpRm7to0, dw, String.valueOf(8), dw,
        tmpVar1));

    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, sourceRegister, dw,
        String.valueOf(0x0000FF00L), dw, tmpRm15to8));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpRm15to8, dw, String.valueOf(-8),
        dw, tmpVar2));
    instructions.add(ReilHelpers.createOr(baseOffset++, dw, tmpVar1, dw, tmpVar2, dw, tmpVar3));

    // signextend targetregister
    instructions.add(ReilHelpers.createAdd(baseOffset++, dw, tmpVar3, dw,
        String.valueOf(0x00008000L), dw, tmpVar4));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, tmpVar4, dw,
        String.valueOf(0x0000FFFF), dw, tmpVar5));
    instructions.add(ReilHelpers.createSub(baseOffset++, dw, tmpVar5, dw,
        String.valueOf(0x00008000), dw, targetRegister));
  }

  /**
   * REVSH{<cond>} Rd, Rm
   * 
   * Operation:
   * 
   * if ConditionPassed(cond) then Rd[15: 8] = Rm[ 7: 0] Rd[ 7: 0] = Rm[15: 8] if Rm[7] == 1 then
   * Rd[31:16] = 0xFFFF else Rd[31:16] = 0x0000
   */

  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "REVSH");
    translateAll(environment, instruction, "REVSH", instructions);
  }
}
