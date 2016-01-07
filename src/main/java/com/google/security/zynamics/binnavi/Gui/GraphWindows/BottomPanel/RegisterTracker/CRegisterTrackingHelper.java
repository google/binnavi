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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.RegisterTracker;

import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.IAddress;

import java.util.HashMap;
import java.util.List;
import java.util.Map;



public final class CRegisterTrackingHelper {
  /**
   * You are not supposed to instantiate this class.
   */
  private CRegisterTrackingHelper() {
  }

  /**
   * Creates an address -> instruction mapping for all instructions in a graph.
   *
   * @param view The input graph.
   * @return The created mapping.
   */
  public static Map<IAddress, INaviInstruction> getInstructionMap(final INaviView view) {
    final Map<IAddress, INaviInstruction> instructionMap =
        new HashMap<IAddress, INaviInstruction>();

    final List<INaviViewNode> nodes = view.getGraph().getNodes();

    for (final INaviViewNode node : nodes) {
      if (node instanceof INaviCodeNode) {
        final INaviCodeNode cnode = (INaviCodeNode) node;

        for (final INaviInstruction instruction : cnode.getInstructions()) {
          instructionMap.put(instruction.getAddress(), instruction);
        }
      }
    }

    return instructionMap;
  }
}
