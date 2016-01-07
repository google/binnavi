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
package com.google.security.zynamics.binnavi.ZyGraph.Implementations;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.disassembly.CCodeNodeHelpers;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.INodeCallback;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IViewNode;
import com.google.security.zynamics.zylib.types.common.IterationMode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.functions.ZoomFunctions;

/**
 * Contains code for zooming to nodes and addresses.
 */
public final class ZyZoomHelpers {
  /**
   * You are not supposed to instantiate this class.
   */
  private ZyZoomHelpers() {}

  /**
   * Uncollapses the parent groups of a node all the way up until all parent groups are uncollapsed
   * and the node can be made visible.
   *
   * @param node The node whose parent groups are uncollapsed.
   */
  private static void uncollapseParents(final IViewNode<?> node) {
    if (node.getParentGroup() != null) {
      uncollapseParents(node.getParentGroup());

      node.getParentGroup().setCollapsed(false);
    }
  }

  /**
   * Zooms to an address in a code node.
   *
   * @param graph The graph where zooming happens.
   * @param address The address to zoom to.
   * @param animate True, to animate the zoom operation.
   * @param node The node to zoom to if it contains the address.
   * @param codeNode The code node that backs the visible node.
   *
   * @return True, if the node contains the address and was zoomed to.
   */
  static boolean zoomToAddress(final ZyGraph graph, final IAddress address, final boolean animate,
      final NaviNode node, final INaviCodeNode codeNode) {
    for (final INaviInstruction instruction : codeNode.getInstructions()) {
      if (instruction.getAddress().equals(address)) {
        uncollapseParents(codeNode);

        if (!node.isVisible()) {
          graph.showNode(node, true);
        }

        ZoomFunctions.zoomToNode(
            graph, node, CCodeNodeHelpers.instructionToLine(codeNode, instruction), animate);

        return true;
      }
    }

    return false;
  }

  /**
   * Zooms to an address in a function node.
   *
   * @param graph The graph where zooming happens.
   * @param address The address to zoom to.
   * @param node The node to zoom to if it contains the address.
   * @param functionNode The code node that backs the visible node.
   *
   * @return True, if the node contains the address and was zoomed to.
   */
  static boolean zoomToAddress(final ZyGraph graph, final IAddress address, final NaviNode node,
      final INaviFunctionNode functionNode) {
    if (functionNode.getFunction().getAddress().equals(address)) {
      uncollapseParents(functionNode);

      if (!node.isVisible()) {
        graph.showNode(node, true);
      }

      ZoomFunctions.zoomToNode(graph, node);

      return true;
    }

    return false;
  }

  /**
   * Zooms to the first occurrence of an address in a graph.
   *
   * @param graph The graph where the zoom operation takes place.
   * @param address The address to zoom to.
   */
  public static void zoomToAddress(final ZyGraph graph, final IAddress address) {
    graph.iterate(new INodeCallback<NaviNode>() {
      @Override
      public IterationMode next(final NaviNode node) {
        if (node.getRawNode() instanceof INaviCodeNode) {
          final INaviCodeNode codeNode = (INaviCodeNode) node.getRawNode();

          for (final INaviInstruction instruction : codeNode.getInstructions()) {
            if (instruction.getAddress().equals(address)) {
              uncollapseParents(codeNode);
              graph.showNode(node, true);
              ZoomFunctions.zoomToNode(graph, node);
              return IterationMode.STOP;
            }
          }
        } else if (node.getRawNode() instanceof INaviFunctionNode) {
          final INaviFunctionNode functionNode = (INaviFunctionNode) node.getRawNode();

          if (functionNode.getFunction().getAddress().equals(address)) {
            uncollapseParents(functionNode);
            graph.showNode(node, true);
            ZoomFunctions.zoomToNode(graph, node);
            return IterationMode.STOP;
          }
        }
        return IterationMode.CONTINUE;
      }
    });
  }

  /**
   * Zooms to the first occurrence of an address in a graph.
   *
   * @param graph The graph where the zoom operation takes place.
   * @param address The address to zoom to.
   * @param module The module in which the address is located.
   * @param animate True, to animate the zoom operation.
   * @return True, if the address was found in the graph. False, otherwise.
   */
  public static boolean zoomToAddress(final ZyGraph graph, final IAddress address,
      final INaviModule module, final boolean animate) {
    Preconditions.checkNotNull(graph, "IE02105: Graph argument can not be null");
    Preconditions.checkNotNull(address, "IE02106: Address argument can not be null");
    final InternalNodeCallBack callBack = new InternalNodeCallBack(module, graph, address, animate);
    graph.iterate(callBack);
    return callBack.nodeFound();
  }



  /**
   * Zooms to an instruction in a graph.
   *
   * @param graph The graph where the zoom operation takes place.
   * @param instruction The instruction to zoom to.
   */
  public static void zoomToInstruction(final ZyGraph graph, final INaviInstruction instruction) {
    graph.iterate(new INodeCallback<NaviNode>() {
      @Override
      public IterationMode next(final NaviNode node) {
        if (node.getRawNode() instanceof INaviCodeNode) {
          final INaviCodeNode codeNode = (INaviCodeNode) node.getRawNode();

          if (codeNode.hasInstruction(instruction)) {
            uncollapseParents(codeNode);

            graph.showNode(node, true);

            ZoomFunctions.zoomToNode(graph, node);

            return IterationMode.STOP;
          }
        }

        return IterationMode.CONTINUE;
      }
    });
  }
}


/**
 * Internal node call back. This class exists as we need to be able to ask if the node has been
 * found or not. 
 */
class InternalNodeCallBack implements INodeCallback<NaviNode> {

  private boolean nodeFound = false;
  private final INaviModule module;
  private final ZyGraph graph;
  private final IAddress address;
  private final boolean animate;

  public InternalNodeCallBack(
      final INaviModule module, ZyGraph graph, final IAddress address, boolean animate) {
    this.module = module;
    this.graph = graph;
    this.address = address;
    this.animate = animate;
  }

  @Override
  public IterationMode next(final NaviNode node) {
    if (node.getRawNode() instanceof INaviCodeNode) {
      final INaviCodeNode codeNode = (INaviCodeNode) node.getRawNode();

      try {
        if (module.equals(codeNode.getParentFunction().getModule())) {
          if (ZyZoomHelpers.zoomToAddress(graph, address, animate, node, codeNode)) {
            nodeFound = true;
            return IterationMode.STOP;
          }
        }
      } catch (final MaybeNullException exception) {
        CUtilityFunctions.logException(exception);
      }
    } else if (node.getRawNode() instanceof INaviFunctionNode) {
      final INaviFunctionNode functionNode = (INaviFunctionNode) node.getRawNode();

      if (module.equals(functionNode.getFunction().getModule())) {
        if (ZyZoomHelpers.zoomToAddress(graph, address, node, functionNode)) {
          nodeFound = true;
          return IterationMode.STOP;
        }
      }
    }

    return IterationMode.CONTINUE;
  }

  public boolean nodeFound() {
    return nodeFound;
  }
}
