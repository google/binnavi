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


public class LoadGenerator {
  public static void generate(long baseOffset, final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions,
      final String mnemonic, final OperandSize opSize, final boolean isIndexed,
      final boolean withUpdate, final boolean isAlgebraic, final boolean loadMultiple,
      final boolean isByteReverse) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, mnemonic);

    final IOperandTreeNode targetRegister =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0);
    final IOperandTreeNode sourceRegisterOperand1 =
        isIndexed ? instruction.getOperands().get(1).getRootNode().getChildren().get(0)
            : instruction.getOperands().get(1).getRootNode().getChildren().get(0).getChildren()
                .get(0).getChildren().get(0);
    final IOperandTreeNode sourceRegisterOperand2 =
        isIndexed ? instruction.getOperands().get(2).getRootNode().getChildren().get(0)
            : instruction.getOperands().get(1).getRootNode().getChildren().get(0).getChildren()
                .get(0).getChildren().get(1);

    // TODO: check if the condition sourceRegisterOperand == "0" is handled in the applicable cases.

    final OperandSize bt = OperandSize.BYTE;
    final OperandSize wd = OperandSize.WORD;
    final OperandSize dw = OperandSize.DWORD;
    final OperandSize qw = OperandSize.QWORD;

    final String effectiveAddress = environment.getNextVariableString();
    final String tmpEffectiveAddress = environment.getNextVariableString();
    final String tmpDataWord = environment.getNextVariableString();
    final String tmpDataByte = environment.getNextVariableString();
    final String tmpDataDWord = environment.getNextVariableString();

    // always compute effective address
    instructions.add(ReilHelpers.createAdd(baseOffset++, dw, sourceRegisterOperand1.getValue(), dw,
        sourceRegisterOperand2.getValue(), qw, tmpEffectiveAddress));
    instructions.add(ReilHelpers.createAnd(baseOffset++, qw, tmpEffectiveAddress, dw,
        String.valueOf(0xFFFFFFFFL), dw, effectiveAddress));

    // resize it to match with instruction
    if (opSize == bt) {
      instructions.add(ReilHelpers.createLdm(baseOffset++, dw, effectiveAddress, bt, tmpDataByte));
      instructions.add(ReilHelpers.createAnd(baseOffset++, bt, tmpDataByte, dw,
          String.valueOf(0x000000FFL), dw, targetRegister.getValue()));
    } else if (opSize == wd) {
      instructions.add(ReilHelpers.createLdm(baseOffset++, dw, effectiveAddress, wd, tmpDataWord));

      if (!isAlgebraic && !isByteReverse) {
        instructions.add(ReilHelpers.createAnd(baseOffset++, wd, tmpDataWord, dw,
            String.valueOf(0x0000FFFFL), dw, targetRegister.getValue()));
      } else if (isAlgebraic && !isByteReverse) {
        // sign extend if Bit 15 is set

        final String tmpAlgebraic1 = environment.getNextVariableString();
        final String tmpAlgebraic2 = environment.getNextVariableString();
        final String tmpAlgebraic3 = environment.getNextVariableString();
        final String tmpAlgebraic4 = environment.getNextVariableString();
        final String tmpAlgebraic5 = environment.getNextVariableString();
        final String tmpAlgebraic6 = environment.getNextVariableString();

        // TODO: Look up sign extension in the book (Not just in this class)
        instructions.add(ReilHelpers.createAnd(baseOffset++, wd, tmpDataWord, dw,
            String.valueOf(0x0000FFFFL), dw, tmpAlgebraic6));
        instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpAlgebraic6, wd,
            String.valueOf(-15L), dw, tmpAlgebraic1));
        instructions.add(ReilHelpers.createAnd(baseOffset++, dw, tmpAlgebraic1, dw,
            String.valueOf(1L), dw, tmpAlgebraic2));
        instructions
            .add(ReilHelpers.createBisz(baseOffset++, dw, tmpAlgebraic2, dw, tmpAlgebraic3));
        instructions.add(ReilHelpers.createAdd(baseOffset++, dw, tmpAlgebraic3, dw,
            String.valueOf(0xFFFFFFFFL), dw, tmpAlgebraic4));
        instructions.add(ReilHelpers.createAnd(baseOffset++, dw, tmpAlgebraic4, dw,
            String.valueOf(0xFFFF0000L), dw, tmpAlgebraic5));
        instructions.add(ReilHelpers.createOr(baseOffset++, dw, tmpAlgebraic5, dw, tmpAlgebraic6,
            dw, targetRegister.getValue()));
      } else if (!isAlgebraic && isByteReverse) {
        final String tmpHighByte = environment.getNextVariableString();
        final String tmpHighByteShifted = environment.getNextVariableString();
        final String tmpLowByte = environment.getNextVariableString();
        final String tmpLowByteShifted = environment.getNextVariableString();

        instructions.add(ReilHelpers.createAnd(baseOffset++, wd, tmpDataWord, dw,
            String.valueOf(0x000000FFL), dw, tmpLowByte));
        instructions.add(ReilHelpers.createAnd(baseOffset++, wd, tmpDataWord, dw,
            String.valueOf(0x0000FF00L), dw, tmpHighByte));
        instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpLowByte, bt,
            String.valueOf(8L), dw, tmpLowByteShifted));
        instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpHighByte, bt,
            String.valueOf(-8L), dw, tmpHighByteShifted));
        instructions.add(ReilHelpers.createOr(baseOffset++, dw, tmpHighByteShifted, dw,
            tmpLowByteShifted, dw, targetRegister.getValue()));
      }

    } else if (opSize == dw) {
      if (isByteReverse) {
        final String tmpByte1 = environment.getNextVariableString();
        final String tmpByte2 = environment.getNextVariableString();
        final String tmpByte3 = environment.getNextVariableString();
        final String tmpByte4 = environment.getNextVariableString();
        final String tmpByte1Shifted = environment.getNextVariableString();
        final String tmpByte2Shifted = environment.getNextVariableString();
        final String tmpByte3Shifted = environment.getNextVariableString();
        final String tmpByte4Shifted = environment.getNextVariableString();
        final String tmpTargetValue1 = environment.getNextVariableString();
        final String tmpTargetValue2 = environment.getNextVariableString();

        instructions.add(ReilHelpers
            .createLdm(baseOffset++, dw, effectiveAddress, dw, tmpDataDWord));

        // extract bytes
        instructions.add(ReilHelpers.createAnd(baseOffset++, dw, tmpDataDWord, dw,
            String.valueOf(0x000000FFL), dw, tmpByte1));
        instructions.add(ReilHelpers.createAnd(baseOffset++, dw, tmpDataDWord, dw,
            String.valueOf(0x0000FF00L), dw, tmpByte2));
        instructions.add(ReilHelpers.createAnd(baseOffset++, dw, tmpDataDWord, dw,
            String.valueOf(0x00FF0000L), dw, tmpByte3));
        instructions.add(ReilHelpers.createAnd(baseOffset++, dw, tmpDataDWord, dw,
            String.valueOf(0xFF000000L), dw, tmpByte4));

        // shift bytes
        instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpByte1, bt, String.valueOf(24L),
            dw, tmpByte1Shifted));
        instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpByte2, bt, String.valueOf(8L),
            dw, tmpByte2Shifted));
        instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpByte3, bt, String.valueOf(-8L),
            dw, tmpByte3Shifted));
        instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpByte4, bt,
            String.valueOf(-24L), dw, tmpByte4Shifted));

        // compose register store statement
        instructions.add(ReilHelpers.createOr(baseOffset++, dw, tmpByte1Shifted, dw,
            tmpByte2Shifted, dw, tmpTargetValue1));
        instructions.add(ReilHelpers.createOr(baseOffset++, dw, tmpByte3Shifted, dw,
            tmpByte4Shifted, dw, tmpTargetValue2));
        instructions.add(ReilHelpers.createOr(baseOffset++, dw, tmpTargetValue1, dw,
            tmpTargetValue2, dw, targetRegister.getValue()));
      } else if (loadMultiple) {
        int index = Helpers.getRegisterIndex(targetRegister.getValue());
        while (index <= 31) {
          instructions.add(ReilHelpers.createLdm(baseOffset++, dw, effectiveAddress, dw, "%r"
              + index));
          instructions.add(ReilHelpers.createAdd(baseOffset++, dw, effectiveAddress, bt,
              String.valueOf(4L), dw, effectiveAddress));
          index++;
        }
      } else {
        instructions.add(ReilHelpers.createLdm(baseOffset++, dw, effectiveAddress, dw,
            targetRegister.getValue()));
      }
    }

    if (withUpdate) {
      // Update the register specified in the instruction with the effective address

      instructions.add(ReilHelpers.createStr(baseOffset++, dw, effectiveAddress, dw,
          sourceRegisterOperand1.getValue()));
    }
  }
}
