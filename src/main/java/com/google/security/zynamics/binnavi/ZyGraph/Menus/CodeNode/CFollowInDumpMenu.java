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
package com.google.security.zynamics.binnavi.ZyGraph.Menus.CodeNode;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Gui.Debug.MemoryPanel.Implementations.CExpressionEvaluationException;
import com.google.security.zynamics.binnavi.Gui.Debug.MemoryPanel.Implementations.CExpressionEvaluator;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations.CGraphDebugger;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.PerspectiveType;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.CGotoOperandExpressionAction;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ProcessHelpers;
import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.RegisterValue;
import com.google.security.zynamics.binnavi.disassembly.CCodeNodeHelpers;
import com.google.security.zynamics.binnavi.disassembly.COperandTreeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviOperandTreeNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.ExpressionType;
import com.google.security.zynamics.zylib.strings.Commafier;
import com.google.security.zynamics.zylib.types.common.CollectionHelpers;
import com.google.security.zynamics.zylib.types.common.ICollectionMapper;

import java.math.BigInteger;
import java.util.List;

import javax.swing.JPopupMenu;

/**
 * Contains code for adding the Follow in Dump menu to a code node menu.
 */
public final class CFollowInDumpMenu {
  /**
   * You are not supposed to instantiate this class.
   */
  private CFollowInDumpMenu() {
  }

  /**
   * Checks what follow in dump menus are needed and adds those that are missing.
   *
   * @param menu The popup menu to extend.
   * @param viewModel Debug perspective model that shows the memory.
   * @param debugger Provides the register values.
   * @param activeThread The thread whose register values should be considered.
   * @param module Needed for relocation information.
   * @param treeNode The clicked operand tree node.
   *
   * @return True, if at least one menu was added. False, otherwise.
   */
  private static boolean addFollowInDumpMenu(final JPopupMenu menu,
      final CDebugPerspectiveModel viewModel, final IDebugger debugger, final TargetProcessThread activeThread,
      final INaviModule module, final COperandTreeNode treeNode) {
    final BigInteger simpleAddress =
        getSimpleAddress(treeNode, activeThread.getRegisterValues(), debugger, module);

    if ((simpleAddress != null) && needsSimpleFollowMenu(debugger, treeNode, simpleAddress)) {
      menu.add(new CGotoOperandExpressionAction(viewModel, treeNode.getValue(), simpleAddress));

      return true;
    }

    final BigInteger expressionAddress =
        getExpressionAddress(treeNode, activeThread.getRegisterValues(), debugger, module);

    // Don't show menu twice for instructions like "mov eax, [someSymbol]"
    if ((expressionAddress != null) && !expressionAddress.equals(simpleAddress)
        && needsAddressExpressionMenu(debugger, treeNode, expressionAddress)) {
      final INaviOperandTreeNode addressExpression = getAddressExpression(treeNode);

      menu.add(new CGotoOperandExpressionAction(viewModel, toString(addressExpression),
          expressionAddress));

      return true;
    }

    return false;
  }

  /**
   * Returns the address expression that encloses a given node.
   *
   * @param node Node enclosed by the address expression.
   *
   * @return The address expression.
   */
  private static INaviOperandTreeNode getAddressExpression(final INaviOperandTreeNode node) {
    Preconditions.checkNotNull(node, "IE02370: node argument can not be null");
    if (node.getType() == ExpressionType.MEMDEREF) {
      return node.getChildren().get(0);
    } else if (node.getParent() == null) {
      throw new IllegalStateException("IE00705: Operand tree node.getParent is null");
    } else {
      return getAddressExpression(node.getParent());
    }
  }

  /**
   * Evaluates a clicked operand assuming that it is a memory access expression.
   *
   * @param treeNode The clicked operand.
   * @param registerValues Provides the current register values needed for evaluation.
   * @param debugger Provides relocation information.
   * @param module Provides relocation information.
   *
   * @return The evaluated clicked operand or null if evaluation failed.
   */
  private static BigInteger getExpressionAddress(final COperandTreeNode treeNode,
      final ImmutableList<RegisterValue> registerValues, final IDebugger debugger,
      final INaviModule module) {
    if (!isAddressExpression(treeNode)) {
      return null;
    }
    final INaviOperandTreeNode addressExpression = getAddressExpression(treeNode);

    try {
      return CExpressionEvaluator.evaluateExpression(addressExpression, registerValues, debugger,
          module);
    } catch (final CExpressionEvaluationException exception) {
      CUtilityFunctions.logException(exception);
    }

    return null;
  }

  /**
   * Evaluates a clicked operand assuming that it is a register or a literal.
   *
   * @param treeNode The clicked operand.
   * @param registerValues Provides the current register values needed for evaluation.
   * @param debugger Provides relocation information.
   * @param module Provides relocation information.
   *
   * @return The evaluated clicked operand or null if evaluation failed.
   */
  private static BigInteger getSimpleAddress(final COperandTreeNode treeNode,
      final ImmutableList<RegisterValue> registerValues, final IDebugger debugger,
      final INaviModule module) {
    if ((treeNode.getType() == ExpressionType.REGISTER)
        || (treeNode.getType() == ExpressionType.IMMEDIATE_INTEGER)) {
      try {
        return CExpressionEvaluator.evaluateExpression(treeNode, registerValues, debugger, module);
      } catch (final CExpressionEvaluationException exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    return null;
  }

  /**
   * Finds out whether a given node is part of an address expression.
   *
   * @param node The node to check.
   *
   * @return True, if the node is part of an address expression. False, otherwise.
   */
  private static boolean isAddressExpression(final INaviOperandTreeNode node) {
    if (node.getType() == ExpressionType.MEMDEREF) {
      return true;
    }

    return node.getParent() == null ? false : isAddressExpression(node.getParent());
  }

  /**
   * Checks whether a context menu should be extended with a follow-in-dump menu for a memory
   * dereference expression.
   *
   * @param debugger Provides relocation information.
   * @param treeNode The clicked node.
   * @param expressionAddress The expression address.
   *
   * @return True, if the menu should be extended.
   */
  private static boolean needsAddressExpressionMenu(final IDebugger debugger,
      final COperandTreeNode treeNode, final BigInteger expressionAddress) {
    return isAddressExpression(treeNode)
        && (ProcessHelpers.getSectionWith(debugger.getProcessManager().getMemoryMap(),
            new CAddress(expressionAddress)) != null);
  }

  /**
   * Checks whether a context menu should be extended with a follow-in-dump menu for a simple
   * expression.
   *
   * @param debugger Provides relocation information.
   * @param treeNode The clicked node.
   * @param simpleAddress The simple address.
   *
   * @return True, if the menu should be extended.
   */
  private static boolean needsSimpleFollowMenu(final IDebugger debugger,
      final COperandTreeNode treeNode, final BigInteger simpleAddress) {
    return (treeNode.getType() == ExpressionType.REGISTER)
        || ((treeNode.getType() == ExpressionType.IMMEDIATE_INTEGER) && (ProcessHelpers
            .getSectionWith(debugger.getProcessManager().getMemoryMap(),
                new CAddress(simpleAddress)) != null));
  }

  /**
   * Creates a printable string for a node and its children.
   *
   * @param node The node.
   *
   * @return The printable string for the node.
   */
  private static String toString(final INaviOperandTreeNode node) {
    switch (node.getType()) {
      case IMMEDIATE_INTEGER:
      case REGISTER:
        return node.getValue();
      case OPERATOR:
        return Commafier.commafy(toString(node.getChildren()), " " + node.getValue() + " ");
      default:
        throw new IllegalStateException("IE00711: This should not happen");
    }
  }

  /**
   * Creates printable strings for a list of nodes.
   *
   * @param nodes The nodes.
   *
   * @return A list of printable strings for the given nodes.
   */
  private static List<String> toString(final List<INaviOperandTreeNode> nodes) {
    return CollectionHelpers.map(nodes, new ICollectionMapper<INaviOperandTreeNode, String>() {
      @Override
      public String map(final INaviOperandTreeNode item) {
        return CFollowInDumpMenu.toString(item);
      }
    });
  }

  /**
   * Adds the menu that follow in dump menu for the clicked instruction.
   *
   * @param menu The code node menu that is extended.
   * @param model The graph model that provides information about the graph.
   * @param node The node whose menu is created.
   * @param clickedObject The object that was clicked.
   * @param y The y-coordinate of the click.
   */
  public static void addFollowInDumpMenu(final JPopupMenu menu, final CGraphModel model,
      final NaviNode node, final Object clickedObject, final double y) {
    Preconditions.checkNotNull(menu, "IE02371: menu argument can not be null");
    Preconditions.checkNotNull(model, "IE02372: model argument can not be null");
    Preconditions.checkNotNull(node, "IE02373: node argument can not be null");

    final int line = node.positionToRow(y);

    if (line == -1) {
      return;
    }

    final INaviCodeNode codeNode = (INaviCodeNode) node.getRawNode();

    final INaviInstruction instruction = CCodeNodeHelpers.lineToInstruction(codeNode, line);

    if (instruction != null) {
      final IDebugger debugger =
          CGraphDebugger.getDebugger(model.getDebuggerProvider(), instruction);

      if ((debugger != null) && (clickedObject instanceof COperandTreeNode)) {
        final TargetProcessThread activeThread = debugger.getProcessManager().getActiveThread();

        if (activeThread != null) {
          final CDebugPerspectiveModel viewModel =
              (CDebugPerspectiveModel) model.getGraphPanel().getViewModel()
                  .getModel(PerspectiveType.DebugPerspective);
          final COperandTreeNode treeNode = (COperandTreeNode) clickedObject;

          final boolean added =
              addFollowInDumpMenu(menu, viewModel, debugger, activeThread, instruction.getModule(),
                  treeNode);

          if (added) {
            menu.addSeparator();
          }
        }
      }
    }
  }
}
