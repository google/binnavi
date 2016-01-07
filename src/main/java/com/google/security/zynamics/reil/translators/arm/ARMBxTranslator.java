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


public class ARMBxTranslator extends ARMBaseTranslator {
  @Override
  protected void translateCore(final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions) {
    final IOperandTreeNode registerOperand1 =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0);

    final String sourceRegister1 = (registerOperand1.getValue());

    final String[] meta_false = new String[] {"isCall", "false"};
    final String[] meta_true = new String[] {"isCall", "true"};

    final OperandSize dw = OperandSize.DWORD;
    final OperandSize bt = OperandSize.BYTE;

    final String jumpOperand = environment.getNextVariableString();

    long baseOffset = (instruction.getAddress().toLong() * 0x100) + instructions.size();

    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, sourceRegister1, bt,
        String.valueOf(1), bt, "T"));

    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, sourceRegister1, dw,
        String.valueOf(0xFFFFFFFEL), dw, jumpOperand));

    if (sourceRegister1.equalsIgnoreCase("LR")) {
      instructions.add(ReilHelpers.createJcc(baseOffset++, bt, String.valueOf(1), dw, jumpOperand,
          meta_false));
    } else {
      instructions.add(ReilHelpers.createJcc(baseOffset++, bt, String.valueOf(1), dw, jumpOperand,
          meta_true));
    }
  }

  /**
   * BX{<cond>} <Rm>
   * 
   * if ConditionPassed(cond) then CPSR T bit = Rm[0] PC = Rm AND 0xFFFFFFFE
   * 
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "BX");
    translateAll(environment, instruction, "BX", instructions);
  }
}
