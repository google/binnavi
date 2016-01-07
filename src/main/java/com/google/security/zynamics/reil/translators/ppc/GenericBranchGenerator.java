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
import com.google.security.zynamics.reil.translators.ITranslationEnvironment;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.TranslationHelpers;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.disassembly.IOperandTreeNode;

import java.util.List;



public class GenericBranchGenerator {
  public static void generate(long baseOffset, final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions,
      final String mnemonic, final String addressOperand, final boolean setLinkRegister,
      final boolean toCTR) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, mnemonic);

    final IOperandTreeNode BOOperand =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0);
    final IOperandTreeNode BIOperand =
        instruction.getOperands().get(1).getRootNode().getChildren().get(0);

    final String[] meta = setLinkRegister ? new String[] {"isCall", "true"} : new String[0];

    final String jumpOperand = environment.getNextVariableString();

    final String ctrDecrement1 = toCTR ? null : environment.getNextVariableString();
    final String ctrDecrement2 = toCTR ? null : environment.getNextVariableString();
    final String ctrDecrement3 = toCTR ? null : environment.getNextVariableString();

    final String tmpCountRegister = toCTR ? null : environment.getNextVariableString();

    final String ctrIsZero = toCTR ? null : environment.getNextVariableString();
    final String ctrIsNotZero = toCTR ? null : environment.getNextVariableString();

    final String ctrCondition1 = environment.getNextVariableString();
    final String ctrCondition2 = environment.getNextVariableString();
    final String ctrCondition3 = environment.getNextVariableString();

    final String ctrOk = toCTR ? String.valueOf(1L) : environment.getNextVariableString();

    final String condCondition1 = environment.getNextVariableString();
    final String condCondition2 = environment.getNextVariableString();
    final String condCondition3 = environment.getNextVariableString();
    final String condCondition4 = environment.getNextVariableString();
    final String condCondition5 = environment.getNextVariableString();

    final String condOk = environment.getNextVariableString();

    final OperandSize bt = OperandSize.BYTE;
    final OperandSize dw = OperandSize.DWORD;
    final OperandSize qw = OperandSize.QWORD;

    if (!toCTR) {
      // if ! BO[2]
      instructions.add(ReilHelpers.createBsh(baseOffset++, dw, String.valueOf(BOOperand), bt,
          String.valueOf(-2L), dw, ctrDecrement1));
      instructions.add(ReilHelpers.createAnd(baseOffset++, dw, ctrDecrement1, dw,
          String.valueOf(1L), dw, ctrDecrement2));
      instructions.add(ReilHelpers.createBisz(baseOffset++, dw, ctrDecrement2, bt, ctrDecrement3));

      // then CTR <- CTR - 1
      instructions.add(ReilHelpers.createSub(baseOffset++, dw, Helpers.COUNT_REGISTER, bt,
          ctrDecrement3, qw, tmpCountRegister));
      instructions.add(ReilHelpers.createAnd(baseOffset++, qw, tmpCountRegister, dw,
          String.valueOf(0xFFFFFFFFL), dw, Helpers.COUNT_REGISTER));

      // (CTR != 0)
      instructions.add(ReilHelpers.createBisz(baseOffset++, dw, Helpers.COUNT_REGISTER, bt,
          ctrIsZero));
      instructions.add(ReilHelpers.createBisz(baseOffset++, bt, ctrIsZero, bt, ctrIsNotZero));

      // BO[3]
      instructions.add(ReilHelpers.createBsh(baseOffset++, dw, String.valueOf(BOOperand), bt,
          String.valueOf(-3L), dw, ctrCondition1));
      instructions.add(ReilHelpers.createAnd(baseOffset++, dw, ctrCondition1, dw,
          String.valueOf(1L), bt, ctrCondition2));

      // ((CTR != 0) XOR BO[3])
      instructions.add(ReilHelpers.createXor(baseOffset++, bt, ctrIsNotZero, bt, ctrCondition2, bt,
          ctrCondition3));

      // BO[2] | ((CTR != 0) XOR BO[3])
      instructions.add(ReilHelpers.createOr(baseOffset++, dw, ctrDecrement2, bt, ctrCondition3, bt,
          ctrOk));
    }
    // BO[1]
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, String.valueOf(BOOperand), dw,
        String.valueOf(-1L), dw, condCondition1));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, condCondition1, dw,
        String.valueOf(1L), bt, condCondition2));

    // (CR[BI] EQIV BO[1])
    instructions.add(ReilHelpers.createXor(baseOffset++, bt,
        Helpers.getCRBit(Integer.valueOf(String.valueOf(BIOperand))), bt, condCondition2, bt,
        condCondition3));
    instructions.add(ReilHelpers.createBisz(baseOffset++, bt, condCondition3, bt, condCondition4));

    // BO[0]
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, String.valueOf(BOOperand), dw,
        String.valueOf(1L), bt, condCondition5));

    // BO[0] | (CR[BI] EQIV BO[1])
    instructions.add(ReilHelpers.createOr(baseOffset++, bt, condCondition5, bt, condCondition4, bt,
        condOk));

    // ctr_ok & cond_ok
    instructions.add(ReilHelpers.createAnd(baseOffset++, bt, ctrOk, bt, condOk, bt, jumpOperand));

    if (setLinkRegister) {
      instructions.add(ReilHelpers.createStr(baseOffset++, dw,
          String.valueOf(instruction.getAddress().toLong() + 4), dw, Helpers.LINK_REGISTER));
    }

    // do actual jump
    instructions
        .add(ReilHelpers.createJcc(baseOffset++, bt, jumpOperand, dw, addressOperand, meta));
  }
}
