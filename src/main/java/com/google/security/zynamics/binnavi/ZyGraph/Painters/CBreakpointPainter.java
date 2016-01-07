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
import com.google.security.zynamics.binnavi.ZyGraph.CHighlightLayers;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointManager;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointType;
import com.google.security.zynamics.binnavi.disassembly.CCodeNodeHelpers;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.INodeCallback;
import com.google.security.zynamics.zylib.types.common.IterationMode;

/**
 * The breakpoint painter class is used to paint breakpoints into code node and function nodes.
 */
public final class CBreakpointPainter {
  /**
   * You are not supposed to instantiate this class.
   */
  private CBreakpointPainter() {
  }

  /**
   * Paints breakpoints into a view node.
   * 
   * @param manager The breakpoint manager that provides breakpoint information.
   * @param node The visible BinNavi node where the breakpoint is painted.
   */
  private static void paintBreakpoints(final BreakpointManager manager, final NaviNode node) {
    if (node.getRawNode() instanceof INaviCodeNode) {
      paintBreakpoints(manager, node, (INaviCodeNode) node.getRawNode());
    } else if (node.getRawNode() instanceof INaviFunctionNode) {
      paintBreakpoints(manager, node, (INaviFunctionNode) node.getRawNode());
    }
  }

  /**
   * Paints breakpoints into code nodes.
   * 
   * @param manager The breakpoint manager that provides breakpoint information.
   * @param node The visible BinNavi node where the breakpoint is painted.
   * @param codeNode The code node that contains the raw data for the BinNavi node.
   */
  public static void paintBreakpoints(final BreakpointManager manager, final NaviNode node,
      final INaviCodeNode codeNode) {
    Preconditions.checkNotNull(manager, "IE02171: Manager argument can not be null");
    Preconditions.checkNotNull(node, "IE02172: Node argument can not be null");
    Preconditions.checkNotNull(codeNode, "IE02173: Code node argument can not be null");

    for (final INaviInstruction instruction : codeNode.getInstructions()) {
      final BreakpointAddress address =
          new BreakpointAddress(instruction.getModule(), new UnrelocatedAddress(
              instruction.getAddress()));

      final int line = CCodeNodeHelpers.instructionToLine(codeNode, instruction);

      if (manager.hasBreakpoint(BreakpointType.REGULAR, address)) {
        // Only the address is highlighted when breakpoints are set on instructions
        // visible in code nodes.

        final int addressCharacters = address.getAddress().getAddress().toHexString().length();

        node.setHighlighting(CHighlightLayers.BREAKPOINT_LAYER, line, 0,
            addressCharacters, BreakpointManager.getBreakpointColor(
                manager.getBreakpointStatus(address, BreakpointType.REGULAR)));
      } else {
        // If there is no breakpoint, clear potential older breakpoint line.

        node.clearHighlighting(CHighlightLayers.BREAKPOINT_LAYER, line);
      }
    }
  }

  /**
   * Paints breakpoints into a function node.
   * 
   * @param manager The breakpoint manager that provides breakpoint information.
   * @param node The visible BinNavi node where the breakpoint is painted.
   * @param functionNode The function node that contains the raw data for the BinNavi node.
   */
  public static void paintBreakpoints(final BreakpointManager manager, final NaviNode node,
      final INaviFunctionNode functionNode) {
    Preconditions.checkNotNull(manager, "IE02374: Manager argument can not be null");
    Preconditions.checkNotNull(node, "IE02375: Node argument can not be null");
    Preconditions.checkNotNull(functionNode, "IE02376: Code node argument can not be null");

    final INaviFunction function = functionNode.getFunction();

    final INaviModule module = function.getModule();

    final int FUNCTION_BREAKPOINT_LINE = 1;

    final BreakpointAddress address =
        new BreakpointAddress(module, new UnrelocatedAddress(function.getAddress()));

    if (manager.hasBreakpoint(BreakpointType.REGULAR, address)) {
      node.setHighlighting(CHighlightLayers.BREAKPOINT_LAYER,
          FUNCTION_BREAKPOINT_LINE, BreakpointManager.getBreakpointColor(
              manager.getBreakpointStatus(address, BreakpointType.REGULAR)));
    } else {
      // If there is no breakpoint, clear potential older breakpoint line.
      node.clearHighlighting(500, FUNCTION_BREAKPOINT_LINE);
    }
  }

  /**
   * Paints breakpoints on all nodes of a graph.
   * 
   * @param manager The breakpoint manager that provides breakpoint information.
   * @param graph The graph where the breakpoints are painted.
   */
  public static void paintBreakpoints(final BreakpointManager manager, final ZyGraph graph) {
    Preconditions.checkNotNull(manager, "IE02179: Manager argument can not be null");
    Preconditions.checkNotNull(graph, "IE02180: Graph argument can not be null");

    // TODO this can done more efficiently.
    graph.iterate(new INodeCallback<NaviNode>() {
      @Override
      public IterationMode next(final NaviNode node) {
        paintBreakpoints(manager, node);

        return IterationMode.CONTINUE;
      }
    });
  }

  /**
   * Paints breakpoints on a specific address of a graph.
   * 
   * @param manager The breakpoint manager that provides breakpoint information.
   * @param graph The graph where the breakpoints are painted.
   * @param breakpointAddress The breakpoint address where the breakpoints are set.
   */
  public static void paintBreakpoints(final BreakpointManager manager, final ZyGraph graph,
      final BreakpointAddress breakpointAddress) {
    Preconditions.checkNotNull(manager, "IE02181: Manager argument can not be null");
    Preconditions.checkNotNull(graph, "IE02182: Graph argument can not be null");
    Preconditions.checkNotNull(breakpointAddress,
        "IE02183: Breakpoint address node argument can not be null");

    // TODO this can done more efficiently.
    graph.iterate(new INodeCallback<NaviNode>() {
      @Override
      public IterationMode next(final NaviNode naviNode) {
        paintBreakpoints(manager, naviNode);

        return IterationMode.CONTINUE;
      }
    });
  }
}
