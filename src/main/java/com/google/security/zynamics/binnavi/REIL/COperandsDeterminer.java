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
package com.google.security.zynamics.binnavi.REIL;

import java.util.HashSet;
import java.util.Set;

import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.reil.OperandType;
import com.google.security.zynamics.reil.ReilBlock;
import com.google.security.zynamics.reil.ReilEdge;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.ReilOperand;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.ReilTranslator;
import com.google.security.zynamics.reil.translators.StandardEnvironment;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.types.graphs.DirectedGraph;



/**
 * Used to find the registers read and written by a native instruction.
 */
public final class COperandsDeterminer {
  /**
   * You are not supposed to instantiate this class.
   */
  private COperandsDeterminer() {
  }

  /**
   * Determines whether a given operand is a register.
   * 
   * @param operand The operand to check.
   * @param translatingReil True, if we are doing a REIL -> REIL translation.
   * 
   * @return True, if the operand is a register. False, otherwise.
   */
  private static boolean isRegister(final ReilOperand operand, final boolean translatingReil) {
    return ((operand.getType() == OperandType.REGISTER) && (translatingReil || ReilHelpers
        .isNativeRegister(operand)));
  }

  /**
   * Determines whether a given instructions writes to its third operand.
   * 
   * @param reilInstruction The REIL instruction to check.
   * @param translatingReil True, if we are doing a REIL -> REIL translation.
   * 
   * @return True, if the instruction writes to its third operand.
   */
  private static boolean writesThirdOperand(final ReilInstruction reilInstruction,
      final boolean translatingReil) {
    return ReilHelpers.writesThirdOperand(reilInstruction.getMnemonicCode())
        && (translatingReil || ReilHelpers.isNativeRegister(reilInstruction.getThirdOperand()));
  }

  /**
   * Returns the registers read and written by a native instruction.
   * 
   * @param instruction The instruction whose accessed registers are returned.
   * 
   * @return The read and written registers of the instruction.
   * 
   * @throws InternalTranslationException Thrown if the instruction could not be translated to REIL.
   */
  public static Pair<Set<String>, Set<String>> getRegisters(final INaviInstruction instruction)
      throws InternalTranslationException {
    final Set<String> inSet = new HashSet<String>();
    final Set<String> outSet = new HashSet<String>();

    final ReilTranslator<INaviInstruction> translator = new ReilTranslator<INaviInstruction>();

    final DirectedGraph<ReilBlock, ReilEdge> reilCode =
        translator.translate(new StandardEnvironment(), instruction);

    final boolean translatingReil = instruction.getArchitecture().equals("REIL");

    for (final ReilBlock reilBlock : reilCode) {
      for (final ReilInstruction reilInstruction : reilBlock) {
        if (writesThirdOperand(reilInstruction, translatingReil)) {
          outSet.add(reilInstruction.getThirdOperand().getValue());
        }

        if (!writesThirdOperand(reilInstruction, translatingReil)
            && isRegister(reilInstruction.getThirdOperand(), translatingReil)) {
          // JCC + STM
          inSet.add(reilInstruction.getThirdOperand().getValue());
        }

        if (isRegister(reilInstruction.getFirstOperand(), translatingReil)) {
          inSet.add(reilInstruction.getFirstOperand().getValue());
        }

        if (isRegister(reilInstruction.getSecondOperand(), translatingReil)) {
          inSet.add(reilInstruction.getSecondOperand().getValue());
        }
      }
    }

    return new Pair<Set<String>, Set<String>>(inSet, outSet);
  }
}
