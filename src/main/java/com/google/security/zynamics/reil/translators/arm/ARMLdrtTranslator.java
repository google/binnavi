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
package com.google.security.zynamics.reil.translators.arm;

import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.translators.ITranslationEnvironment;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.TranslationHelpers;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.disassembly.IOperandTreeNode;
import com.google.security.zynamics.zylib.general.Pair;

import java.util.List;


public class ARMLdrtTranslator extends ARMBaseTranslator {
  @Override
  protected void translateCore(final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions)
      throws InternalTranslationException {
    final IOperandTreeNode registerOperand1 =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0);
    final IOperandTreeNode rootNode = instruction.getOperands().get(1).getRootNode();

    final String registerNodeValue = (registerOperand1.getValue());

    final OperandSize wd = OperandSize.WORD;
    final OperandSize dw = OperandSize.DWORD;
    final OperandSize bt = OperandSize.BYTE;

    long baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);

    final Pair<String, String> resultPair =
        AddressingModeTwoGenerator.generate(baseOffset, environment, instruction, instructions,
            rootNode);

    final String tmpAddress = resultPair.first();

    final String negRotateVal = environment.getNextVariableString();
    final String posRotateVal = environment.getNextVariableString();
    final String rotateVal1 = environment.getNextVariableString();
    final String rotateVal2 = environment.getNextVariableString();
    final String rotResult1 = environment.getNextVariableString();
    final String rotResult2 = environment.getNextVariableString();
    final String tmpData1 = environment.getNextVariableString();
    final String tmpRotResult = environment.getNextVariableString();
    baseOffset = baseOffset + instructions.size();
    instructions.add(ReilHelpers.createLdm(baseOffset++, dw, tmpAddress, dw, tmpData1));

    // get rotate * 8
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, tmpAddress, bt, String.valueOf(0x3L),
        bt, rotateVal1));
    instructions.add(ReilHelpers.createMul(baseOffset++, bt, rotateVal1, bt, String.valueOf(8), wd,
        rotateVal2));

    // subtraction to get the negative shift val
    instructions.add(ReilHelpers.createSub(baseOffset++, wd, String.valueOf(0), wd, rotateVal2, dw,
        negRotateVal));
    instructions.add(ReilHelpers.createSub(baseOffset++, wd, String.valueOf(32), wd, rotateVal2,
        dw, posRotateVal));

    // do the rotation
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpData1, dw, negRotateVal, dw,
        rotResult1));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, tmpData1, dw, posRotateVal, dw,
        rotResult2));
    instructions.add(ReilHelpers.createOr(baseOffset++, dw, rotResult1, dw, rotResult2, dw,
        tmpRotResult));

    // assing it
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, tmpRotResult, dw,
        String.valueOf(0xFFFFFFFFL), dw, registerNodeValue));
  }

  /**
   * LDR{<cond>}T <Rd>, <post_indexed_addressing_mode>
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "LDR");
    translateAll(environment, instruction, "LDR", instructions);
  }
}
