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

import java.util.List;


public class MadduTranslator implements IInstructionTranslator {
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "maddu");

    final String sourceRegister1 =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0).getValue();
    final String sourceRegister2 =
        instruction.getOperands().get(1).getRootNode().getChildren().get(0).getValue();

    final OperandSize dw = OperandSize.DWORD;
    final OperandSize qw = OperandSize.QWORD;

    final long baseOffset = ReilHelpers.toReilAddress(instruction.getAddress()).toLong();
    long offset = baseOffset;

    final String multiplicationResult = environment.getNextVariableString();
    final String hiShifted = environment.getNextVariableString();
    final String hiAndLoConcatenated = environment.getNextVariableString();
    final String temporaryResult = environment.getNextVariableString();

    instructions.add(ReilHelpers.createMul(offset++, dw, sourceRegister1, dw, sourceRegister2, qw,
        multiplicationResult));

    instructions.add(ReilHelpers.createBsh(offset++, dw, "HI", dw, String.valueOf(32L), qw,
        hiShifted));
    instructions.add(ReilHelpers.createOr(offset++, qw, hiShifted, dw, "LO", qw,
        hiAndLoConcatenated));

    instructions.add(ReilHelpers.createAdd(offset++, qw, hiAndLoConcatenated, qw,
        multiplicationResult, qw, temporaryResult));

    instructions.add(ReilHelpers.createAnd(offset++, qw, temporaryResult, dw,
        String.valueOf(0xFFFFFFFFL), dw, "LO"));
    instructions.add(ReilHelpers.createBsh(offset++, qw, temporaryResult, dw, String.valueOf(-32L),
        dw, "HI"));
  }
}
