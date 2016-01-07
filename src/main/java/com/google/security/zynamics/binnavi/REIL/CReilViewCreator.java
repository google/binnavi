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
package com.google.security.zynamics.binnavi.REIL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.disassembly.CCodeNode;
import com.google.security.zynamics.binnavi.disassembly.CNaviViewEdge;
import com.google.security.zynamics.binnavi.disassembly.COperandTree;
import com.google.security.zynamics.binnavi.disassembly.COperandTreeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.Builders.EdgeInitializer;
import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.OperandType;
import com.google.security.zynamics.reil.ReilBlock;
import com.google.security.zynamics.reil.ReilEdge;
import com.google.security.zynamics.reil.ReilGraph;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.ReilOperand;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.ReilGraphGenerator;
import com.google.security.zynamics.zylib.disassembly.ExpressionType;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.Convert;


/**
 * This class is used to create view objects from REIL code. This is used to display REIL code in
 * the GUI.
 */
public final class CReilViewCreator {
  /**
   * You are not supposed to instantiate this class.
   */
  private CReilViewCreator() {
  }

  /**
   * Converts a REIL operand into an instruction operand tree.
   * 
   * @param module The module where the operand tree is created.
   * @param operand The REIL operand to convert.
   * 
   * @return The created operand tree.
   */
  private static COperandTree convert(final INaviModule module, final ReilOperand operand) {
    final COperandTreeNode rootNode =
        module.createOperandExpression(toDatabaseString(operand.getSize()),
            ExpressionType.SIZE_PREFIX);

    final COperandTreeNode childNode =
        module.createOperandExpression(operand.getValue(), getType(operand));

    COperandTreeNode.link(rootNode, childNode);

    return module.createOperand(rootNode);
  }

  /**
   * Creates a special empty instruction operand to simulate empty REIL operands.
   * 
   * @param module The module where the operand tree is created.
   * 
   * @return The created operand tree.
   */
  private static COperandTree getEmptyOperand(final INaviModule module) {
    final COperandTreeNode rootNode =
        module.createOperandExpression("b4", ExpressionType.SIZE_PREFIX);

    final COperandTreeNode childNode = module.createOperandExpression(" ", ExpressionType.SYMBOL);

    COperandTreeNode.link(rootNode, childNode);

    return module.createOperand(rootNode);
  }

  /**
   * Returns the expression type of a given REIL operand.
   * 
   * @param operand The REIL operand.
   * 
   * @return The expression type of the given REIL operand.
   */
  private static ExpressionType getType(final ReilOperand operand) {
    // TODO: Handle sub-addresses.

    if (Convert.isDecString(operand.getValue())) {
      return ExpressionType.IMMEDIATE_INTEGER;
    } else {
      return ExpressionType.REGISTER;
    }
  }

  /**
   * Returns the database string of a given operand size.
   * 
   * @param size The operand size to convert.
   * 
   * @return The database string for the given operand size object.
   */
  private static String toDatabaseString(final OperandSize size) {
    switch (size) {
      case BYTE:
        return "b1";
      case WORD:
        return "b2";
      case DWORD:
        return "b4";
      case QWORD:
        return "b8";
      case OWORD:
        return "b16";
      case ADDRESS:
        return "b4";
      default:
        throw new IllegalArgumentException(String.format("Error: Unknown operand size %s", size));
    }
  }

  /**
   * Creates a REIL view from a view.
   * 
   * @param container The container in which the new REIL view is created.
   * @param view The view to be translated to REIL code.
   * 
   * @return The created REIL code view.
   * 
   * @throws InternalTranslationException Thrown if the view could not be translated to REIL code.
   * @throws CouldntLoadDataException
   */
  public static INaviView create(final INaviModule container, final INaviView view)
      throws InternalTranslationException, CouldntLoadDataException {
    Preconditions.checkNotNull(container, "IE01768: Container argument can not be null");
    Preconditions.checkNotNull(view, "IE01769: View argument can not be null");

    final Map<IAddress, String> textMap = new HashMap<IAddress, String>();

    for (final CCodeNode node : view.getBasicBlocks()) {
      for (final INaviInstruction instruction : node.getInstructions()) {
        textMap.put(instruction.getAddress(), instruction.toString());
      }
    }

    final INaviView reilView =
        CReilViewCreator.create(container, view.getContent().getReilCode().getGraph());

    for (final CCodeNode node : reilView.getBasicBlocks()) {
      for (final INaviInstruction reilInstruction : node.getInstructions()) {
        if ((reilInstruction.getAddress().toLong() & 0xFF) == 0) {
          try {
            node.getComments().appendLocalInstructionComment(reilInstruction,
                textMap.get(ReilHelpers.toNativeAddress(reilInstruction.getAddress())));
          } catch (final CouldntSaveDataException e) {
            // Not possible for unsaved views.

            CUtilityFunctions.logException(e);
          }
        }
      }
    }

    return reilView;
  }

  /**
   * Creates a REIL view object from a list of REIL instructions.
   * 
   * @param container The container in which the new REIL view is created.
   * @param instructions The instructions to be shown in the REIL view.
   * 
   * @return The created REIL code view.
   */
  public static INaviView create(final INaviModule container,
      final List<ReilInstruction> instructions) {
    Preconditions.checkNotNull(container, "IE01775: Container argument can not be null");
    Preconditions.checkNotNull(instructions, "IE01779: Instructions argument can not be null");

    final Collection<List<ReilInstruction>> instructionList =
        new ArrayList<List<ReilInstruction>>();
    instructionList.add(instructions);

    return create(container,
        ReilGraphGenerator.createGraph(instructionList, new ArrayList<IAddress>()));
  }

  /**
   * Creates a REIL view object from a REIL graph.
   * 
   * @param container The container in which the new REIL view is created.
   * @param graph The graph that contains the REIL code to be shown in the view.
   * 
   * @return The created REIL code view.
   */
  public static INaviView create(final INaviModule container, final ReilGraph graph) {
    Preconditions.checkNotNull(container, "IE01809: Container argument can not be null");
    Preconditions.checkNotNull(graph, "IE01815: Graph argument can not be null");

    final INaviView view = container.getContent().getViewContainer().createView("REIL View", "");

    final Map<ReilBlock, CCodeNode> nodeMap = new HashMap<ReilBlock, CCodeNode>();

    for (final ReilBlock block : graph) {
      final List<INaviInstruction> instructions = new ArrayList<INaviInstruction>();

      for (final ReilInstruction reilInstruction : block) {
        final List<COperandTree> operands = new ArrayList<COperandTree>();

        if (reilInstruction.getFirstOperand().getType() == OperandType.EMPTY) {
          operands.add(getEmptyOperand(container));
        } else {
          operands.add(convert(container, reilInstruction.getFirstOperand()));
        }

        if (reilInstruction.getSecondOperand().getType() == OperandType.EMPTY) {
          operands.add(getEmptyOperand(container));
        } else {
          operands.add(convert(container, reilInstruction.getSecondOperand()));
        }

        if (reilInstruction.getThirdOperand().getType() == OperandType.EMPTY) {
          operands.add(getEmptyOperand(container));
        } else {
          operands.add(convert(container, reilInstruction.getThirdOperand()));
        }

        final INaviInstruction convertedInstruction =
            container.createInstruction(reilInstruction.getAddress(),
                reilInstruction.getMnemonic(), operands, new byte[0], "REIL");

        instructions.add(convertedInstruction);
      }

      final CCodeNode node = view.getContent().createCodeNode(null, instructions);

      node.setColor(ConfigManager.instance().getColorSettings().getBasicBlocksColor());

      nodeMap.put(block, node);
    }

    for (final ReilEdge edge : graph.getEdges()) {
      final CNaviViewEdge reilEdge =
          view.getContent().createEdge(nodeMap.get(edge.getSource()),
              nodeMap.get(edge.getTarget()), edge.getType());

      EdgeInitializer.adjustColor(reilEdge);
    }

    return view;
  }
}
