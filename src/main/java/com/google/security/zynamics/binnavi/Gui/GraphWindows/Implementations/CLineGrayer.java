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

import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphModel;
import com.google.security.zynamics.binnavi.ZyGraph.Builders.ZyInstructionBuilder;
import com.google.security.zynamics.binnavi.ZyGraph.Builders.Modifiers.CDefaultModifier;
import com.google.security.zynamics.binnavi.disassembly.CCodeNodeHelpers;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.CStyleRunData;

import java.awt.Color;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class that manages the grayed instructions in a graph.
 */
public final class CLineGrayer {
  /**
   * List of instructions in the current graph which are grayed.
   */
  private final Set<IInstruction> m_grayedInstructions = new HashSet<IInstruction>();

  /**
   * Grays or ungrays a line in a code node.
   *
   * @param model The model of the graph the node belongs to.
   * @param node The node where the user clicked.
   * @param codeNode The node that provides the raw data for the node.
   * @param y The y location where the user clicked.
   */
  private void grayLine(
      final CGraphModel model, final NaviNode node, final INaviCodeNode codeNode, final double y) {
    final double yPos = y - node.getY();

    final int row = node.positionToRow(yPos);

    final INaviInstruction instruction = CCodeNodeHelpers.lineToInstruction(codeNode, row);

    if (instruction == null) {
      return;
    }

    // Lines can be toggled between "gray" and "not gray" depending
    // on the current status of the line.

    if (m_grayedInstructions.contains(instruction)) {
      final Pair<String, List<CStyleRunData>> content = ZyInstructionBuilder.buildInstructionLine(
          instruction, model.getGraph().getSettings(),
          new CDefaultModifier(model.getGraph().getSettings(), model.getDebuggerProvider()));

      for (final CStyleRunData style : content.second()) {
        node.setColor(row, style.getStart(), style.getLength(), style.getColor());
      }

      m_grayedInstructions.remove(instruction);
    } else {
      node.setColor(row, Color.LIGHT_GRAY);

      m_grayedInstructions.add(instruction);
    }
  }

  /**
   * Invoked when the user wants to gray/ungray a line.
   *
   * @param model The model of the graph the node belongs to.
   * @param node The node where the user clicked.
   * @param y The y location where the user clicked.
   */
  public void handleGrayLine(final CGraphModel model, final NaviNode node, final double y) {
    if (node.getRawNode() instanceof INaviCodeNode) {
      grayLine(model, node, (INaviCodeNode) node.getRawNode(), y);
    }
  }
}
