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


public class SllvTranslator implements IInstructionTranslator {
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "sllv");

    final Triple<IOperandTree, IOperandTree, IOperandTree> operands =
        OperandLoader.loadDuplicateFirst(instruction);
    final String rd = operands.first().getRootNode().getChildren().get(0).getValue();
    final String rt = operands.second().getRootNode().getChildren().get(0).getValue();
    final String rs = operands.third().getRootNode().getChildren().get(0).getValue();

    final OperandSize dw = OperandSize.DWORD;
    final OperandSize qw = OperandSize.QWORD;

    final String lowFiveShiftBits = environment.getNextVariableString();
    final String temporaryShiftResult = environment.getNextVariableString();

    final long baseOffset = ReilHelpers.toReilAddress(instruction.getAddress()).toLong();
    long offset = baseOffset;

    instructions.add(ReilHelpers.createAnd(offset++, dw, rs, dw, String.valueOf(0x1FL), dw,
        lowFiveShiftBits));
    instructions.add(ReilHelpers.createBsh(offset++, dw, rt, dw, lowFiveShiftBits, qw,
        temporaryShiftResult));
    instructions.add(ReilHelpers.createAnd(offset, qw, temporaryShiftResult, dw,
        String.valueOf(0xFFFFFFFFL), dw, rd));
  }
}
