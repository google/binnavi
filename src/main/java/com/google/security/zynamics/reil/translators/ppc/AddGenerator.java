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


public class AddGenerator {
  public static void generate(long baseOffset, final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions,
      final String mnemonic, final String firstOperand, final String secondOperand,
      final boolean setCr, final boolean setOverflow, final boolean setCarry,
      final boolean isExtended) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, mnemonic);

    final IOperandTreeNode registerOperand1 =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0);

    final String targetRegister = registerOperand1.getValue();

    final String extendedAddResult = environment.getNextVariableString();
    final String afterCarryAddition = isExtended ? environment.getNextVariableString() : null;
    final String tmpVar3 = setOverflow ? environment.getNextVariableString() : null;
    final String tmpVar4 = setOverflow ? environment.getNextVariableString() : null;
    final String overflowTmp = setOverflow ? environment.getNextVariableString() : null;
    final String msbVara = setOverflow ? environment.getNextVariableString() : null;
    final String msbVarb = setOverflow ? environment.getNextVariableString() : null;
    final String msbVarr = setOverflow ? environment.getNextVariableString() : null;
    final String crTemp = setCr ? environment.getNextVariableString() : null;

    // perform actual addition
    instructions.add(ReilHelpers.createAdd(baseOffset++, OperandSize.DWORD, firstOperand,
        OperandSize.DWORD, secondOperand, OperandSize.QWORD, extendedAddResult));

    // perform addition of carry
    if (isExtended) {
      instructions.add(ReilHelpers.createAdd(baseOffset++, OperandSize.QWORD, extendedAddResult,
          OperandSize.BYTE, Helpers.XER_CARRY_BIT, OperandSize.QWORD, afterCarryAddition));
      instructions.add(ReilHelpers.createAnd(baseOffset++, OperandSize.QWORD, afterCarryAddition,
          OperandSize.DWORD, "4294967295", OperandSize.DWORD, targetRegister));
    } else {
      instructions.add(ReilHelpers.createAnd(baseOffset++, OperandSize.QWORD, extendedAddResult,
          OperandSize.DWORD, "4294967295", OperandSize.DWORD, targetRegister));
    }

    // reduce to register size

    if (setOverflow) {
      // Isolate summands msb's
      instructions.add(ReilHelpers.createBsh(baseOffset++, OperandSize.DWORD, firstOperand,
          OperandSize.WORD, "-31", OperandSize.DWORD, msbVara));
      instructions.add(ReilHelpers.createBsh(baseOffset++, OperandSize.DWORD, secondOperand,
          OperandSize.WORD, "-31", OperandSize.DWORD, msbVarb));

      // Isolate MSB(Result)
      instructions.add(ReilHelpers.createBsh(baseOffset++, OperandSize.DWORD, targetRegister,
          OperandSize.WORD, "-31", OperandSize.DWORD, msbVarr));

      // perform overflow calculation ( msbA XOR msbR ) AND ( msbB XOR msbR ) == OF
      instructions.add(ReilHelpers.createXor(baseOffset++, OperandSize.DWORD, msbVara,
          OperandSize.DWORD, msbVarr, OperandSize.DWORD, tmpVar4));
      instructions.add(ReilHelpers.createXor(baseOffset++, OperandSize.DWORD, msbVarb,
          OperandSize.DWORD, msbVarr, OperandSize.DWORD, tmpVar3));
      instructions.add(ReilHelpers.createAnd(baseOffset++, OperandSize.DWORD, tmpVar4,
          OperandSize.DWORD, tmpVar3, OperandSize.DWORD, overflowTmp));

      // set XER register bits according to the current register state and overflow calculation
      instructions.add(ReilHelpers.createStr(baseOffset++, OperandSize.DWORD, overflowTmp,
          OperandSize.WORD, Helpers.XER_OVERFLOW));
      instructions.add(ReilHelpers.createOr(baseOffset++, OperandSize.WORD,
          Helpers.XER_SUMMARY_OVERFLOW, OperandSize.DWORD, overflowTmp, OperandSize.WORD,
          Helpers.XER_SUMMARY_OVERFLOW));
    }

    // isolate the carry bit
    if (setCarry) {
      if (isExtended) {
        instructions.add(ReilHelpers.createBsh(baseOffset++, OperandSize.QWORD, afterCarryAddition,
            OperandSize.DWORD, "-32", OperandSize.WORD, Helpers.XER_CARRY_BIT));
      } else {
        instructions.add(ReilHelpers.createBsh(baseOffset++, OperandSize.QWORD, extendedAddResult,
            OperandSize.DWORD, "-32", OperandSize.WORD, Helpers.XER_CARRY_BIT));
      }
    }
    if (setCr) {
      // EQ CR0
      instructions.add(ReilHelpers.createBisz(baseOffset++, OperandSize.DWORD, targetRegister,
          OperandSize.BYTE, Helpers.CR0_EQUAL));

      // LT CR0
      instructions.add(ReilHelpers.createBsh(baseOffset++, OperandSize.DWORD, targetRegister,
          OperandSize.WORD, "-31", OperandSize.BYTE, Helpers.CR0_LESS_THEN));

      // GT CR0
      instructions.add(ReilHelpers.createOr(baseOffset++, OperandSize.BYTE, Helpers.CR0_EQUAL,
          OperandSize.BYTE, Helpers.CR0_LESS_THEN, OperandSize.BYTE, crTemp));
      instructions.add(ReilHelpers.createBisz(baseOffset++, OperandSize.BYTE, crTemp,
          OperandSize.BYTE, Helpers.CR0_GREATER_THEN));

      // SO CR0
      instructions.add(ReilHelpers.createStr(baseOffset, OperandSize.BYTE,
          Helpers.XER_SUMMARY_OVERFLOW, OperandSize.BYTE, Helpers.CRO_SUMMARY_OVERFLOW));
    }

  }
}
