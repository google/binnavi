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
import com.google.security.zynamics.zylib.general.Pair;

import java.util.List;


public class DivTranslator implements IInstructionTranslator {

  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "div");

    final String sourceRegister1 =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0).getValue();
    final String sourceRegister2 =
        instruction.getOperands().get(1).getRootNode().getChildren().get(0).getValue();

    final OperandSize dw = OperandSize.DWORD;

    final long baseOffset = ReilHelpers.toReilAddress(instruction.getAddress()).toLong();
    long offset = baseOffset;

    final Pair<String, String> sourceRegister1Abs =
        Helpers.generateAbs(environment, offset, sourceRegister1, dw, instructions);
    final String sourceRegister1Absolute = sourceRegister1Abs.second();
    offset = baseOffset + instructions.size();

    final Pair<String, String> sourceRegister2Abs =
        Helpers.generateAbs(environment, offset, sourceRegister2, dw, instructions);
    final String sourceRegister2Absolute = sourceRegister2Abs.second();
    offset = baseOffset + instructions.size();

    final String divResult = environment.getNextVariableString();
    final String modResult = environment.getNextVariableString();

    instructions.add(ReilHelpers.createDiv(offset++, dw, sourceRegister1Absolute, dw,
        sourceRegister2Absolute, dw, divResult));
    instructions.add(ReilHelpers.createMod(offset++, dw, sourceRegister1Absolute, dw,
        sourceRegister2Absolute, dw, modResult));

    final String xoredSigns = environment.getNextVariableString();
    final String divToggleMask = environment.getNextVariableString();
    final String xoredDivResult = environment.getNextVariableString();

    // Find out if the two operands had different signs and adjust the result accordingly
    instructions.add(ReilHelpers.createXor(offset++, dw, sourceRegister1Abs.first(), dw,
        sourceRegister2Abs.first(), dw, xoredSigns));
    instructions.add(ReilHelpers.createSub(offset++, dw, String.valueOf(0L), dw, xoredSigns, dw,
        divToggleMask));
    instructions.add(ReilHelpers.createXor(offset++, dw, divToggleMask, dw, divResult, dw,
        xoredDivResult));
    instructions.add(ReilHelpers.createAdd(offset++, dw, xoredDivResult, dw, xoredSigns, dw, "LO"));

    final String modToggleMask = environment.getNextVariableString();
    final String xoredModResult = environment.getNextVariableString();

    instructions.add(ReilHelpers.createSub(offset++, dw, String.valueOf(0L), dw,
        sourceRegister1Abs.first(), dw, modToggleMask));
    instructions.add(ReilHelpers.createXor(offset++, dw, modToggleMask, dw, modResult, dw,
        xoredModResult));
    instructions.add(ReilHelpers.createAdd(offset++, dw, xoredModResult, dw,
        sourceRegister1Abs.first(), dw, "HI"));
  }
}
