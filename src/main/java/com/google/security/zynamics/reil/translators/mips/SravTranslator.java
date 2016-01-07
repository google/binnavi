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
package com.google.security.zynamics.reil.translators.mips;

import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.translators.IInstructionTranslator;
import com.google.security.zynamics.reil.translators.ITranslationEnvironment;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.TranslationHelpers;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.disassembly.IOperandTree;
import com.google.security.zynamics.zylib.general.Triple;

import java.util.List;


public class SravTranslator implements IInstructionTranslator {
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "srav");

    final Triple<IOperandTree, IOperandTree, IOperandTree> operands =
        OperandLoader.loadDuplicateFirst(instruction);
    final String rd = operands.first().getRootNode().getChildren().get(0).getValue();
    final String rt = operands.second().getRootNode().getChildren().get(0).getValue();
    final String rs = operands.third().getRootNode().getChildren().get(0).getValue();

    final OperandSize dw = OperandSize.DWORD;

    final long baseOffset = ReilHelpers.toReilAddress(instruction.getAddress()).toLong();
    long offset = baseOffset;

    final String tempOperand1 = environment.getNextVariableString();
    final String tempOperand2 = environment.getNextVariableString();
    final String tempOperand3 = environment.getNextVariableString();
    final String tempOperand4 = environment.getNextVariableString();
    final String subRs = environment.getNextVariableString();

    instructions.add(ReilHelpers.createSub(offset++, dw, String.valueOf(0), dw, rs, dw, subRs));
    instructions.add(ReilHelpers.createBsh(offset++, dw, String.valueOf(0x80000000L), dw, subRs,
        dw, tempOperand1));
    instructions.add(ReilHelpers.createBsh(offset++, dw, rt, dw, subRs, dw, tempOperand2));
    instructions.add(ReilHelpers.createXor(offset++, dw, tempOperand1, dw, tempOperand2, dw,
        tempOperand3));
    instructions.add(ReilHelpers.createSub(offset++, dw, tempOperand3, dw, tempOperand1, dw,
        tempOperand4));
    instructions.add(ReilHelpers.createAnd(offset, dw, tempOperand4, dw,
        String.valueOf(0xFFFFFFFFL), dw, rd));
  }
}
