// Copyright 2011-2016 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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

import java.util.List;


public class DsllTranslator implements IInstructionTranslator {
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "dsll");

    final List<? extends IOperandTree> operands = instruction.getOperands();

    final String rd = operands.get(0).getRootNode().getChildren().get(0).getValue();
    final String rt =
        operands.size() == 2 ? rd : operands.get(1).getRootNode().getChildren().get(0).getValue();
    final String sa =
        operands.size() == 2 ? operands.get(1).getRootNode().getChildren().get(0).getValue()
            : operands.get(2).getRootNode().getChildren().get(0).getValue();

    final long baseOffset = ReilHelpers.toReilAddress(instruction.getAddress()).toLong();
    long offset = baseOffset;

    final OperandSize qw = OperandSize.QWORD;
    final OperandSize ow = OperandSize.OWORD;

    final String shiftedValue = environment.getNextVariableString();

    instructions.add(ReilHelpers.createBsh(offset++, qw, rt, qw, sa, ow, shiftedValue));
    instructions.add(ReilHelpers.createAnd(offset++, ow, shiftedValue, qw,
        String.valueOf(0xFFFFFFFFFFFFFFFFL), qw, rd));
  }
}
