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


public class ARMBlxTranslator extends ARMBaseTranslator {
  @Override
  protected void translateCore(final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions) {
    final String[] meta = new String[] {"isCall", "true"};

    final IOperandTreeNode registerOperand1 =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0);

    final String sourceRegister1 = registerOperand1.getValue();

    final OperandSize dw = OperandSize.DWORD;
    final OperandSize bt = OperandSize.BYTE;

    final String jumpOperand = environment.getNextVariableString();

    long baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);

    instructions.add(ReilHelpers.createStr(baseOffset++, dw,
        String.valueOf(instruction.getAddress().toLong() + 0x4), dw, "LR"));

    if (registerOperand1.getType() == ExpressionType.REGISTER) {
      /**
       * instruction TYPE (2)
       * 
       * BLX{<cond>} <Rm>
       * 
       * if ConditionPassed(cond) then target = Rm LR = address of instruction after the BLX
       * instruction CPSR T bit = target[0] PC = target AND 0xFFFFFFFE
       * 
       */
      instructions.add(ReilHelpers.createAnd(baseOffset++, dw, sourceRegister1, bt,
          String.valueOf(1), bt, "T"));
      instructions.add(ReilHelpers.createAnd(baseOffset++, dw, sourceRegister1, dw,
          String.valueOf(0xFFFFFFFEL), dw, jumpOperand));
    } else {
      /**
       * instruction TYPE (1)
       * 
       * BLX <target_addr>
       * 
       * LR = address of the instruction after the BLX instruction CPSR T bit = 1 PC = PC +
       * (SignExtend(signed_immed_24) << 2) + (H << 1)
       */
      instructions.add(ReilHelpers.createStr(baseOffset++, bt, String.valueOf(1), bt, "T"));
      instructions.add(ReilHelpers.createStr(baseOffset++, dw, sourceRegister1, dw, jumpOperand));
    }
    instructions.add(ReilHelpers.createJcc(baseOffset++, bt, String.valueOf(1L), dw, jumpOperand,
        meta));
  }

  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "BLX");
    translateAll(environment, instruction, "BLX", instructions);
  }
}
