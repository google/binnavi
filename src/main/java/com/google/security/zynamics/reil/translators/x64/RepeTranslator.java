/*
Copyright 2015 Google Inc. All Rights Reserved.

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
package com.google.security.zynamics.reil.translators.x64;

import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.translators.IInstructionTranslator;
import com.google.security.zynamics.reil.translators.ITranslationEnvironment;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.TranslationHelpers;
import com.google.security.zynamics.zylib.disassembly.IInstruction;

import java.util.ArrayList;
import java.util.List;


public class RepeTranslator implements IInstructionTranslator {
  private final IStringInstructionGenerator translator;
  private final OperandSize operandSize;

  public RepeTranslator(final IStringInstructionGenerator translator, final OperandSize operandSize) {
    this.translator = translator;
    this.operandSize = operandSize;
  }

  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    final long baseOffset = ReilHelpers.toReilAddress(instruction.getAddress()).toLong();
    final long offset = baseOffset;

    final OperandSize archSize = Helpers.ArchitectureSize;

    final String invertedEcx = environment.getNextVariableString();
    final List<ReilInstruction> innerInstructions = new ArrayList<ReilInstruction>();

    translator.generate(environment,
        ReilHelpers.toReilAddress(instruction.getAddress()).toLong() + 2, operandSize,
        innerInstructions);

    final String firstInstruction = String.format("%d.0", instruction.getAddress().toLong());

    final String jmpGoal =
        String.format("%d.%d", instruction.getAddress().toLong(), innerInstructions.size() + 6);
    instructions
        .add(ReilHelpers.createBisz(offset, archSize, "rcx", OperandSize.BYTE, invertedEcx));
    instructions.add(ReilHelpers.createJcc(offset + 1, OperandSize.BYTE, invertedEcx,
        OperandSize.ADDRESS, jmpGoal));

    instructions.addAll(innerInstructions);

    final String decrementedEcx = environment.getNextVariableString();

    final String truncateMask =
        String.valueOf(TranslationHelpers.getAllBitsMask(archSize));

    instructions.add(ReilHelpers.createSub(baseOffset + instructions.size(), OperandSize.QWORD,
        "rcx", OperandSize.QWORD, "1", OperandSize.OWORD, decrementedEcx));
    instructions.add(ReilHelpers.createAnd(baseOffset + instructions.size(), OperandSize.OWORD,
        decrementedEcx, OperandSize.QWORD, truncateMask, OperandSize.QWORD, "rcx"));
    instructions.add(ReilHelpers.createJcc(baseOffset + instructions.size(), OperandSize.BYTE,
        "ZF", OperandSize.ADDRESS, firstInstruction));
    instructions.add(ReilHelpers.createNop(baseOffset + instructions.size()));
  }
}
