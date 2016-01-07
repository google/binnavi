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

public class THUMBAddTranslator extends ARMBaseTranslator {
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

    final String resultRegister = (operand1.getValue());
    final String sourceRegister1 =
        instruction.getOperands().size() == 3 ? operand2.getValue() : operand1.getValue();
    final String sourceRegister2 =
        instruction.getOperands().size() == 3 ? operand3.getValue() : operand2.getValue();

    final OperandSize dw = OperandSize.DWORD;
    final OperandSize qw = OperandSize.QWORD;

    long baseOffset = (instruction.getAddress().toLong() * 0x100) + instructions.size();

    final String tmpVar2 = environment.getNextVariableString();

    instructions.add(ReilHelpers.createAdd(baseOffset++, dw, sourceRegister1, dw, sourceRegister2,
        qw, tmpVar2));
    instructions.add(ReilHelpers.createAnd(baseOffset++, qw, tmpVar2, dw,
        String.valueOf(0xFFFFFFFFL), dw, resultRegister));

    if (instruction.getMnemonic().contains("ADDS")) {
      ARMFlagSettingHelper.setThumbAddFlags(environment, instructions, baseOffset, qw, tmpVar2,
          dw, resultRegister, dw, sourceRegister1, dw, sourceRegister2);
    }
  }

  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "ADD");
    translateAll(environment, instruction, "ADD", instructions);
  }

}
