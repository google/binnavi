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
package com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations;

import com.google.security.zynamics.binnavi.disassembly.CCallgraph;
import com.google.security.zynamics.binnavi.disassembly.CFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.ICallgraphEdge;
import com.google.security.zynamics.binnavi.disassembly.ICallgraphNode;
import com.google.security.zynamics.binnavi.disassembly.INaviAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.binnavi.disassembly.algorithms.CFunctionNodeColorizer;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class used to create a view that contains the combined call graph of all modules of an address
 * space.
 */
public final class CCallgraphCombiner {
  /**
   * You are not supposed to instantiate this class.
   */
  private CCallgraphCombiner() {
  }

  /**
   * Returns the resolved function of a given function.
   * 
   * @param function The function to resolve.
   * @param modules The modules to search through for the resolved function.
   * 
   * @return The resolved function or null if the function could not be resolved.
   */
  private static INaviFunction getResolvedFunction(final INaviFunction function,
      final List<INaviModule> modules) {
    if (function.getForwardedFunctionAddress() == null) {
      return null;
    }

    for (final INaviModule module : modules) {
      if (module.getConfiguration().getId() == function.getForwardedFunctionModuleId()) {
        return module.getContent().getFunctionContainer()
            .getFunction(function.getForwardedFunctionAddress());
      }
    }

    return null;
  }

  /**
   * Combines the call graphs of the modules of an address space.
   * 
   * @param project The project where the combined view is created.
   * @param addressSpace Provides the modules whose call graphs are combined.
   * 
   * @return The view that contains the combined call graph.
   */
  public static INaviView createCombinedCallgraph(final INaviProject project,
      final INaviAddressSpace addressSpace) {
    final INaviView view = project.getContent().createView("Combined Callgraph", "");

    final Map<INaviFunction, CFunctionNode> nodeMap = new HashMap<INaviFunction, CFunctionNode>();
    final Map<INaviFunction, INaviFunction> resolvedMap =
        new HashMap<INaviFunction, INaviFunction>();

    final List<INaviModule> modules = addressSpace.getContent().getModules();

    // In the first step we create all the necessary nodes for the regular
    // functions that are not forwarded.
    for (final INaviModule module : modules) {
      final CCallgraph callgraph = module.getContent().getNativeCallgraph();

      for (final ICallgraphNode callgraphNode : callgraph) {
        final INaviFunction function = callgraphNode.getFunction();

        final INaviFunction resolvedFunction = getResolvedFunction(function, modules);

        if (resolvedFunction == null) {
          final CFunctionNode node = view.getContent().createFunctionNode(function);

          node.setColor(CFunctionNodeColorizer.getFunctionColor(function.getType()));

          nodeMap.put(function, node);
        } else {
          resolvedMap.put(function, resolvedFunction);
        }
      }
    }

    // In the second step we created the edges between the nodes with special consideration
    // for the forwarded functions.
    for (final INaviModule module : modules) {
      final CCallgraph callgraph = module.getContent().getNativeCallgraph();

      for (final ICallgraphEdge callgraphEdge : callgraph.getEdges()) {
        final INaviFunction source =
            resolvedMap.containsKey(callgraphEdge.getSource().getFunction()) ? resolvedMap
                .get(callgraphEdge.getSource().getFunction()) : callgraphEdge.getSource()
                .getFunction();
        final INaviFunction target =
            resolvedMap.containsKey(callgraphEdge.getTarget().getFunction()) ? resolvedMap
                .get(callgraphEdge.getTarget().getFunction()) : callgraphEdge.getTarget()
                .getFunction();

        view.getContent().createEdge(nodeMap.get(source), nodeMap.get(target),
            EdgeType.JUMP_UNCONDITIONAL);
      }
    }

    return view;
  }

}
