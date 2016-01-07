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
package com.google.security.zynamics.reil.translators.ppc;

import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.translators.IInstructionTranslator;
import com.google.security.zynamics.reil.translators.ITranslationEnvironment;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.TranslationHelpers;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.disassembly.IOperandTreeNode;

import java.util.List;


public class CmpwTranslator implements IInstructionTranslator {
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "cmpw");

    final IOperandTreeNode conditionRegister =
        (instruction.getOperands().size() == 2) ? null : instruction.getOperands().get(0)
            .getRootNode().getChildren().get(0);
    final IOperandTreeNode registerOperand1 =
        (instruction.getOperands().size() == 2) ? instruction.getOperands().get(0).getRootNode()
            .getChildren().get(0) : instruction.getOperands().get(1).getRootNode().getChildren()
            .get(0);
    final IOperandTreeNode registerOperand2 =
        (instruction.getOperands().size() == 2) ? instruction.getOperands().get(1).getRootNode()
            .getChildren().get(0) : instruction.getOperands().get(2).getRootNode().getChildren()
            .get(0);

    if (instruction.getOperands().size() == 2) {
      CmpGenerator.generate(instruction.getAddress().toLong() * 0x100, environment, instruction,
          instructions, "cmpw", "CR0", registerOperand1.getValue(), registerOperand2.getValue(),
          false);
    } else {
      CmpGenerator.generate(instruction.getAddress().toLong() * 0x100, environment, instruction,
          instructions, "cmpw", conditionRegister.getValue(), registerOperand1.getValue(),
          registerOperand2.getValue(), false);
    }
  }
}
