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
package com.google.security.zynamics.reil.translators.reil;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.ReilOperand;
import com.google.security.zynamics.reil.translators.ITranslationEnvironment;
import com.google.security.zynamics.reil.translators.ITranslationExtension;
import com.google.security.zynamics.reil.translators.ITranslator;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.disassembly.IOperandTree;


/**
 * Translator class that translates x86 code to REIL code
 * 
 * sp
 * 
 */
public class TranslatorREIL<InstructionType extends IInstruction> implements
    ITranslator<InstructionType> {
  private ReilOperand convert(final IOperandTree operandTree) {
    if (operandTree.getRootNode().getChildren().get(0).getValue().equals(" ")) {
      return ReilHelpers.createOperand(OperandSize.EMPTY, "");
    } else {
      return ReilHelpers.createOperand(
          OperandSize.sizeStringToValue(operandTree.getRootNode().getValue()), operandTree
              .getRootNode().getChildren().get(0).getValue());
    }
  }

  /**
   * Translates a REIL instruction to REIL code
   * 
   * @param environment A valid translation environment
   * @param instruction The REIL instruction to translate
   * 
   * @return The list of REIL instructions the REIL instruction was translated to
   * 
   * @throws InternalTranslationException An internal translation error occured
   * @throws IllegalArgumentException Any of the arguments passed to the function are invalid
   * 
   */
  @Override
  public List<ReilInstruction> translate(final ITranslationEnvironment environment,
      final InstructionType instruction,
      final List<ITranslationExtension<InstructionType>> extensions)
      throws InternalTranslationException {
    Preconditions.checkNotNull(environment, "Error: Argument environment can't be null");
    Preconditions.checkNotNull(instruction, "Error: Argument instruction can't be null");

    final IAddress offset = ReilHelpers.toReilAddress(instruction.getAddress());
    final String mnemonic = instruction.getMnemonic();

    final ReilOperand firstOperand = convert(instruction.getOperands().get(0));
    final ReilOperand secondOperand = convert(instruction.getOperands().get(1));
    final ReilOperand thirdOperand = convert(instruction.getOperands().get(2));

    return Lists.newArrayList(new ReilInstruction(offset, mnemonic, firstOperand, secondOperand,
        thirdOperand));
  }
}
