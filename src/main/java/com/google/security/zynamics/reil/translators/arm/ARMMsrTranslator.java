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
package com.google.security.zynamics.reil.translators.arm;

import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.translators.ITranslationEnvironment;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.TranslationHelpers;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.disassembly.IOperandTreeNode;

import java.util.List;


public class ARMMsrTranslator extends ARMBaseTranslator {
  @Override
  protected void translateCore(final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions) {
    final IOperandTreeNode sourceOperand =
        instruction.getOperands().get(1).getRootNode().getChildren().get(0);

    final String sourceRegister = (sourceOperand.getValue());

    final OperandSize bt = OperandSize.BYTE;
    final OperandSize dw = OperandSize.DWORD;

    final String tmpZ = environment.getNextVariableString();
    final String tmpC = environment.getNextVariableString();
    final String tmpV = environment.getNextVariableString();
    final String tmpQ = environment.getNextVariableString();

    long baseOffset = (instruction.getAddress().toLong() * 0x100) + instructions.size();

    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, sourceRegister, dw,
        String.valueOf(-31), bt, "N"));

    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, sourceRegister, dw,
        String.valueOf(-30), bt, tmpZ));
    instructions
        .add(ReilHelpers.createAnd(baseOffset++, bt, tmpZ, bt, String.valueOf(1L), bt, "Z"));

    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, sourceRegister, dw,
        String.valueOf(-29), bt, tmpC));
    instructions
        .add(ReilHelpers.createAnd(baseOffset++, bt, tmpC, bt, String.valueOf(1L), bt, "C"));

    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, sourceRegister, dw,
        String.valueOf(-28), bt, tmpV));
    instructions
        .add(ReilHelpers.createAnd(baseOffset++, bt, tmpV, bt, String.valueOf(1L), bt, "C"));

    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, sourceRegister, dw,
        String.valueOf(-29), bt, tmpQ));
    instructions
        .add(ReilHelpers.createAnd(baseOffset++, bt, tmpQ, bt, String.valueOf(1L), bt, "C"));
  }

  /**
   * MSR{<cond>} CPSR_<fields>, #<immediate> MSR{<cond>} CPSR_<fields>, <Rm> if ConditionPassed()
   * then EncodingSpecificOperations(); if write_nzcvq then APSR.N = R[n]<31>; APSR.Z = R[n]<30>;
   * APSR.C = R[n]<29>; APSR.V = R[n]<28>; APSR.Q = R[n]<27>; if write_g then APSR.GE = R[n]<19:16>;
   * 
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "MSR");
    translateAll(environment, instruction, "MSR", instructions);
  }
}
