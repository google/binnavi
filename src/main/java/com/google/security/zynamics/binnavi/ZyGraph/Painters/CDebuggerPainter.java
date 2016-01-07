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
package com.google.security.zynamics.binnavi.ZyGraph.Painters;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.ZyGraph.CHighlightLayers;
import com.google.security.zynamics.binnavi.ZyGraph.Implementations.ZyZoomHelpers;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.disassembly.CCodeNodeHelpers;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.INodeCallback;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.ICodeNode;
import com.google.security.zynamics.zylib.types.common.IterationMode;

/**
 * This class is used to highlight the instruction at the current program counter during debugging.
 */
public final class CDebuggerPainter {
  /**
   * You are not supposed to instantiate this class.
   */
  private CDebuggerPainter() {
  }

  /**
   * Updates the debugger highlighting in a function node.
   *
   * @param address The address of the program counter to be highlighted.
   * @param node The node to be updated.
   * @param functionNode The function node that backs the node.
   */
  private static void updateDebuggerHighlighting(final UnrelocatedAddress address,
      final NaviNode node, final INaviFunctionNode functionNode) {
    if (functionNode.getFunction().getAddress().equals(address.getAddress())) {
      node.setHighlighting(CHighlightLayers.PROGRAM_COUNTER_LAYER, 1,
          ConfigManager.instance().getDebuggerColorSettings().getActiveLine());
    }
  }

  /**
   * Updates the debugger highlighting for an instruction in a code node.
   *
   * @param graph The graph where the highlighting is updated.
   * @param node The node to be updated.
   * @param codeNode The code node that backs the node.
   * @param instruction The instruction to highlight.
   */
  private static void updateDebuggerHighlighting(final ZyGraph graph, final NaviNode node,
      final INaviCodeNode codeNode, final INaviInstruction instruction) {
    node.setHighlighting(CHighlightLayers.PROGRAM_COUNTER_LAYER,
        CCodeNodeHelpers.instructionToLine(codeNode, instruction), 11, -1,
        ConfigManager.instance().getDebuggerColorSettings().getActiveLine());

    if (!node.getRawNode().isVisible()) {
      node.getRawNode().setVisible(true);

      if (graph.getSettings().getProximitySettings().getProximityBrowsing()) {
        graph.showNode(node, true);
      }

      graph.doLayout();
    }
  }

  /**
   * Updates the debugger highlighting in a code node.
   *
   * @param graph The graph where the highlighting is updated.
   * @param address The address of the program counter to be highlighted.
   * @param node The node to be updated.
   * @param codeNode The code node that backs the node.
   */
  private static void updateDebuggerHighlighting(final ZyGraph graph,
      final UnrelocatedAddress address, final NaviNode node, final INaviCodeNode codeNode) {
    for (final INaviInstruction instruction : codeNode.getInstructions()) {
      if (instruction.getAddress().equals(address.getAddress())) {
        updateDebuggerHighlighting(graph, node, codeNode, instruction);
      }
    }
  }

  /**
   * Removes the highlighting of the program counter from the whole graph.
   *
   * @param graph The graph from which the highlighting is removed.
   */
  public static void clearDebuggerHighlighting(final ZyGraph graph) {
    Preconditions.checkNotNull(graph, "IE02186: Graph argument can not be null");

    graph.iterate(new INodeCallback<NaviNode>() {
      @Override
      public IterationMode next(final NaviNode node) {
        final INaviViewNode rawNode = node.getRawNode();

        if (rawNode instanceof ICodeNode) {
          final INaviCodeNode codeNode = (INaviCodeNode) rawNode;

          for (final INaviInstruction instruction : codeNode.getInstructions()) {
            node.clearHighlighting(CHighlightLayers.PROGRAM_COUNTER_LAYER,
                CCodeNodeHelpers.instructionToLine(codeNode, instruction));
          }
        } else if (rawNode instanceof INaviFunctionNode) {
          node.clearHighlighting(CHighlightLayers.PROGRAM_COUNTER_LAYER, 1);
        }

        return IterationMode.CONTINUE;
      }
    });
  }

  /**
   * Updates the program counter highlighting in a whole graph.
   *
   * @param graph The graph where the highlighting is updated.
   * @param address The address of the program counter to be highlighted.
   * @param module The module in which the address is located.
   */
  public static void updateDebuggerHighlighting(
      final ZyGraph graph, final UnrelocatedAddress address, final INaviModule module) {
    Preconditions.checkNotNull(graph, "IE02187: Graph argument can not be null");
    Preconditions.checkNotNull(address, "IE02188: Address argument can not be null");

    graph.iterate(new INodeCallback<NaviNode>() {
      @Override
      public IterationMode next(final NaviNode node) {
        final INaviViewNode rawNode = node.getRawNode();

        if (rawNode instanceof ICodeNode) {
          final INaviCodeNode codeNode = (INaviCodeNode) rawNode;

          try {
            if (module.equals(codeNode.getParentFunction().getModule())) {
              updateDebuggerHighlighting(graph, address, node, codeNode);
            }
          } catch (final MaybeNullException exception) {
            CUtilityFunctions.logException(exception);
          }
        } else if (rawNode instanceof INaviFunctionNode) {
          final INaviFunctionNode functionNode = (INaviFunctionNode) rawNode;

          if (module.equals(functionNode.getFunction().getModule())) {
            updateDebuggerHighlighting(address, node, functionNode);
          }
        }
        return IterationMode.CONTINUE;
      }
    });

    ZyZoomHelpers.zoomToAddress(graph, address.getAddress(), module, false);
  }

  /**
   * Updates the debugger highlighting for a single node
   *
   * @param graph The graph in which the node is located
   * @param address The address of the breakpoint
   * @param node The node in which the breakpoint is located.
   */
  public static void updateSingleNodeDebuggerHighlighting(
      final ZyGraph graph, final UnrelocatedAddress address, final NaviNode node) {
    Preconditions.checkNotNull(graph, "IE01192: Graph argument can not be null");
    Preconditions.checkNotNull(address, "IE01216: Address argument can not be null");

    final INaviViewNode rawNode = node.getRawNode();

    if (rawNode instanceof ICodeNode) {
      final INaviCodeNode codeNode = (INaviCodeNode) rawNode;

      updateDebuggerHighlighting(graph, address, node, codeNode);
    } else if (rawNode instanceof INaviFunctionNode) {
      final INaviFunctionNode functionNode = (INaviFunctionNode) rawNode;

      updateDebuggerHighlighting(address, node, functionNode);
    }
  }
}
