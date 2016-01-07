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
import com.google.security.zynamics.reil.translators.TranslationHelpers;
import com.google.security.zynamics.zylib.disassembly.IInstruction;

import java.util.List;


public class ARMBxjTranslator implements IInstructionTranslator {
  /**
   * BXJ{<cond>} <Rm>
   * 
   * if ConditionPassed(cond) then if (JE bit of Main Configuration register) == 0 then T Flag =
   * Rm[0] PC = Rm AND 0xFFFFFFFE else jpc = SUB-ARCHITECTURE DEFINED value invalidhandler =
   * SUB-ARCHITECTURE DEFINED value if (Jazelle Extension accepts opcode at jpc) then if (CV bit of
   * Jazelle OS Control register) == 0 then PC = invalidhandler else J Flag = 1 Start opcode
   * execution at jpc else if ((CV bit of Jazelle OS Control register) == 0) AND (IMPLEMENTATION
   * DEFINED CONDITION) then PC = invalidhandler else // Subject to SUB-ARCHITECTURE DEFINED
   * restrictions on Rm: T Flag = Rm[0] PC = Rm AND 0xFFFFFFFE
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "BXJ");

    final long baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);

    instructions.add(ReilHelpers.createUnknown(baseOffset));
  }
}
