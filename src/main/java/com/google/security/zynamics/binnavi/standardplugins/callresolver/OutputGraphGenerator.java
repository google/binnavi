/*
Copyright 2015 Google Inc. All Rights Reserved.

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
package com.google.security.zynamics.binnavi.standardplugins.callresolver;

import java.awt.Color;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.security.zynamics.binnavi.API.debug.MemoryModule;
import com.google.security.zynamics.binnavi.API.disassembly.Callgraph;
import com.google.security.zynamics.binnavi.API.disassembly.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.API.disassembly.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.API.disassembly.EdgeType;
import com.google.security.zynamics.binnavi.API.disassembly.Function;
import com.google.security.zynamics.binnavi.API.disassembly.FunctionEdge;
import com.google.security.zynamics.binnavi.API.disassembly.FunctionNode;
import com.google.security.zynamics.binnavi.API.disassembly.Module;
import com.google.security.zynamics.binnavi.API.disassembly.View;
import com.google.security.zynamics.binnavi.API.disassembly.ViewEdge;

/**
 * Generates the graph that shows the resolved function calls in a graph window.
 */
public final class OutputGraphGenerator {
  /**
   * Creates a view that shows all nodes and edges from the original call graph in addition to the
   * newly resolved functions.
   * 
   * @param target The target whose indirect modules were resolved.
   * @param indirectCallAddresses The addresses of the indirect call objects from the target.
   * @param resolvedAddresses The resolved function addresses.
   * 
   * @return The generated view.
   */
  public static View createCompleteView(final ICallResolverTarget target,
      final List<IndirectCall> indirectCallAddresses,
      final Map<BigInteger, Set<ResolvedFunction>> resolvedAddresses) {
    final View view = target.createView();

    final Map<Function, FunctionNode> nodes = new HashMap<Function, FunctionNode>();

    for (final Module module : target.getModules()) {
      for (final Function function : module.getFunctions()) {
        final FunctionNode node = view.createFunctionNode(function);

        nodes.put(function, node);
      }

      final Callgraph callgraph = module.getCallgraph();

      for (final FunctionEdge edge : callgraph.getEdges()) {
        final FunctionNode sourceNode = nodes.get(edge.getSource().getFunction());
        final FunctionNode targetNode = nodes.get(edge.getTarget().getFunction());

        view.createEdge(sourceNode, targetNode, EdgeType.JumpUnconditional);
      }
    }

    for (final Entry<BigInteger, Set<ResolvedFunction>> element : resolvedAddresses.entrySet()) {
      final BigInteger start = element.getKey();
      final Set<ResolvedFunction> targets = element.getValue();

      final IndirectCall call =
          IndirectCallResolver.findIndirectCall(target.getDebugger(), indirectCallAddresses, start);

      final FunctionNode sourceNode = nodes.get(call.getFunction());

      if (sourceNode != null) {
        for (final ResolvedFunction targetFunction : targets) {
          final Function function = targetFunction.getFunction();

          if (function != null) {
            final FunctionNode targetNode = nodes.get(function);

            final ViewEdge edge =
                view.createEdge(sourceNode, targetNode, EdgeType.JumpUnconditional);

            edge.setColor(Color.RED);
          }
        }
      }
    }

    return view;
  }

  /**
   * Creates a view that shows all nodes and edges from the original call graph in addition to the
   * newly resolved functions.
   * 
   * @param target The target whose indirect modules were resolved.
   * @param indirectCallAddresses The addresses of the indirect call objects from the target.
   * @param resolvedAddresses The resolved function addresses.
   * 
   * @return The generated view.
   */
  public static View createLoggedView(final ICallResolverTarget target,
      final List<IndirectCall> indirectCallAddresses,
      final Map<BigInteger, Set<ResolvedFunction>> resolvedAddresses) {
    final View view = target.createView();

    final Map<Function, FunctionNode> nodes = new HashMap<Function, FunctionNode>();

    for (final Entry<BigInteger, Set<ResolvedFunction>> element : resolvedAddresses.entrySet()) {
      final BigInteger start = element.getKey();
      final Set<ResolvedFunction> targets = element.getValue();

      final IndirectCall call =
          IndirectCallResolver.findIndirectCall(target.getDebugger(), indirectCallAddresses, start);

      FunctionNode sourceNode = nodes.get(call.getFunction());

      if (sourceNode == null) {
        sourceNode = view.createFunctionNode(call.getFunction());
        nodes.put(call.getFunction(), sourceNode);
      }

      for (final ResolvedFunction targetFunction : targets) {
        final Function function = targetFunction.getFunction();
        final MemoryModule memoryModule = targetFunction.getMemoryModule();

        if (function != null) {
          FunctionNode targetNode = nodes.get(function);

          if (targetNode == null) {
            targetNode = view.createFunctionNode(function);
            nodes.put(function, targetNode);
          }

          try {
            sourceNode.appendComment(start.toString(16).toUpperCase() + " -> "
                + function.getAddress().toHexString().toUpperCase());
          } catch (CouldntSaveDataException | CouldntLoadDataException e) {
            e.printStackTrace();
          }

          view.createEdge(sourceNode, targetNode, EdgeType.JumpUnconditional);
        } else if (memoryModule != null) {
          final String targetString =
              String.format("%s!%s", targetFunction.getMemoryModule().getName(), targetFunction
                  .getAddress().toHexString().toUpperCase());

          try {
            sourceNode.appendComment(start.toString(16).toUpperCase() + " -> " + targetString);
          } catch (CouldntSaveDataException | CouldntLoadDataException e) {
            e.printStackTrace();
          } 
        } else {
          final String targetString =
              "???!" + targetFunction.getAddress().toHexString().toUpperCase();

          try {
            sourceNode.appendComment(start.toString(16).toUpperCase() + " -> " + targetString);
          } catch (CouldntSaveDataException | CouldntLoadDataException e) {
            e.printStackTrace();
          } 
        }
      }
    }

    for (final Module module : target.getModules()) {
      final Callgraph callgraph = module.getCallgraph();

      for (final FunctionEdge edge : callgraph.getEdges()) {
        final FunctionNode source = nodes.get(edge.getSource().getFunction());
        final FunctionNode targetNode = nodes.get(edge.getTarget().getFunction());

        if ((source != null) && (targetNode != null)) {
          view.createEdge(source, targetNode, EdgeType.JumpUnconditional);
        }
      }
    }

    return view;
  }
}
