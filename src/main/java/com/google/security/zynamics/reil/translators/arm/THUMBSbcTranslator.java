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


public class THUMBSbcTranslator extends ARMBaseTranslator {
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
    final OperandSize wd = OperandSize.WORD;
    final OperandSize bt = OperandSize.BYTE;

    long baseOffset = (instruction.getAddress().toLong() * 0x100) + instructions.size();

    final String notCFlag = environment.getNextVariableString();
    final String tmpVar1 = environment.getNextVariableString();
    final String tmpVar2 = environment.getNextVariableString();
    final String tmpVar3 = environment.getNextVariableString();
    final String tmpVar4 = environment.getNextVariableString();

    instructions.add(ReilHelpers.createBisz(baseOffset++, bt, "C", bt, notCFlag));
    instructions.add(ReilHelpers.createSub(baseOffset++, dw, targetRegister, bt, notCFlag, dw,
        tmpVar1));
    instructions.add(ReilHelpers.createSub(baseOffset++, dw, tmpVar1, bt, sourceRegister, dw,
        tmpVar2));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, tmpVar2, dw,
        String.valueOf(0xFFFFFFFFL), dw, targetRegister));

    // N Flag
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, targetRegister, wd,
        String.valueOf(-31), bt, "N"));

    // Z Flag
    instructions.add(ReilHelpers.createBisz(baseOffset++, dw, targetRegister, bt, "Z"));

    // C Flag
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpVar2, wd, String.valueOf(-31L), bt,
        tmpVar3));
    instructions.add(ReilHelpers.createAnd(baseOffset++, bt, tmpVar3, bt, String.valueOf(1L), bt,
        tmpVar4));
    instructions.add(ReilHelpers.createBisz(baseOffset++, bt, tmpVar4, bt, "C"));

    // V Flag
    Helpers.subOverflow(baseOffset, environment, instruction, instructions, dw, tmpVar1, bt,
        sourceRegister, dw, tmpVar2, "V", 32);
  }

  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "SBC");
    translateAll(environment, instruction, "SBC", instructions);
  }
}
