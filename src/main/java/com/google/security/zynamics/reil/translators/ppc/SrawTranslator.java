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


public class SrawTranslator implements IInstructionTranslator {
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "sraw");

    final IOperandTreeNode targetRegister =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0);
    final IOperandTreeNode sourceRegister =
        instruction.getOperands().get(1).getRootNode().getChildren().get(0);
    final IOperandTreeNode shiftRegister =
        instruction.getOperands().get(2).getRootNode().getChildren().get(0);

    Long baseOffset = instruction.getAddress().toLong() * 0x100;

    final OperandSize dw = OperandSize.DWORD;
    final OperandSize bt = OperandSize.BYTE;

    final String shiftAmmount = environment.getNextVariableString();

    final String tmpResult1 = environment.getNextVariableString();
    final String tmpResult2 = environment.getNextVariableString();
    final String tmpResult3 = environment.getNextVariableString();
    final String tmpResult4 = environment.getNextVariableString();

    final String oneComp = environment.getNextVariableString();
    final String twoComp = environment.getNextVariableString();

    final String signBit = environment.getNextVariableString();

    final String isZeroIfEqual = environment.getNextVariableString();
    final String noBitsShiftedOut = environment.getNextVariableString();
    final String bitsShiftedOut = environment.getNextVariableString();

    // save sign value before shift for CA Flag
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, sourceRegister.getValue(), dw,
        String.valueOf(-31L), dw, signBit));

    // n <- rB[26-31]
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, shiftRegister.getValue(), dw,
        String.valueOf(0x3FL), dw, shiftAmmount));

    // computer two's complement for shift amount == - (original value)
    instructions.add(ReilHelpers.createXor(baseOffset++, dw, shiftRegister.getValue(), dw,
        String.valueOf(0xFFFFFFFFL), dw, oneComp));
    instructions.add(ReilHelpers.createAdd(baseOffset++, dw, oneComp, dw, String.valueOf(1L), dw,
        twoComp));

    // t = 0x8000 0000 >> n
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, String.valueOf(0x80000000L), dw,
        twoComp, dw, tmpResult1));

    // x >> n
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, sourceRegister.getValue(), dw,
        twoComp, dw, tmpResult2));

    // ( x >> n ) XOR t
    instructions.add(ReilHelpers.createXor(baseOffset++, dw, tmpResult2, dw, tmpResult1, dw,
        tmpResult3));

    // ( ( x >> n ) XOR t ) - t
    instructions.add(ReilHelpers.createSub(baseOffset++, dw, tmpResult3, dw, tmpResult1, dw,
        targetRegister.getValue()));

    // compute XER[CA] Flag
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpResult2, dw,
        shiftRegister.getValue(), dw, tmpResult4));
    instructions.add(ReilHelpers.createXor(baseOffset++, dw, sourceRegister.getValue(), dw,
        tmpResult4, dw, isZeroIfEqual));
    instructions.add(ReilHelpers.createBisz(baseOffset++, dw, isZeroIfEqual, dw, noBitsShiftedOut));
    instructions
        .add(ReilHelpers.createBisz(baseOffset++, dw, noBitsShiftedOut, dw, bitsShiftedOut));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, bitsShiftedOut, dw, signBit, bt,
        Helpers.XER_CARRY_BIT));

  }
}
