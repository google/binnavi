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

public class THUMBAsrTranslator extends ARMBaseTranslator {
  @Override
  protected void translateCore(final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions) {
    final IOperandTreeNode registerOperand1 =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0);
    final IOperandTreeNode registerOperand2 =
        instruction.getOperands().get(1).getRootNode().getChildren().get(0);
    final IOperandTreeNode registerOperand3 = instruction.getOperands().size() == 3 ? instruction
        .getOperands()
        .get(2)
        .getRootNode()
        .getChildren()
        .get(0)
        : null;

    final String sourceRegister1 = (registerOperand1.getValue());
    final String sourceRegister2 = (registerOperand2.getValue());
    final String sourceImmediate = registerOperand3 != null ? (registerOperand3.getValue()) : null;

    long baseOffset = (instruction.getAddress().toLong() * 0x100) + instructions.size();

    Pair<String, String> result = null;

    if (instruction.getOperands().size() == 3) {
      if (instruction.getMnemonic().endsWith(".W")) {
        result = AddressingModeOneGenerator.asrRegister(baseOffset, environment, instructions,
            sourceRegister2, sourceImmediate);
      } else {
        result = AddressingModeOneGenerator.asrImmediate(baseOffset, environment, instructions,
            sourceRegister2, sourceImmediate);
      }
    } else if (instruction.getOperands().size() == 2) {
      result = AddressingModeOneGenerator.asrRegister(baseOffset, environment, instructions,
          sourceRegister1, sourceRegister2);
    }

    baseOffset = (instruction.getAddress().toLong() * 0x100) + instructions.size();
    instructions.add(ReilHelpers.createAnd(baseOffset++,
        OperandSize.DWORD,
        result.first(),
        OperandSize.DWORD,
        String.valueOf(0xFFFFFFFFL),
        OperandSize.DWORD,
        sourceRegister1));

    if (instruction.getMnemonic().contains("ASRS")) {
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
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "ASR");
    translateAll(environment, instruction, "ASR", instructions);
  }
}
