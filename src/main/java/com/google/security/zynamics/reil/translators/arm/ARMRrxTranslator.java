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
import com.google.security.zynamics.zylib.general.Pair;

import java.util.List;

public class ARMRrxTranslator extends ARMBaseTranslator {

  @Override
  protected void translateCore(final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions) {
    long baseOffset = (instruction.getAddress().toLong() * 0x100) + instructions.size();
    final IOperandTreeNode registerOperand1 =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0);
    final IOperandTreeNode registerOperand2 =
        instruction.getOperands().get(1).getRootNode().getChildren().get(0);

    final String sourceRegister1 = (registerOperand1.getValue());
    final String sourceRegister2 = (registerOperand2.getValue());

    final Pair<String, String> result = AddressingModeOneGenerator.rrxRegister(baseOffset,
        environment, instructions, sourceRegister2);

    baseOffset = (instruction.getAddress().toLong() * 0x100) + instructions.size();

    instructions.add(ReilHelpers.createAnd(baseOffset++,
        OperandSize.DWORD,
        result.first(),
        OperandSize.DWORD,
        String.valueOf(0xFFFFFFFFL),
        OperandSize.DWORD,
        sourceRegister1));

    if (instruction.getMnemonic().contains("RRXS")) {
      ARMFlagSettingHelper.setThumbRotateFlags(instructions,
          baseOffset,
          OperandSize.BYTE,
          result.second(),
          OperandSize.DWORD,
          result.first());
    }
  }

  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "RRX");
    translateAll(environment, instruction, "RRX", instructions);
  }
}
