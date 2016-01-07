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


public class RotrvTranslator implements IInstructionTranslator {
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "rotrv");

    final Triple<IOperandTree, IOperandTree, IOperandTree> operands =
        OperandLoader.loadDuplicateFirst(instruction);
    final String rd = operands.first().getRootNode().getChildren().get(0).getValue();
    final String rt = operands.second().getRootNode().getChildren().get(0).getValue();
    final String rs = operands.third().getRootNode().getChildren().get(0).getValue();

    final OperandSize dw = OperandSize.DWORD;

    final String lowShiftRegisterBits = environment.getNextVariableString();
    final String lowRotateBits = environment.getNextVariableString();
    final String highRotateBits = environment.getNextVariableString();
    final String temporaryRotateResult = environment.getNextVariableString();
    final String negativeRt = environment.getNextVariableString();
    final String negativeShiftValue = environment.getNextVariableString();

    final long baseOffset = ReilHelpers.toReilAddress(instruction.getAddress()).toLong();
    long offset = baseOffset;

    instructions.add(ReilHelpers.createAnd(offset++, dw, rs, dw, String.valueOf(0x1FL), dw,
        lowShiftRegisterBits));
    instructions
        .add(ReilHelpers.createSub(offset++, dw, String.valueOf(0L), dw, rs, dw, negativeRt));
    instructions.add(ReilHelpers.createSub(offset++, dw, String.valueOf(32L), dw, rs, dw,
        negativeShiftValue));

    instructions.add(ReilHelpers.createBsh(offset++, dw, rt, dw, negativeRt, dw, lowRotateBits));
    instructions.add(ReilHelpers.createBsh(offset++, dw, rt, dw, negativeShiftValue, dw,
        highRotateBits));
    instructions.add(ReilHelpers.createOr(offset++, dw, lowRotateBits, dw, highRotateBits, dw,
        temporaryRotateResult));
    instructions.add(ReilHelpers.createAnd(offset++, dw, temporaryRotateResult, dw,
        String.valueOf(0xFFFFFFFFL), dw, rd));
  }
}
