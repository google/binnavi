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


public class MtocrfTranslator implements IInstructionTranslator {
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "mtocrf");

    final IOperandTreeNode sourceRegister =
        instruction.getOperands().get(1).getRootNode().getChildren().get(0);
    final IOperandTreeNode crMask =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0);

    Long baseOffset = instruction.getAddress().toLong() * 0x100;

    final OperandSize bt = OperandSize.BYTE;
    final OperandSize dw = OperandSize.DWORD;

    final String maskVar1 = environment.getNextVariableString();
    final String andVar1 = environment.getNextVariableString();
    final String andVar2 = environment.getNextVariableString();
    final String andVar3 = environment.getNextVariableString();

    final String orVar1 = environment.getNextVariableString();
    final String orVar2 = environment.getNextVariableString();
    final String inverseMask = environment.getNextVariableString();

    // construct mask and inverse mask
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, sourceRegister.getValue(), dw,
        Helpers.getCRM(Integer.decode(crMask.getValue())), dw, maskVar1));
    instructions.add(ReilHelpers.createXor(baseOffset++, dw,
        Helpers.getCRM(Integer.decode(crMask.getValue())), dw, String.valueOf(0xFFFFFFFFL), dw,
        inverseMask));

    for (int i = 0; i < 31; i++) {
      // compute cr & inverse mask to preserve bits in cr
      instructions.add(ReilHelpers.createBsh(baseOffset++, dw, inverseMask, bt,
          String.valueOf(i - 31), dw, andVar2));
      instructions.add(ReilHelpers.createAnd(baseOffset++, dw, andVar2, dw, String.valueOf(1L), dw,
          andVar3));
      instructions.add(ReilHelpers.createAnd(baseOffset++, bt, Helpers.getCRBit(i), bt, andVar3,
          bt, orVar1));

      // compute reg & mask to propagate only the right bits to cr
      instructions.add(ReilHelpers.createBsh(baseOffset++, dw, maskVar1, bt,
          String.valueOf(i - 31), dw, andVar1));
      instructions.add(ReilHelpers.createAnd(baseOffset++, dw, andVar1, dw, String.valueOf(1L), dw,
          orVar2));

      // fill cr
      instructions.add(ReilHelpers.createOr(baseOffset++, bt, orVar1, bt, orVar2, bt,
          Helpers.getCRBit(i)));
    }
  }
}
