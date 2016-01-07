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


public class THUMBStrTranslator extends ARMBaseTranslator {
  @Override
  protected void translateCore(final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions) {
    final IOperandTreeNode registerOperand1 =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0);

    // Hack for STR Rx, =0xSOMEADDR
    IOperandTreeNode registerOperand2 = null;
    if ((instruction.getOperands().get(1).getRootNode().getChildren().get(0).getChildren().get(0)
        .getType() == ExpressionType.IMMEDIATE_INTEGER)
        || (instruction.getOperands().get(1).getRootNode().getChildren().get(0).getChildren()
            .get(0).getType() == ExpressionType.REGISTER)) {
      registerOperand2 =
          instruction.getOperands().get(1).getRootNode().getChildren().get(0).getChildren().get(0);
    } else {
      registerOperand2 =
          instruction.getOperands().get(1).getRootNode().getChildren().get(0).getChildren().get(0)
              .getChildren().get(0);
    }

    IOperandTreeNode variableOperand1 = null;
    if (instruction.getOperands().get(1).getRootNode().getChildren().get(0).getChildren().get(0)
        .getChildren().size() == 2) {
      variableOperand1 =
          instruction.getOperands().get(1).getRootNode().getChildren().get(0).getChildren().get(0)
              .getChildren().get(1);
    }
    final String targetRegister1 = (registerOperand1.getValue());
    final String sourceRegister2 = (registerOperand2.getValue());
    final String sourceVariable1 = variableOperand1 == null ? "0" : (variableOperand1.getValue());

    final OperandSize dw = OperandSize.DWORD;

    long baseOffset = (instruction.getAddress().toLong() * 0x100) + instructions.size();

    final String tmpAddress = environment.getNextVariableString();
    final String tmpVar1 = environment.getNextVariableString();

    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, targetRegister1, dw,
        String.valueOf(0xFFFFFFFFL), dw, tmpVar1));

    instructions.add(ReilHelpers.createAdd(baseOffset++, dw, sourceRegister2, dw, sourceVariable1,
        dw, tmpAddress));

    instructions.add(ReilHelpers.createStm(baseOffset++, dw, tmpVar1, dw, tmpAddress));

  }

  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "STR");
    translateAll(environment, instruction, "STR", instructions);
  }
}
