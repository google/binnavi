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

import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.translators.IInstructionTranslator;
import com.google.security.zynamics.reil.translators.ITranslationEnvironment;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.TranslationHelpers;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.disassembly.IOperandTreeNode;

import java.util.List;


public class SlwTranslator implements IInstructionTranslator {
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "slw");

    final IOperandTreeNode targetRegister =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0);
    final IOperandTreeNode sourceRegister =
        instruction.getOperands().get(1).getRootNode().getChildren().get(0);
    final IOperandTreeNode shiftRegister =
        instruction.getOperands().get(2).getRootNode().getChildren().get(0);

    Long baseOffset = instruction.getAddress().toLong() * 0x100;

    final OperandSize dw = OperandSize.DWORD;

    final String shiftAmmount = environment.getNextVariableString();
    final String tmpResult = environment.getNextVariableString();

    /**
     * n <- rB[27-31] r <- ROTL(rS, n) if rB[26] = 0 then m <- MASK(0, 31 - n) else m <- (32)0 rA <-
     * r & m
     */

    // n <- rB[27-31]
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, shiftRegister.getValue(), dw,
        String.valueOf(0x3FL), dw, shiftAmmount));

    // r <- ROTL(rS, n)
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, sourceRegister.getValue(), dw,
        shiftAmmount, dw, tmpResult));

    // if rB[26] = 0
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, tmpResult, dw,
        String.valueOf(0xFFFFFFFFL), dw, targetRegister.getValue()));
  }
}
