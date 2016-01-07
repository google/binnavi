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
import com.google.security.zynamics.reil.ReilOperandNode;
import com.google.security.zynamics.reil.translators.ITranslationEnvironment;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.TranslationHelpers;
import com.google.security.zynamics.zylib.disassembly.ExpressionType;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.disassembly.IOperandTreeNode;

import java.util.List;


public class StoreGenerator {
  public static void generate(long baseOffset, final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions,
      final String mnemonic, final OperandSize opSize, final boolean isIndexed,
      final boolean withUpdate, final boolean storeMultiple, final boolean isByteReverse)
      throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, mnemonic);

    final IOperandTreeNode sourceRegister =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0);

    // there is a case where we only have 1 operand in the export due to the fact that we do not get
    // an
    // offset this case must be taken care of.
    IOperandTreeNode sourceRegisterOperand1 = null;
    IOperandTreeNode sourceRegisterOperand2 = null;

    if ((instruction.getOperands().size() == 2)
        && !(instruction.getOperands().get(1).getRootNode().getChildren().get(0).getChildren()
            .get(0).getChildren().size() == 2)) {
      sourceRegisterOperand1 = new ReilOperandNode("0", ExpressionType.IMMEDIATE_INTEGER);
      sourceRegisterOperand2 = instruction.getOperands().get(1).getRootNode().getChildren().get(0);
    } else {
      sourceRegisterOperand1 =
          isIndexed ? instruction.getOperands().get(1).getRootNode().getChildren().get(0)
              : instruction.getOperands().get(1).getRootNode().getChildren().get(0).getChildren()
                  .get(0).getChildren().get(0);
      sourceRegisterOperand2 =
          isIndexed ? instruction.getOperands().get(2).getRootNode().getChildren().get(0)
              : instruction.getOperands().get(1).getRootNode().getChildren().get(0).getChildren()
                  .get(0).getChildren().get(1);
    }
    // TODO: check if the condition sourceRegisterOperand == "0" is handled in the applicable cases.

    final OperandSize bt = OperandSize.BYTE;
    final OperandSize wd = OperandSize.WORD;
    final OperandSize dw = OperandSize.DWORD;
    final OperandSize qw = OperandSize.QWORD;

    final String effectiveAddress = environment.getNextVariableString();
    final String tmpEffectiveAddress = environment.getNextVariableString();
    final String tmpData = environment.getNextVariableString();

    instructions.add(ReilHelpers.createAdd(baseOffset++, dw, sourceRegisterOperand1.getValue(), dw,
        sourceRegisterOperand2.getValue(), qw, tmpEffectiveAddress));
    instructions.add(ReilHelpers.createAnd(baseOffset++, qw, tmpEffectiveAddress, dw,
        String.valueOf(0xFFFFFFFFL), dw, effectiveAddress));

    // resize it to match with instruction
    if (opSize == bt) {
      instructions.add(ReilHelpers.createAnd(baseOffset++, dw, sourceRegister.getValue(), dw,
          String.valueOf(0x000000FFL), bt, tmpData));
      instructions.add(ReilHelpers.createStm(baseOffset++, bt, tmpData, dw, effectiveAddress));
    } else if (opSize == wd) {
      if (isByteReverse) {
        final String tmpHighByte = environment.getNextVariableString();
        final String tmpLowByte = environment.getNextVariableString();
        final String tmpHighByteShifted = environment.getNextVariableString();
        final String tmpLowByteShifted = environment.getNextVariableString();

        instructions.add(ReilHelpers.createAnd(baseOffset++, wd, sourceRegister.getValue(), dw,
            String.valueOf(0x000000FFL), dw, tmpLowByte));
        instructions.add(ReilHelpers.createAnd(baseOffset++, wd, sourceRegister.getValue(), dw,
            String.valueOf(0x0000FF00L), dw, tmpHighByte));
        instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpLowByte, bt,
            String.valueOf(8L), dw, tmpLowByteShifted));
        instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpHighByte, bt,
            String.valueOf(-8L), dw, tmpHighByteShifted));
        instructions.add(ReilHelpers.createOr(baseOffset++, dw, tmpHighByteShifted, dw,
            tmpLowByteShifted, dw, tmpData));
      } else {
        instructions.add(ReilHelpers.createAnd(baseOffset++, dw, sourceRegister.getValue(), dw,
            String.valueOf(0x0000FFFFL), wd, tmpData));
      }

      instructions.add(ReilHelpers.createStm(baseOffset++, wd, tmpData, dw, effectiveAddress));

    } else if (opSize == dw) {
      if (!isByteReverse && !storeMultiple) {
        instructions.add(ReilHelpers.createAnd(baseOffset++, dw, sourceRegister.getValue(), dw,
            String.valueOf(0xFFFFFFFFL), dw, tmpData));
        instructions.add(ReilHelpers.createStm(baseOffset++, dw, tmpData, dw, effectiveAddress));
      } else if (storeMultiple && !isByteReverse) {
        int index = Helpers.getRegisterIndex(sourceRegister.getValue());
        while (index <= 31) {
          instructions.add(ReilHelpers.createStm(baseOffset++, dw, "%r" + index, dw,
              effectiveAddress));
          instructions.add(ReilHelpers.createAdd(baseOffset++, dw, effectiveAddress, bt,
              String.valueOf(4L), dw, effectiveAddress));
          index++;
        }
      } else if (!storeMultiple && isByteReverse) {
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

        // extract bytes
        instructions.add(ReilHelpers.createAnd(baseOffset++, wd, sourceRegister.getValue(), dw,
            String.valueOf(0x000000FFL), dw, tmpByte1));
        instructions.add(ReilHelpers.createAnd(baseOffset++, wd, sourceRegister.getValue(), dw,
            String.valueOf(0x0000FF00L), dw, tmpByte2));
        instructions.add(ReilHelpers.createAnd(baseOffset++, wd, sourceRegister.getValue(), dw,
            String.valueOf(0x00FF0000L), dw, tmpByte3));
        instructions.add(ReilHelpers.createAnd(baseOffset++, wd, sourceRegister.getValue(), dw,
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
            tmpTargetValue2, dw, tmpData));

        instructions.add(ReilHelpers.createStm(baseOffset++, dw, tmpData, dw, effectiveAddress));
      }

    }

    if (withUpdate) {
      // Update the register specified in the instruction with the effective address

      instructions.add(ReilHelpers.createStr(baseOffset++, dw, effectiveAddress, dw,
          sourceRegisterOperand1.getValue()));
    }
  }
}
