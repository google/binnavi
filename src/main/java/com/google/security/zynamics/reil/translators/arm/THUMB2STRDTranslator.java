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


public class THUMB2STRDTranslator extends ARMBaseTranslator {
  @Override
  protected void translateCore(final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions)
      throws InternalTranslationException {
    final Boolean writeBack =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0).getChildren().size() == 1
            ? true : false;
    final IOperandTreeNode registerOperand1 =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0);
    final IOperandTreeNode registerOperand2 =
        instruction.getOperands().get(1).getRootNode().getChildren().get(0);

    final IOperandTreeNode rootNode = instruction.getOperands().get(2).getRootNode();

    final String registerNodeValue1 = registerOperand1.getValue();
    final String registerNodeValue2 = registerOperand2.getValue();

    final OperandSize bt = OperandSize.BYTE;
    final OperandSize dw = OperandSize.DWORD;

    long baseOffset = (instruction.getAddress().toLong() * 0x100) + instructions.size();

    final Pair<String, String> resultPair =
        AddressingModeTwoGenerator.generate(baseOffset, environment, instruction, instructions,
            rootNode);

    final String tmpAddress = resultPair.first();
    final String tmpAddress2 = environment.getNextVariableString();

    final int registerNum = Helpers.getRegisterIndex(registerNodeValue1);

    if (((registerNum % 2) == 0) && (registerNum != 14)) {
      instructions.add(ReilHelpers.createStm(baseOffset++, dw, registerNodeValue1, dw, tmpAddress));
      instructions.add(ReilHelpers.createAdd(baseOffset++, dw, tmpAddress, bt, String.valueOf(4),
          dw, tmpAddress2));
      instructions
          .add(ReilHelpers.createStm(baseOffset++, dw, registerNodeValue2, dw, tmpAddress2));
      if (writeBack) {
        instructions.add(ReilHelpers.createStr(baseOffset++, dw, tmpAddress2, dw,
            registerNodeValue1));
      }
    } else {
      instructions.add(ReilHelpers.createUnknown(baseOffset++));
    }

  }

  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "STR");
    translateAll(environment, instruction, "STR", instructions);
  }
}
