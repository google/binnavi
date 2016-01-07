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
import java.util.List;


import com.google.common.collect.ListMultimap;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.reil.ReilBlock;
import com.google.security.zynamics.reil.ReilFunction;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.zylib.disassembly.IAddress;


/**
 * Instruction type class for function calls.
 */
public final class CCallsDescription extends CAbstractTypeDescription {
  /**
   * Creates a new function calls description object.
   */
  protected CCallsDescription() {
    super(new Color(42, 143, 255), "Highlights function calls");
  }

  @Override
  public String getDescription() {
    return "Function Calls";
  }

  @Override
  public Collection<CSpecialInstruction> visit(final ReilFunction reilCode,
      final ListMultimap<IAddress, INaviInstruction> instructionMap) {
    final Collection<CSpecialInstruction> instructions = new ArrayList<CSpecialInstruction>();

    for (final ReilBlock block : reilCode.getGraph()) {
      for (final ReilInstruction reilInstruction : block) {
        if (ReilHelpers.isFunctionCall(reilInstruction)) {
          final List<INaviInstruction> nativeInstructions =
              instructionMap.get(ReilHelpers.toNativeAddress(reilInstruction.getAddress()));
          if (nativeInstructions != null) {
            for (final INaviInstruction naviInstruction : nativeInstructions) {
              instructions.add(new CSpecialInstruction(this, naviInstruction));
            }
          }
        }
      }
    }

    return instructions;
  }
}
