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

import java.util.List;


public class CmpGenerator {
  public static void generate(long baseOffset, final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions,
      final String mnemonic, final String firstOperand, final String secondOperand,
      final String thirdOperand, final boolean isLogical) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, mnemonic);

    // check is we have two or three parameters to work with
    final String conditionRegister = firstOperand.toUpperCase();
    final String realFirstOperand = secondOperand;
    final String realSecondOperand = thirdOperand;

    final String crTemp = environment.getNextVariableString();
    final String firstToUnsigned = (!isLogical) ? environment.getNextVariableString() : null;
    final String secondToUnsigned = (!isLogical) ? environment.getNextVariableString() : null;
    final String firstUnsigned =
        (!isLogical) ? environment.getNextVariableString() : realFirstOperand;
    final String secondUnsigned =
        (!isLogical) ? environment.getNextVariableString() : realSecondOperand;
    final String logicalTmpQword = environment.getNextVariableString();
    final String logicalTmp = environment.getNextVariableString();

    if (!isLogical) {
      // case where two signed integers are compared
      // add 0x80000000 to operands to be able to do unsigned compare
      instructions.add(ReilHelpers.createAdd(baseOffset++, OperandSize.DWORD, realFirstOperand,
          OperandSize.DWORD, "2147483648", OperandSize.QWORD, firstToUnsigned));
      instructions.add(ReilHelpers.createAdd(baseOffset++, OperandSize.DWORD, realSecondOperand,
          OperandSize.DWORD, "2147483648", OperandSize.QWORD, secondToUnsigned));

      // reduce both operands to register size to evade the use of the bits 32 - 63
      instructions.add(ReilHelpers.createAnd(baseOffset++, OperandSize.QWORD, firstToUnsigned,
          OperandSize.DWORD, "4294967295", OperandSize.DWORD, firstUnsigned));
      instructions.add(ReilHelpers.createAnd(baseOffset++, OperandSize.QWORD, secondToUnsigned,
          OperandSize.DWORD, "4294967295", OperandSize.DWORD, secondUnsigned));
    }

    // do subtraction if a == b => c = 0 = EQ, a < b => c is negative = LT, a > b => c is positive =
    // GT
    instructions.add(ReilHelpers.createSub(baseOffset++, OperandSize.DWORD, firstUnsigned,
        OperandSize.DWORD, secondUnsigned, OperandSize.QWORD, logicalTmpQword));

    // reduce to the right size
    instructions.add(ReilHelpers.createAnd(baseOffset++, OperandSize.QWORD, logicalTmpQword,
        OperandSize.DWORD, "8589934591", OperandSize.QWORD, logicalTmp));

    // EQ CRi
    instructions.add(ReilHelpers.createBisz(baseOffset++, OperandSize.QWORD, logicalTmp,
        OperandSize.BYTE, conditionRegister + "EQ"));

    // LT CRi
    instructions.add(ReilHelpers.createBsh(baseOffset++, OperandSize.QWORD, logicalTmp,
        OperandSize.WORD, "-32", OperandSize.BYTE, conditionRegister + "LT"));

    // GT CRi
    instructions.add(ReilHelpers.createOr(baseOffset++, OperandSize.BYTE, conditionRegister + "EQ",
        OperandSize.BYTE, conditionRegister + "LT", OperandSize.BYTE, crTemp));
    instructions.add(ReilHelpers.createBisz(baseOffset++, OperandSize.BYTE, crTemp,
        OperandSize.BYTE, conditionRegister + "GT"));

    // SO CRi
    instructions.add(ReilHelpers.createStr(baseOffset, OperandSize.BYTE,
        Helpers.XER_SUMMARY_OVERFLOW, OperandSize.BYTE, conditionRegister + "SO"));
  }
}
