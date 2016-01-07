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


public class XorGenerator {
  public static void generate(long baseOffset, final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions,
      final String mnemonic, final String firstOperand, final String secondOperand,
      final boolean setCr, final boolean setOverflow, final boolean isNeg, final boolean isEqv)
      throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, mnemonic);

    final IOperandTreeNode registerOperand1 =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0);

    final String targetRegister = registerOperand1.getValue();
    final String crTemp = setCr ? environment.getNextVariableString() : null;
    final String negTemp = isNeg ? environment.getNextVariableString() : null;
    final String negTemp2 = isNeg ? environment.getNextVariableString() : null;
    final String overflowTmp = setOverflow ? environment.getNextVariableString() : null;
    final String eqvTmp = isEqv ? environment.getNextVariableString() : null;

    if (isNeg) {
      // perform rR = ! rA + 1 equivalent rR = ( rA XOR 0xFFFFFFFF ) + 1

      instructions.add(ReilHelpers.createXor(baseOffset++, OperandSize.DWORD, firstOperand,
          OperandSize.DWORD, secondOperand, OperandSize.DWORD, negTemp));
      instructions.add(ReilHelpers.createAdd(baseOffset++, OperandSize.DWORD, negTemp,
          OperandSize.BYTE, "1", OperandSize.QWORD, negTemp2));

      // reduce to register size
      instructions.add(ReilHelpers.createAnd(baseOffset++, OperandSize.QWORD, negTemp2,
          OperandSize.DWORD, "4294967295", OperandSize.DWORD, targetRegister));
      if (setOverflow) {
        // Overflow is set if Result == 0x80000000
        instructions.add(ReilHelpers.createXor(baseOffset++, OperandSize.DWORD, targetRegister,
            OperandSize.DWORD, String.valueOf(0x80000000L), OperandSize.DWORD, overflowTmp));

        // set XER register bits according to the current register state and overflow calculation
        instructions.add(ReilHelpers.createBisz(baseOffset++, OperandSize.DWORD, overflowTmp,
            OperandSize.WORD, Helpers.XER_OVERFLOW));
        instructions.add(ReilHelpers.createOr(baseOffset++, OperandSize.WORD,
            Helpers.XER_SUMMARY_OVERFLOW, OperandSize.DWORD, Helpers.XER_OVERFLOW,
            OperandSize.WORD, Helpers.XER_SUMMARY_OVERFLOW));
      }
    } else if (isEqv) {
      instructions.add(ReilHelpers.createXor(baseOffset++, OperandSize.DWORD, firstOperand,
          OperandSize.DWORD, secondOperand, OperandSize.DWORD, eqvTmp));
      instructions.add(ReilHelpers.createXor(baseOffset++, OperandSize.DWORD, eqvTmp,
          OperandSize.DWORD, String.valueOf(0xFFFFFFFFL), OperandSize.DWORD, targetRegister));
    } else {
      // perform rR = rA XOR rB

      instructions.add(ReilHelpers.createXor(baseOffset++, OperandSize.DWORD, firstOperand,
          OperandSize.DWORD, secondOperand, OperandSize.DWORD, targetRegister));
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
