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
 * Highlights the results of a register tracking operation in a graph.
 */
public final class CTrackingResultsHighlighter {
  /**
   * You are not supposed to instantiate this class.
   */
  private CTrackingResultsHighlighter() {
  }

  /**
   * Clears previous register tracking highlighting.
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

            node.clearHighlighting(CHighlightLayers.REGISTER_TRACKING_LAYER, line);
          }
        }

        return IterationMode.CONTINUE;
      }
    });
  }

  /**
   * Highlights the instructions of a node depending on the result of a register tracking operation.
   *
   * @param cnode The raw node that provides the data for the node.
   * @param startInstruction The start instruction of the operand tracking operation.
   * @param trackedRegister The tracked register.
   * @param instructionResult The instruction result to highlight.
   */
  private static void highlightCodeNode(final INaviCodeNode cnode,
      final INaviInstruction startInstruction, final String trackedRegister,
      final CInstructionResult instructionResult) {
    final INaviInstruction searchInstruction = instructionResult.getInstruction();

    if (!CCodeNodeHelpers.containsAddress(cnode, searchInstruction.getAddress())) {
      return;
    }

    for (final INaviInstruction instruction : cnode.getInstructions()) {
      if (searchInstruction == instruction) {
        cnode.setInstructionColor(instruction, CHighlightLayers.REGISTER_TRACKING_LAYER,
            CResultColor.determineBackgroundColor(
                startInstruction, trackedRegister, instructionResult));
      }
    }
  }

  /**
   * Highlights the currently selected instructions.
   *
   * @param graph The graph that is highlighted.
   * @param startInstruction The start instruction of the operand tracking operation.
   * @param trackedRegister The tracked register.
   * @param instructionResults The instruction results that are displayed in the graph.
   */
  private static void highlightInstructions(final ZyGraph graph,
      final INaviInstruction startInstruction, final String trackedRegister,
      final List<CInstructionResult> instructionResults) {
    for (final CInstructionResult instructionResult : instructionResults) {
      graph.iterate(new INodeCallback<NaviNode>() {
        @Override
        public IterationMode next(final NaviNode node) {
          if (node.getRawNode() instanceof INaviCodeNode) {
            final INaviCodeNode cnode = (INaviCodeNode) node.getRawNode();
            highlightCodeNode(cnode, startInstruction, trackedRegister, instructionResult);
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
   * @param startInstruction The start instruction of the operand tracking operation.
   * @param trackedRegister The tracked register.
   * @param instructionResults The instruction results that are displayed in the graph.
   */
  public static void updateHighlighting(final ZyGraph graph,
      final INaviInstruction startInstruction, final String trackedRegister,
      final List<CInstructionResult> instructionResults) {
    Preconditions.checkNotNull(graph, "IE01529: Graph argument can not be null");
    Preconditions.checkNotNull(
        startInstruction, "IE01687: Start instruction argument can not be null");
    Preconditions.checkNotNull(
        trackedRegister, "IE01688: Tracked register argument can not be null");
    Preconditions.checkNotNull(
        instructionResults, "IE01540: Instruction results argument can not be null");

    clearHighlighting(graph);

    highlightInstructions(graph, startInstruction, trackedRegister, instructionResults);
  }
}
