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

import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.translators.IInstructionTranslator;
import com.google.security.zynamics.reil.translators.ITranslationEnvironment;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.StandardEnvironment;
import com.google.security.zynamics.zylib.disassembly.IInstruction;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public abstract class ARMBaseTranslator implements IInstructionTranslator {
  private void translateCondition(final ITranslationEnvironment environment,
      final IInstruction instruction, final String prefix,
      final List<ReilInstruction> instructions, final int nopIndex)
      throws InternalTranslationException {
    final long baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);

    // check if instruction is conditional
    final Pattern condPattern =
        Pattern.compile(prefix + "(AL|CC|LO|CS|HS|EQ|GE|GT|HI|LE|LS|LT|MI|NE|NV|PL|VC|VS).{0,2}$");
    final Matcher condMatcher = condPattern.matcher(instruction.getMnemonic());
    if (condMatcher.matches()) {
      // conditional execution is found call condition generator
      final String jmpGoal = String.format("%d.%d", instruction.getAddress().toLong(), nopIndex);
      ConditionGenerator.generate(baseOffset, environment, instruction, instructions,
          condMatcher.group(1), jmpGoal);
    }
  }

  protected int countConditionInstructions(final IInstruction instruction, final String prefix)
      throws InternalTranslationException {
    final List<ReilInstruction> instructions = new ArrayList<ReilInstruction>();

    translateCondition(new StandardEnvironment(), instruction, prefix, instructions, 0);

    return instructions.size();
  }

  protected int countInnerInstructions(final IInstruction instruction)
      throws InternalTranslationException {
    final List<ReilInstruction> instructions = new ArrayList<ReilInstruction>();

    translateCore(new StandardEnvironment(), instruction, instructions);

    return instructions.size();
  }

  protected void translateAll(final ITranslationEnvironment environment,
      final IInstruction instruction, final String prefix, final List<ReilInstruction> instructions)
      throws InternalTranslationException {
    final long baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);

    final int conditionInstructions = countConditionInstructions(instruction, prefix);
    final int totalInstructions = conditionInstructions + countInnerInstructions(instruction);

    translateCondition(environment, instruction, prefix, instructions, totalInstructions);
    translateCore(environment, instruction, instructions);

    if (conditionInstructions != 0) {
      instructions.add(ReilHelpers.createNop(baseOffset + totalInstructions));
    }
  }

  protected abstract void translateCore(final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions)
      throws InternalTranslationException;
}
