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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.viewReferences;

import com.google.security.zynamics.binnavi.ZyGraph.CHighlightLayers;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviGroupNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviTextNode;
import com.google.security.zynamics.binnavi.disassembly.algorithms.CNodeTypeSwitcher;
import com.google.security.zynamics.binnavi.disassembly.algorithms.INodeTypeCallback;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.INodeCallback;
import com.google.security.zynamics.zylib.types.common.IterationMode;

import java.awt.Color;
import java.util.Collection;

/**
 * Helper class used to highlight instructions that use a given variable.
 */
public final class CVariableHighlighter {
  /**
   * You are not supposed to instantiate this class.
   */
  private CVariableHighlighter() {}

  /**
   * Highlights the given instructions.
   *
   * @param graph The graph where the instructions are highlighted.
   * @param toHighlight The instructions to highlight.
   */
  public static void highlightInstructions(
      final ZyGraph graph, final Collection<INaviInstruction> toHighlight) {
    graph.iterate(new INodeCallback<NaviNode>() {
      @Override
      public IterationMode next(final NaviNode node) {
        CNodeTypeSwitcher.switchNode(node.getRawNode(), new INodeTypeCallback<Void>() {
          @Override
          public Void handle(final INaviCodeNode node) {
            for (final INaviInstruction instruction : node.getInstructions()) {
              if (toHighlight.contains(instruction)) {
                node.setInstructionColor(
                    instruction, CHighlightLayers.VARIABLE_LAYER, new Color(100, 160, 200));
              } else {
                node.setInstructionColor(instruction, CHighlightLayers.VARIABLE_LAYER, null);
              }
            }

            return null;
          }

          @Override
          public Void handle(final INaviFunctionNode node) {
            return null;
          }

          @Override
          public Void handle(final INaviGroupNode node) {
            return null;
          }

          @Override
          public Void handle(final INaviTextNode node) {
            return null;
          }
        });

        return IterationMode.CONTINUE;
      }
    });
  }
}
