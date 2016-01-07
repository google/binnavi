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


public class BranchGenerator {
  public static void generate(long baseOffset, final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions,
      final String mnemonic, final String BIOperand, final String addressOperand,
      final boolean setLinkRegister, final boolean isUnconditional, final boolean ctrDecrement,
      final boolean ctrZero, final boolean crCheck, final boolean crZero)
      throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, mnemonic);

    final OperandSize qw = OperandSize.QWORD;
    final OperandSize dw = OperandSize.DWORD;
    final OperandSize bt = OperandSize.BYTE;

    final String tmpCountRegister = ctrDecrement ? environment.getNextVariableString() : null;
    final String branchCondition = environment.getNextVariableString();
    final String conditionConditionRegister = environment.getNextVariableString();
    final String conditionCountRegister = environment.getNextVariableString();
    final String tmpVar1 = environment.getNextVariableString();
    final String tmpVar2 = environment.getNextVariableString();

    final String[] meta = setLinkRegister ? new String[] {"isCall", "true"} : new String[0];

    if (setLinkRegister) {
      instructions.add(ReilHelpers.createStr(baseOffset++, OperandSize.DWORD,
          String.valueOf(instruction.getAddress().toLong() + 4), OperandSize.DWORD,
          Helpers.LINK_REGISTER));
    }

    if (isUnconditional) {
      // perform actual branch

      instructions.add(ReilHelpers.createJcc(baseOffset++, bt, String.valueOf(1L), dw,
          addressOperand, meta));
    } else {
      /**
       * conditional branches:
       * 
       * first combine the condition then branch accordingly
       */
      if (ctrDecrement) {
        // decrement the count register and reduce it to avoid overflows and subsequent calculation
        // errors

        instructions.add(ReilHelpers.createSub(baseOffset++, dw, Helpers.COUNT_REGISTER, bt,
            String.valueOf(1L), qw, tmpCountRegister));
        instructions.add(ReilHelpers.createAnd(baseOffset++, qw, tmpCountRegister, dw,
            String.valueOf(0xFFFFFFFFL), dw, Helpers.COUNT_REGISTER));
      }
      if (ctrDecrement & !crCheck) {
        if (ctrZero) {
          // branch if count register is zero

          instructions.add(ReilHelpers.createBisz(baseOffset++, dw, Helpers.COUNT_REGISTER, bt,
              branchCondition));
          instructions.add(ReilHelpers.createJcc(baseOffset++, bt, branchCondition, dw,
              addressOperand, meta));
        } else {
          // branch if count register is not zero

          instructions.add(ReilHelpers.createJcc(baseOffset++, dw, Helpers.COUNT_REGISTER, dw,
              addressOperand, meta));
        }
      } else if (ctrDecrement & crCheck) {
        if (ctrZero) {
          // set condition true if count register is zero

          instructions.add(ReilHelpers.createBisz(baseOffset++, dw, Helpers.COUNT_REGISTER, bt,
              conditionCountRegister));
        } else {
          // set condition true if count register is not zero

          instructions.add(ReilHelpers.createBisz(baseOffset++, dw, Helpers.COUNT_REGISTER, bt,
              tmpVar1));
          instructions.add(ReilHelpers.createXor(baseOffset++, bt, tmpVar1, bt, String.valueOf(1L),
              bt, conditionCountRegister));
        }
        if (crZero) {
          // set condition true if condition register bit specified is zero

          instructions.add(ReilHelpers.createBisz(baseOffset++, bt,
              Helpers.getCRBit(Integer.valueOf(BIOperand)), bt, conditionConditionRegister));
        } else {
          // set condition true if condition register bit specified is not zero

          instructions.add(ReilHelpers.createBisz(baseOffset++, bt,
              Helpers.getCRBit(Integer.valueOf(BIOperand)), bt, tmpVar2));
          instructions.add(ReilHelpers.createXor(baseOffset++, bt, tmpVar2, bt, String.valueOf(1L),
              bt, conditionConditionRegister));
        }
        // calculate condition

        instructions.add(ReilHelpers.createAnd(baseOffset++, bt, conditionConditionRegister, bt,
            conditionCountRegister, bt, branchCondition));

        // perform actual branch

        instructions.add(ReilHelpers.createJcc(baseOffset++, bt, branchCondition, dw,
            addressOperand, meta));
      } else if (!ctrDecrement & crCheck) {
        if (crZero) {
          // branch if condition register bit specified is zero

          instructions.add(ReilHelpers.createBisz(baseOffset++, bt,
              Helpers.getCRBit(Integer.valueOf(BIOperand)), bt, branchCondition));
          instructions.add(ReilHelpers.createJcc(baseOffset++, bt, branchCondition, dw,
              addressOperand, meta));
        } else {
          // branch if condition register bit specified is not zero

          instructions.add(ReilHelpers.createJcc(baseOffset++, bt,
              Helpers.getCRBit(Integer.valueOf(BIOperand)), dw, addressOperand, meta));
        }
      } else if (!ctrDecrement & !crCheck) {
        // branch conditionally case for "less or equal" and "greater then or equal" cases
        instructions.add(ReilHelpers.createJcc(baseOffset++, bt, BIOperand, dw, addressOperand,
            meta));
      }
    }
  }
}
