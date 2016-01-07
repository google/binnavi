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


public class MulGenerator {
  public static void generate(long baseOffset, final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions,
      final String mnemonic, final String firstOperand, final String secondOperand,
      final boolean setCr, final boolean isSigned, final boolean isHigh, final boolean setOv)
      throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, mnemonic);

    final IOperandTreeNode registerOperand1 =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0);

    final String targetRegister = registerOperand1.getValue();

    final String tmpMulVar = environment.getNextVariableString();
    final String crTemp = setCr ? environment.getNextVariableString() : null;
    final String firstComplementVar = environment.getNextVariableString();
    final String secondComplementVar = environment.getNextVariableString();
    final String firstMSB = environment.getNextVariableString();
    final String secondMSB = environment.getNextVariableString();
    final String firstOneComplement = environment.getNextVariableString();
    final String secondOneComplement = environment.getNextVariableString();
    final String firstTwoComplement = environment.getNextVariableString();
    final String secondTwoComplement = environment.getNextVariableString();
    final String shiftedTmpMulVar = environment.getNextVariableString();
    final String resultOneComplement = environment.getNextVariableString();
    final String firstComplementVar2 = environment.getNextVariableString();
    final String secondComplementVar2 = environment.getNextVariableString();
    final String finalFirstComplement = environment.getNextVariableString();
    final String finalSecondComplement = environment.getNextVariableString();
    final String finalComplement = environment.getNextVariableString();
    final String finalMSB = environment.getNextVariableString();
    final String tmpResult = environment.getNextVariableString();
    final String checkOverflowVar = environment.getNextVariableString();
    final String overflowTmp = environment.getNextVariableString();

    final OperandSize qw = OperandSize.QWORD;
    final OperandSize dw = OperandSize.DWORD;
    final OperandSize bt = OperandSize.BYTE;

    if (isSigned) {
      /**
       * if one of the operands is negative the twos complement will be computed and then the
       * multiplication will take place.
       */
      // construct the complement variables
      instructions.add(ReilHelpers.createStr(baseOffset++, dw, String.valueOf(0xFFFFFFFFL), dw,
          firstComplementVar));
      instructions.add(ReilHelpers.createStr(baseOffset++, dw, String.valueOf(0xFFFFFFFFL), dw,
          secondComplementVar));

      instructions.add(ReilHelpers.createBsh(baseOffset++, dw, firstOperand, bt,
          String.valueOf(-31L), bt, firstMSB));
      instructions.add(ReilHelpers.createBsh(baseOffset++, dw, secondOperand, bt,
          String.valueOf(-31L), bt, secondMSB));

      instructions.add(ReilHelpers.createBisz(baseOffset++, bt, firstMSB, bt, firstComplementVar2));
      instructions.add(ReilHelpers
          .createBisz(baseOffset++, bt, secondMSB, bt, secondComplementVar2));

      instructions.add(ReilHelpers.createAdd(baseOffset++, dw, firstComplementVar, bt,
          firstComplementVar2, dw, finalFirstComplement));
      instructions.add(ReilHelpers.createAnd(baseOffset++, dw, finalFirstComplement, dw,
          String.valueOf(0xFFFFFFFFL), dw, finalFirstComplement));
      instructions.add(ReilHelpers.createAdd(baseOffset++, dw, secondComplementVar, bt,
          secondComplementVar2, dw, finalSecondComplement));
      instructions.add(ReilHelpers.createAnd(baseOffset++, dw, finalSecondComplement, dw,
          String.valueOf(0xFFFFFFFFL), dw, finalSecondComplement));

      instructions.add(ReilHelpers.createXor(baseOffset++, bt, firstMSB, bt, secondMSB, bt,
          finalMSB));
      instructions.add(ReilHelpers.createXor(baseOffset++, dw, finalFirstComplement, dw,
          finalSecondComplement, dw, finalComplement));

      instructions.add(ReilHelpers.createXor(baseOffset++, dw, firstOperand, dw, finalComplement,
          dw, firstOneComplement));
      instructions.add(ReilHelpers.createXor(baseOffset++, dw, secondOperand, dw, finalComplement,
          dw, secondOneComplement));

      instructions.add(ReilHelpers.createAdd(baseOffset++, dw, firstOneComplement, bt, finalMSB,
          dw, firstTwoComplement));
      instructions.add(ReilHelpers.createAdd(baseOffset++, dw, secondOneComplement, bt, finalMSB,
          dw, secondTwoComplement));

      // perform multiplication
      instructions.add(ReilHelpers.createMul(baseOffset++, dw, firstTwoComplement, dw,
          secondTwoComplement, qw, tmpMulVar));

      // calculate twos complement of the result
      if (isHigh) {
        instructions.add(ReilHelpers.createBsh(baseOffset++, qw, tmpMulVar, dw,
            String.valueOf(-32L), dw, shiftedTmpMulVar));
      } else {
        instructions.add(ReilHelpers.createAnd(baseOffset++, qw, tmpMulVar, dw,
            String.valueOf(0xFFFFFFFFL), dw, shiftedTmpMulVar));
      }
      instructions.add(ReilHelpers.createXor(baseOffset++, dw, shiftedTmpMulVar, dw,
          finalComplement, dw, resultOneComplement));
      instructions.add(ReilHelpers.createAdd(baseOffset++, dw, resultOneComplement, bt, finalMSB,
          dw, tmpResult));
      instructions.add(ReilHelpers.createAnd(baseOffset++, dw, tmpResult, dw,
          String.valueOf(0xFFFFFFFFL), dw, targetRegister));
    } else {
      // unsigned multiplication
      instructions.add(ReilHelpers.createMul(baseOffset++, dw, firstOperand, dw, secondOperand, qw,
          tmpMulVar));
      if (isHigh) {
        instructions.add(ReilHelpers.createBsh(baseOffset++, qw, tmpMulVar, dw,
            String.valueOf(-32L), dw, shiftedTmpMulVar));
      } else {
        instructions.add(ReilHelpers.createAnd(baseOffset++, qw, tmpMulVar, dw,
            String.valueOf(0xFFFFFFFFL), dw, shiftedTmpMulVar));
      }
      instructions.add(ReilHelpers.createAnd(baseOffset++, dw, shiftedTmpMulVar, dw,
          String.valueOf(0xFFFFFFFFL), dw, targetRegister));
    }
    if (setOv) {
      instructions.add(ReilHelpers.createBsh(baseOffset++, qw, tmpMulVar, bt, String.valueOf(-32L),
          dw, checkOverflowVar));
      instructions.add(ReilHelpers.createBisz(baseOffset++, dw, checkOverflowVar, bt, overflowTmp));
      instructions.add(ReilHelpers.createBisz(baseOffset++, dw, overflowTmp, bt,
          Helpers.XER_OVERFLOW));
      instructions.add(ReilHelpers.createOr(baseOffset++, bt, Helpers.XER_SUMMARY_OVERFLOW, bt,
          Helpers.XER_OVERFLOW, bt, Helpers.XER_SUMMARY_OVERFLOW));
    }

    if (setCr) {
      // EQ CR0
      instructions.add(ReilHelpers.createBisz(baseOffset++, dw, targetRegister, bt,
          Helpers.CR0_EQUAL));

      // LT CR0
      instructions.add(ReilHelpers.createBsh(baseOffset++, dw, targetRegister, OperandSize.WORD,
          "-31", bt, Helpers.CR0_LESS_THEN));

      // GT CR0
      instructions.add(ReilHelpers.createOr(baseOffset++, bt, Helpers.CR0_EQUAL, bt,
          Helpers.CR0_LESS_THEN, bt, crTemp));
      instructions.add(ReilHelpers.createBisz(baseOffset++, bt, crTemp, bt,
          Helpers.CR0_GREATER_THEN));

      // SO CR0
      instructions.add(ReilHelpers.createStr(baseOffset, bt, Helpers.XER_SUMMARY_OVERFLOW, bt,
          Helpers.CRO_SUMMARY_OVERFLOW));
    }

  }
}
