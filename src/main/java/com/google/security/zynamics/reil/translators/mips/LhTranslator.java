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

public class LhTranslator implements IInstructionTranslator {
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    // TODO final String todo64 = "";
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "lh");

    final String rt =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0).getValue();

    final long baseOffset = ReilHelpers.toReilAddress(instruction.getAddress()).toLong();
    long offset = baseOffset;

    final String result =
        SignExtendGenerator.extendAndAdd(offset, environment, instruction.getOperands().get(1),
            instructions);
    offset = baseOffset + instructions.size();

    final OperandSize dw = OperandSize.DWORD;
    final OperandSize wd = OperandSize.WORD;

    final String loadedHalfword = environment.getNextVariableString();

    instructions.add(ReilHelpers.createLdm(offset++, dw, result, wd, loadedHalfword));

    final String extendedByte =
        SignExtendGenerator.extend16BitTo32(offset, environment, loadedHalfword, instructions);
    offset = baseOffset + instructions.size();

    instructions.add(ReilHelpers.createStr(offset, dw, extendedByte, dw, rt));
  }
}
