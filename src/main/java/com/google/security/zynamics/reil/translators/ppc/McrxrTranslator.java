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
package com.google.security.zynamics.reil.translators.ppc;

import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.translators.IInstructionTranslator;
import com.google.security.zynamics.reil.translators.ITranslationEnvironment;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.TranslationHelpers;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.disassembly.IOperandTreeNode;

import java.util.List;


public class McrxrTranslator implements IInstructionTranslator {
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "mcrxr");

    final IOperandTreeNode targetCRField =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0);

    Long baseOffset = instruction.getAddress().toLong() * 0x100;
    final OperandSize bt = OperandSize.BYTE;

    instructions.add(ReilHelpers.createStr(baseOffset++, bt, "XERSO", bt,
        "CR" + targetCRField.getValue() + "LT"));
    instructions.add(ReilHelpers.createStr(baseOffset++, bt, "XEROV", bt,
        "CR" + targetCRField.getValue() + "GT"));
    instructions.add(ReilHelpers.createStr(baseOffset++, bt, "XERCA", bt,
        "CR" + targetCRField.getValue() + "EQ"));
    instructions.add(ReilHelpers.createStr(baseOffset++, bt, String.valueOf(0L), bt, "CR"
        + targetCRField.getValue() + "SO"));
    instructions.add(ReilHelpers.createStr(baseOffset++, bt, String.valueOf(0L), bt, "XERSO"));
    instructions.add(ReilHelpers.createStr(baseOffset++, bt, String.valueOf(0L), bt, "XEROV"));
    instructions.add(ReilHelpers.createStr(baseOffset++, bt, String.valueOf(0L), bt, "XERCA"));
  }
}
