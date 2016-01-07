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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.InstructionHighlighter;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import com.google.common.collect.ListMultimap;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.reil.ReilBlock;
import com.google.security.zynamics.reil.ReilFunction;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.zylib.disassembly.IAddress;


/**
 * Special instruction description for instructions that read memory.
 */
public final class CReadsDescription extends CAbstractTypeDescription {
  /**
   * Creates a new description object.
   */
  protected CReadsDescription() {
    super(new Color(126, 255, 36), "Highlights instructions that read memory");
  }

  /**
   * Determines whether any of the given instructions is a function call.
   * 
   * @param instructions The instructions to search through.
   * @param calls List of known function calls.
   * 
   * @return True, if any of the given instructions are function calls.
   */
  private boolean isAnyCall(final List<INaviInstruction> instructions,
      final Set<INaviInstruction> calls) {
    for (final INaviInstruction naviInstruction : instructions) {
      if (calls.contains(naviInstruction)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public String getDescription() {
    return "Memory Reads";
  }

  @Override
  public Collection<CSpecialInstruction> visit(final ReilFunction reilCode,
      final ListMultimap<IAddress, INaviInstruction> instructionMap) {
    final Collection<CSpecialInstruction> instructions = new ArrayList<CSpecialInstruction>();

    final Set<INaviInstruction> calls = new HashSet<INaviInstruction>();

    for (final ReilBlock block : reilCode.getGraph()) {
      for (final ReilInstruction reilInstruction : block) {
        if (ReilHelpers.isFunctionCall(reilInstruction)) {
          calls
              .addAll(instructionMap.get(ReilHelpers.toNativeAddress(reilInstruction.getAddress())));
        }
      }
    }

    for (final ReilBlock block : reilCode.getGraph()) {
      for (final ReilInstruction reilInstruction : block) {
        if (reilInstruction.getMnemonic().equals(ReilHelpers.OPCODE_LDM)) {
          final List<INaviInstruction> firstInstructions =
              instructionMap.get(ReilHelpers.toNativeAddress(reilInstruction.getAddress()));

          if (isAnyCall(firstInstructions, calls)) {
            continue;
          }

          final List<INaviInstruction> nativeInstructions =
              instructionMap.get(ReilHelpers.toNativeAddress(reilInstruction.getAddress()));

          for (final INaviInstruction naviInstruction : nativeInstructions) {
            instructions.add(new CSpecialInstruction(this, naviInstruction));
          }
        }
      }
    }

    return instructions;
  }
}
