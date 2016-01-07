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


public class MsubTranslator implements IInstructionTranslator {
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "msub");

    final String sourceRegister1 =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0).getValue();
    final String sourceRegister2 =
        instruction.getOperands().get(1).getRootNode().getChildren().get(0).getValue();

    final OperandSize dw = OperandSize.DWORD;
    final OperandSize qw = OperandSize.QWORD;
    final OperandSize bt = OperandSize.BYTE;

    final long baseOffset = ReilHelpers.toReilAddress(instruction.getAddress()).toLong();
    long offset = baseOffset;

    final String signedMultiplicationResult = environment.getNextVariableString();
    final String hiShifted = environment.getNextVariableString();
    final String hiAndLoConcatenated = environment.getNextVariableString();
    final String temporaryResult = environment.getNextVariableString();
    final String temp = environment.getNextVariableString();
    final String temporaryHigh = environment.getNextVariableString();

    Helpers.signedMul(offset, environment, instructions, dw, sourceRegister1, dw, sourceRegister2,
        qw, signedMultiplicationResult);

    offset = baseOffset + instructions.size();

    instructions.add(ReilHelpers.createBsh(offset++, dw, "HI", dw, String.valueOf(32L), qw,
        hiShifted));
    instructions.add(ReilHelpers.createOr(offset++, qw, hiShifted, dw, "LO", qw,
        hiAndLoConcatenated));

    Helpers.signedSub(offset, environment, instructions, hiAndLoConcatenated,
        signedMultiplicationResult, temporaryResult, temp);
    // instructions.add(ReilHelpers.createSub(offset++, qw, hiAndLoConcatenated, qw,
    // signedMultiplicationResult, qw, temporaryResult));

    offset = baseOffset + instructions.size();

    final String resultOperand = environment.getNextVariableString();
    final String toggleMask = environment.getNextVariableString();
    final String xoredResult = environment.getNextVariableString();
    final String xoredSigns = environment.getNextVariableString();
    final String signOne = environment.getNextVariableString();
    final String signTwo = environment.getNextVariableString();
    final String realSignTwo = environment.getNextVariableString();

    // Find out if the two operands had different signs and adjust the result accordingly
    instructions.add(ReilHelpers.createBsh(offset++, dw, "HI", dw, String.valueOf(-31L), bt,
        signOne));
    instructions.add(ReilHelpers.createBsh(offset++, qw, signedMultiplicationResult, dw,
        String.valueOf(-63L), bt, signTwo));
    instructions.add(ReilHelpers.createAnd(offset++, bt, signTwo, bt, String.valueOf(1L), bt,
        realSignTwo));

    instructions.add(ReilHelpers.createXor(offset++, bt, signOne, bt, realSignTwo, dw, xoredSigns));
    instructions.add(ReilHelpers.createSub(offset++, dw, "0", dw, xoredSigns, dw, toggleMask));
    instructions.add(ReilHelpers.createXor(offset++, qw, toggleMask, qw, temporaryResult, qw,
        xoredResult));
    instructions.add(ReilHelpers.createAdd(offset++, qw, xoredResult, dw, xoredSigns, qw,
        resultOperand));

    instructions.add(ReilHelpers.createAnd(offset++, qw, resultOperand, dw,
        String.valueOf(0xFFFFFFFFL), dw, "LO"));
    instructions.add(ReilHelpers.createBsh(offset++, qw, resultOperand, dw, String.valueOf(-32L),
        dw, temporaryHigh));
    instructions.add(ReilHelpers.createAnd(offset++, qw, temporaryHigh, dw,
        String.valueOf(0xFFFFFFFFL), dw, "HI"));
  }
}
