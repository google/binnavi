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


public class DivGenerator {
  public static void generate(long baseOffset, final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions,
      final String mnemonic, final boolean isSigned, final boolean setCr, final boolean setOv)
      throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, mnemonic);

    final IOperandTreeNode registerOperand1 =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0);
    final IOperandTreeNode registerOperand2 =
        instruction.getOperands().get(1).getRootNode().getChildren().get(0);
    final IOperandTreeNode registerOperand3 =
        instruction.getOperands().get(2).getRootNode().getChildren().get(0);

    final String targetRegister = registerOperand1.getValue();
    final String dividend = registerOperand2.getValue();
    final String divisor = registerOperand3.getValue();

    final OperandSize dw = OperandSize.DWORD;
    final OperandSize wd = OperandSize.WORD;
    final OperandSize bt = OperandSize.BYTE;

    final String crTemp = setCr ? environment.getNextVariableString() : null;

    final String zeroCheck = environment.getNextVariableString();

    final String overflowTmp1 = setOv && isSigned ? environment.getNextVariableString() : null;
    final String overflowTmp2 = setOv && isSigned ? environment.getNextVariableString() : null;
    final String overflowTmp3 = setOv && isSigned ? environment.getNextVariableString() : null;
    final String overflowTmp4 = setOv && isSigned ? environment.getNextVariableString() : null;
    final String overflowTmp5 = setOv && isSigned ? environment.getNextVariableString() : null;
    final String overflowTmp6 = setOv && isSigned ? environment.getNextVariableString() : null;

    final String dividendMSB = isSigned ? environment.getNextVariableString() : null;
    final String divisorMSB = isSigned ? environment.getNextVariableString() : null;
    final String newMSB = isSigned ? environment.getNextVariableString() : null;
    final String tmpResult = isSigned ? environment.getNextVariableString() : null;

    // do zero check on divisor
    instructions.add(ReilHelpers.createBisz(baseOffset++, dw, divisor, dw, zeroCheck));

    if (isSigned) {
      final String firstComplementVar2 = environment.getNextVariableString();
      final String secondComplementVar2 = environment.getNextVariableString();
      final String finalComplement = environment.getNextVariableString();
      final String finalFirstComplement = environment.getNextVariableString();
      final String finalSecondComplement = environment.getNextVariableString();
      final String firstOneComplement = environment.getNextVariableString();
      final String firstTwoComplement = environment.getNextVariableString();
      final String secondOneComplement = environment.getNextVariableString();
      final String secondTwoComplement = environment.getNextVariableString();
      final String shiftedTmpResult = environment.getNextVariableString();
      final String allOnes = environment.getNextVariableString();
      final String resultOneComplement = environment.getNextVariableString();
      final String allOnesCheck = environment.getNextVariableString();

      // do check if divisor == 0xFFFFFFFF
      instructions.add(ReilHelpers.createXor(baseOffset++, dw, divisor, dw,
          String.valueOf(0xFFFFFFFFL), dw, allOnes));
      instructions.add(ReilHelpers.createBisz(baseOffset++, dw, allOnes, dw, allOnesCheck));
      instructions.add(ReilHelpers.createOr(baseOffset++, dw, zeroCheck, dw, allOnesCheck, dw,
          zeroCheck));

      // jump away if divisor is == 0
      instructions.add(ReilHelpers.createJcc(baseOffset++, dw, zeroCheck, dw,
          String.format("%d.%d", instruction.getAddress().toLong(), 23)));

      // construct the complement variables
      instructions.add(ReilHelpers.createBsh(baseOffset++, dw, dividend, bt, String.valueOf(-31L),
          bt, dividendMSB));
      instructions.add(ReilHelpers.createBsh(baseOffset++, dw, divisor, bt, String.valueOf(-31L),
          bt, divisorMSB));

      instructions.add(ReilHelpers.createBisz(baseOffset++, bt, dividendMSB, bt,
          firstComplementVar2));
      instructions.add(ReilHelpers.createBisz(baseOffset++, bt, divisorMSB, bt,
          secondComplementVar2));

      instructions.add(ReilHelpers.createAdd(baseOffset++, dw, String.valueOf(0xFFFFFFFFL), bt,
          firstComplementVar2, dw, finalFirstComplement));
      instructions.add(ReilHelpers.createAnd(baseOffset++, dw, finalFirstComplement, dw,
          String.valueOf(0xFFFFFFFFL), dw, finalFirstComplement));
      instructions.add(ReilHelpers.createAdd(baseOffset++, dw, String.valueOf(0xFFFFFFFFL), bt,
          secondComplementVar2, dw, finalSecondComplement));
      instructions.add(ReilHelpers.createAnd(baseOffset++, dw, finalSecondComplement, dw,
          String.valueOf(0xFFFFFFFFL), dw, finalSecondComplement));

      instructions.add(ReilHelpers.createXor(baseOffset++, bt, dividendMSB, bt, divisorMSB, bt,
          newMSB));
      instructions.add(ReilHelpers.createXor(baseOffset++, dw, finalFirstComplement, dw,
          finalSecondComplement, dw, finalComplement));

      instructions.add(ReilHelpers.createXor(baseOffset++, dw, dividend, dw, finalFirstComplement,
          dw, firstOneComplement));
      instructions.add(ReilHelpers.createXor(baseOffset++, dw, divisor, dw, finalSecondComplement,
          dw, secondOneComplement));

      instructions.add(ReilHelpers.createAdd(baseOffset++, dw, firstOneComplement, bt, dividendMSB,
          dw, firstTwoComplement));
      instructions.add(ReilHelpers.createAdd(baseOffset++, dw, secondOneComplement, bt, divisorMSB,
          dw, secondTwoComplement));

      // perform division
      instructions.add(ReilHelpers.createDiv(baseOffset++, dw, firstTwoComplement, dw,
          secondTwoComplement, dw, tmpResult));

      instructions.add(ReilHelpers.createAnd(baseOffset++, dw, tmpResult, dw,
          String.valueOf(0xFFFFFFFFL), dw, shiftedTmpResult));

      instructions.add(ReilHelpers.createXor(baseOffset++, dw, shiftedTmpResult, dw,
          finalComplement, dw, resultOneComplement));
      instructions.add(ReilHelpers.createAdd(baseOffset++, dw, resultOneComplement, bt, newMSB, dw,
          tmpResult));
      instructions.add(ReilHelpers.createAnd(baseOffset++, dw, tmpResult, dw,
          String.valueOf(0xFFFFFFFFL), dw, targetRegister));
    } else {
      // jump away if divisor == 0
      instructions.add(ReilHelpers.createJcc(baseOffset++, dw, zeroCheck, dw,
          String.format("%d.%d", instruction.getAddress().toLong(), 3)));

      // perform division
      instructions.add(ReilHelpers.createDiv(baseOffset++, dw, dividend, dw, divisor, dw,
          targetRegister));
    }
    if (setOv) {
      if (isSigned) {
        // case where divisor == 0
        instructions.add(ReilHelpers.createBisz(baseOffset++, dw, divisor, dw, overflowTmp1));

        // case where dividend == 0x8000 0000 and divisor == -1
        // divisor
        instructions.add(ReilHelpers.createXor(baseOffset++, dw, divisor, dw,
            String.valueOf(0xFFFFFFFFL), dw, overflowTmp2));
        instructions.add(ReilHelpers.createBisz(baseOffset++, dw, overflowTmp2, dw, overflowTmp3));

        // dividend
        instructions.add(ReilHelpers.createXor(baseOffset++, dw, dividend, dw,
            String.valueOf(0x80000000L), dw, overflowTmp4));
        instructions.add(ReilHelpers.createBisz(baseOffset++, dw, overflowTmp4, dw, overflowTmp5));

        // combination
        instructions.add(ReilHelpers.createAnd(baseOffset++, dw, overflowTmp3, dw, overflowTmp5,
            dw, overflowTmp6));

        // set XER register according to result
        instructions.add(ReilHelpers.createOr(baseOffset++, dw, overflowTmp1, dw, overflowTmp6, bt,
            Helpers.XER_OVERFLOW));
        instructions.add(ReilHelpers.createOr(baseOffset++, bt, Helpers.XER_OVERFLOW, bt,
            Helpers.XER_SUMMARY_OVERFLOW, bt, Helpers.XER_SUMMARY_OVERFLOW));
      } else {
        // case where divisor == 0
        instructions.add(ReilHelpers
            .createBisz(baseOffset++, dw, divisor, bt, Helpers.XER_OVERFLOW));
        instructions.add(ReilHelpers.createOr(baseOffset++, bt, Helpers.XER_OVERFLOW, bt,
            Helpers.XER_SUMMARY_OVERFLOW, bt, Helpers.XER_SUMMARY_OVERFLOW));
      }
    }

    if (setCr) {
      // EQ CR0
      instructions.add(ReilHelpers.createBisz(baseOffset++, dw, targetRegister, bt,
          Helpers.CR0_EQUAL));

      // LT CR0
      instructions.add(ReilHelpers.createBsh(baseOffset++, dw, targetRegister, wd, "-31", bt,
          Helpers.CR0_LESS_THEN));

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
