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

import java.util.List;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.ZyGraph.CHighlightLayers;
import com.google.security.zynamics.binnavi.disassembly.CCodeNodeHelpers;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.INodeCallback;
import com.google.security.zynamics.zylib.types.common.IterationMode;

/**
 * Highlights the results of a special instruction search operation in a graph.
 */
public final class CTypeResultsHighlighter {
  /**
   * You are not supposed to instantiate this class.
   */
  private CTypeResultsHighlighter() {
  }

  /**
   * Clears previous instruction search highlighting.
   *
   * @param graph The graph whose highlighting is cleared.
   */
  private static void clearHighlighting(final ZyGraph graph) {
    graph.iterate(new INodeCallback<NaviNode>() {
      @Override
      public IterationMode next(final NaviNode node) {
        if (node.getRawNode() instanceof INaviCodeNode) {
          final INaviCodeNode cnode = (INaviCodeNode) node.getRawNode();

          for (final INaviInstruction instruction : cnode.getInstructions()) {
            final int line = CCodeNodeHelpers.instructionToLine(cnode, instruction);

            node.clearHighlighting(CHighlightLayers.SPECIAL_INSTRUCTION_LAYER, line);
          }
        }

        return IterationMode.CONTINUE;
      }
    });
  }

  /**
   * Highlights the instructions of a node depending on the result of a instruction search
   * operation.
   *
   * @param cnode The raw node that provides the data for the node.
   * @param instructionResult The instruction result to highlight.
   */
  private static void highlightCodeNode(
      final INaviCodeNode cnode, final CSpecialInstruction instructionResult) {
    final INaviInstruction searchInstruction = instructionResult.getInstruction();

    if (!CCodeNodeHelpers.containsAddress(cnode, searchInstruction.getAddress())) {
      return;
    }

    for (final INaviInstruction instruction : cnode.getInstructions()) {
      if (searchInstruction == instruction) {
        cnode.setInstructionColor(instruction, CHighlightLayers.SPECIAL_INSTRUCTION_LAYER,
            instructionResult.getType().getColor());
      }
    }
  }

  /**
   * Highlights the currently selected instructions.
   *
   * @param graph The graph that is highlighted.
   * @param instructionResults The instruction results that are displayed in the graph.
   */
  private static void highlightInstructions(
      final ZyGraph graph, final List<CSpecialInstruction> instructionResults) {
    for (final CSpecialInstruction instructionResult : instructionResults) {
      graph.iterate(new INodeCallback<NaviNode>() {
        @Override
        public IterationMode next(final NaviNode node) {
          if (node.getRawNode() instanceof INaviCodeNode) {
            final INaviCodeNode cnode = (INaviCodeNode) node.getRawNode();

            highlightCodeNode(cnode, instructionResult);
          }

          return IterationMode.CONTINUE;
        }
      });
    }
  }

  /**
   * Updates the graph highlighting according to the current selection
   *
   * @param graph The graph that is highlighted.
   * @param instructionResults The instruction results that are displayed in the graph.
   */
  public static void updateHighlighting(
      final ZyGraph graph, final List<CSpecialInstruction> instructionResults) {
    Preconditions.checkNotNull(graph, "IE01686: Graph argument can not be null");
    Preconditions.checkNotNull(
        instructionResults, "IE01689: Instruction results argument can not be null");

    clearHighlighting(graph);

    highlightInstructions(graph, instructionResults);
  }
}
