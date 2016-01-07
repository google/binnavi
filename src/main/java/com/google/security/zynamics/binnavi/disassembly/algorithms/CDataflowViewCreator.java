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
package com.google.security.zynamics.binnavi.disassembly.algorithms;

import java.util.HashMap;
import java.util.Map;


import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.disassembly.CCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.reil.ReilFunction;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.algorithms.mono.OperandGraph;
import com.google.security.zynamics.reil.algorithms.mono.OperandGraphEdge;
import com.google.security.zynamics.reil.algorithms.mono.OperandGraphNode;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType;


/**
 * This class can be used to create a dataflow view from a normal view. Dataflow views show how
 * registers depend on each other.
 */
public final class CDataflowViewCreator {
  /**
   * You are not supposed to instantiate this class.
   */
  private CDataflowViewCreator() {
  }

  /**
   * Creates a new dataflow view.
   * 
   * @param container The container in which the dataflow view is created.
   * @param view The normal view that provides the control-flow information.
   * 
   * @return The created dataflow view.
   * 
   * @throws InternalTranslationException Thrown if the input view could not be translated to REIL.
   */
  public static INaviView create(final IViewContainer container, final INaviView view)
      throws InternalTranslationException {
    Preconditions.checkNotNull(container, "IE00411: Module argument can not be null");
    Preconditions.checkNotNull(view, "IE00414: View argument can not be null");

    final Map<IAddress, INaviInstruction> instructions = new HashMap<IAddress, INaviInstruction>();

    for (final CCodeNode codeNode : view.getBasicBlocks()) {
      for (final INaviInstruction instruction : codeNode.getInstructions()) {
        instructions.put(instruction.getAddress(), instruction);
      }
    }

    final ReilFunction function = view.getContent().getReilCode();
    final OperandGraph operandGraph = OperandGraph.create(function.getGraph());
    final INaviView dfView =
        container.createView(String.format("Data flow view of '%s'", view.getName()), "");
    final Map<OperandGraphNode, INaviCodeNode> nodeMap =
        new HashMap<OperandGraphNode, INaviCodeNode>();
    final Map<INaviInstruction, CCodeNode> instructionMap =
        new HashMap<INaviInstruction, CCodeNode>();

    for (final OperandGraphNode operandGraphNode : operandGraph) {
      final ReilInstruction reilInstruction = operandGraphNode.getInstruction();

      final INaviInstruction instruction =
          instructions.get(ReilHelpers.toNativeAddress(reilInstruction.getAddress()));

      if (instructionMap.containsKey(instruction)) {
        nodeMap.put(operandGraphNode, instructionMap.get(instruction));

        continue;
      }

      final CCodeNode codeNode =
          dfView.getContent().createCodeNode(null, Lists.newArrayList(instruction));

      codeNode.setColor(ConfigManager.instance().getColorSettings().getBasicBlocksColor());

      nodeMap.put(operandGraphNode, codeNode);

      instructionMap.put(instruction, codeNode);
    }

    for (final OperandGraphEdge edge : operandGraph.getEdges()) {
      final INaviCodeNode source = nodeMap.get(edge.getSource());
      final INaviCodeNode target = nodeMap.get(edge.getTarget());

      if (source.equals(target)) {
        continue;
      }

      dfView.getContent().createEdge(source, target, EdgeType.JUMP_UNCONDITIONAL);
    }

    return dfView;
  }
}
