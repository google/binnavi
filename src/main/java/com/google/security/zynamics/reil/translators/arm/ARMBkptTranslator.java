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


public class ARMBkptTranslator implements IInstructionTranslator {
  /**
   * if (not overridden by debug hardware) R14_abt = address of BKPT instruction + 4 SPSR_abt = CPSR
   * CPSR[4:0] = 0b10111 // Enter Abort mode CPSR[5] = 0 // Execute in ARM state // CPSR[6] is
   * unchanged CPSR[7] = 1 // Disable normal interrupts CPSR[8] = 1 // Disable imprecise aborts - v6
   * only CPSR[9] = CP15_reg1_EEbit if high vectors configured then PC = 0xFFFF000C else PC =
   * 0x0000000C
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "BKPT");

    long baseOffset = ReilHelpers.nextReilAddress(instruction, instructions);

    instructions.add(ReilHelpers.createUnknown(baseOffset));
  }
}
