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


public class ScTranslator implements IInstructionTranslator {
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "sc");

    final String sourceRegister =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0).getValue();

    final long baseOffset = ReilHelpers.toReilAddress(instruction.getAddress()).toLong();
    long offset = baseOffset;

    final OperandSize dw = OperandSize.DWORD;

    final String extendedValue =
        SignExtendGenerator.extendAndAdd(offset, environment, instruction.getOperands().get(1),
            instructions);

    offset = baseOffset + instructions.size();

    final String jumpCondition = environment.getNextVariableString();

    instructions.add(ReilHelpers.createBisz(offset++, dw, "LL", dw, jumpCondition));

    final String jmpGoal =
        String.format("%d.%d", instruction.getAddress().toLong(), instructions.size() + 2);
    instructions.add(ReilHelpers.createJcc(offset++, dw, jumpCondition, dw, jmpGoal));
    instructions.add(ReilHelpers.createStm(offset++, dw, sourceRegister, dw, extendedValue));

    instructions.add(ReilHelpers.createStr(offset, dw, "LL", dw, sourceRegister));
  }
}
