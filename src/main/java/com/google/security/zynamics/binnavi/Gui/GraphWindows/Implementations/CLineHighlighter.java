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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations;

import com.google.security.zynamics.binnavi.ZyGraph.CHighlightLayers;
import com.google.security.zynamics.binnavi.disassembly.CCodeNodeHelpers;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.zylib.disassembly.IInstruction;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;



/**
 * Class that manages the highlighted instructions in a graph.
 */
public final class CLineHighlighter {
  /**
   * List of instructions in the current graph which are highlighted.
   */
  private final Set<IInstruction> m_highlightedInstructions = new HashSet<IInstruction>();

  /**
   * Highlights or unhighlights a line in a code node.
   *
   * @param node The node where the user clicked.
   * @param codeNode The node that provides the raw data for the node.
   * @param y The y location where the user clicked.
   */
  private void highlightLine(final NaviNode node, final INaviCodeNode codeNode, final double y) {
    final double yPos = y - node.getY();

    final int row = node.positionToRow(yPos);

    final INaviInstruction instruction = CCodeNodeHelpers.lineToInstruction(codeNode, row);

    if (instruction == null) {
      return;
    }

    // Lines can be toggled between "highlighted" and "not highlighted" depending
    // on the current status of the line.

    if (m_highlightedInstructions.contains(instruction)) {
      codeNode.setInstructionColor(instruction, CHighlightLayers.HIGHLIGHTING_LAYER, null);

      m_highlightedInstructions.remove(instruction);
    } else {
      codeNode.setInstructionColor(
          instruction, CHighlightLayers.HIGHLIGHTING_LAYER, new Color(0x36D0FE));

      m_highlightedInstructions.add(instruction);
    }
  }

  /**
   * Invoked when the user wants to highlight/unhighlight a line.
   *
   * @param node The node where the user clicked.
   * @param y The y location where the user clicked.
   */
  public void handleHighlightLine(final NaviNode node, final double y) {
    if (node.getRawNode() instanceof INaviCodeNode) {
      highlightLine(node, (INaviCodeNode) node.getRawNode(), y);
    }
  }
}
