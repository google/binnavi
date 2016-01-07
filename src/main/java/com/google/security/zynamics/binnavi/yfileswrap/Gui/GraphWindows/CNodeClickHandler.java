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
package com.google.security.zynamics.binnavi.yfileswrap.Gui.GraphWindows;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.RegisterTracker.CTracking;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.InitialTab;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Extensions.ICodeNodeExtension;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations.CGraphDebugger;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations.CGraphOpener;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations.CGraphZoomer;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations.CLineGrayer;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations.CLineHighlighter;
import com.google.security.zynamics.binnavi.ZyGraph.Builders.Wrappers.CGlobalNodeCommentWrapper;
import com.google.security.zynamics.binnavi.ZyGraph.Builders.Wrappers.CLocalNodeCommentWrapper;
import com.google.security.zynamics.binnavi.ZyGraph.Implementations.CNodeFunctions;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.ZyGraphPopupMenus;
import com.google.security.zynamics.binnavi.disassembly.CCodeNodeHelpers;
import com.google.security.zynamics.binnavi.disassembly.COperandTreeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviGroupNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviOperandTree;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.yfileswrap.Gui.GraphWindows.Implementations.CGraphDialogs;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.reil.algorithms.mono2.common.enums.AnalysisDirection;
import com.google.security.zynamics.reil.algorithms.mono2.registertracking.RegisterTrackingOptions;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.zylib.disassembly.ExpressionType;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.disassembly.IOperandTreeNode;
import com.google.security.zynamics.zylib.disassembly.IReference;
import com.google.security.zynamics.zylib.disassembly.ReferenceType;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyNodeContentHelpers;

import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JPopupMenu;

/**
 * Class that handles clicks on nodes of a graph.
 */
public final class CNodeClickHandler {
  /**
   * Represents the displayed graph whose nodes were clicked.
   */
  private final CGraphModel m_model;

  /**
   * Used to gray individual lines of a node.
   */
  private final CLineGrayer m_grayer = new CLineGrayer();

  /**
   * Used to highlight individual lines of a node.
   */
  private final CLineHighlighter m_highlighter = new CLineHighlighter();

  /**
   * Creates a new node click handler object.
   *
   * @param model Represents the displayed graph whose nodes were clicked.
   */
  public CNodeClickHandler(final CGraphModel model) {
    m_model = Preconditions.checkNotNull(model, "IE01628: Model argument can not be null");
  }

  /**
   * Collects all code references from an operand tree node and all of its children.
   *
   * @param node The root node where reference collection starts.
   * @param references The collected references are stored here.
   */
  private void collectReferences(final IOperandTreeNode node, final Set<IAddress> references) {
    for (final IReference reference : node.getReferences()) {
      if (ReferenceType.isCodeReference(reference.getType())) {
        references.add(reference.getTarget());
      }
    }

    for (final IOperandTreeNode child : node.getChildren()) {
      collectReferences(child, references);
    }
  }

  /**
   * Invoked when the user wants to set or remove breakpoints.
   *
   * @param node The node where the user clicked.
   * @param y The y location where the user clicked.
   */
  private void handleBreakpointClick(final NaviNode node, final double y) {
    if (node.getRawNode() instanceof INaviCodeNode) {
      final int row = node.positionToRow(y - node.getY());

      CGraphDebugger.toggleBreakpoint(m_model.getDebuggerProvider(),
          (INaviCodeNode) node.getRawNode(), row);
    } else if (node.getRawNode() instanceof INaviFunctionNode) {
      CGraphDebugger.toggleBreakpoint(m_model.getDebuggerProvider(),
          (INaviFunctionNode) node.getRawNode());
    }
  }

  /**
   * Handles right-clicks on nodes.
   *
   * @param node The node where the user clicked.
   * @param event Mouse-event that was created when the user clicked.
   * @param x The x location where the user clicked.
   * @param y The y location where the user clicked.
   * @param extensions List of objects that extend code node context menus.
   */
  private void handleRightClick(final NaviNode node, final MouseEvent event, final double x,
      final double y, final List<ICodeNodeExtension> extensions) {
    final Object positionObject = ZyNodeContentHelpers.getObject(node, x, y);

    if (event.isAltDown() && event.isShiftDown()) {
      m_grayer.handleGrayLine(m_model, node, y);
    } else if (event.isAltDown() && event.isControlDown()) {
      m_highlighter.handleHighlightLine(node, y);
    } else if (event.isAltDown()) {
      if (positionObject instanceof CLocalNodeCommentWrapper) {
        CNodeFunctions.editNodeComments(m_model,
            ((CLocalNodeCommentWrapper) positionObject).getNode(), InitialTab.LocalNodeComments);
      } else if (positionObject instanceof CGlobalNodeCommentWrapper) {
        CNodeFunctions.editNodeComments(m_model,
            ((CGlobalNodeCommentWrapper) positionObject).getNode(), InitialTab.GlobalNodeComments);
      } else if (node.getRawNode() instanceof INaviCodeNode) {
        handleShowInstructionComment(node, y);
      }
    } else if (event.isShiftDown()) {
      handleBreakpointClick(node, y);
    } else if (event.isControlDown() && positionObject instanceof COperandTreeNode) {
      handleDoRegisterTrackingDown(node, y, x, (COperandTreeNode) positionObject);
    } else if (event.isControlDown() && event.isShiftDown()
        && positionObject instanceof COperandTreeNode) {
      handleDoRegisterTrackingUp(node, y, x, (COperandTreeNode) positionObject);
    } else {
      showPopupMenu(node, event, positionObject, y, extensions);
    }
  }

  /**
   * Sets the direction of the register tracker analysis to down and then calls the
   * handleRegisterTracker function.
   *
   * @param node The {@link NaviNode} where the mouse event happened.
   * @param y The y coordinate of the mouse event.
   * @param x The x coordinate of the mouse event.
   * @param operand The operand which has been under the cursor in the event.
   */
  private void handleDoRegisterTrackingDown(final NaviNode node, final double y, final double x,
      final COperandTreeNode operand) {
    handleRegisterTracking(node, y, operand, AnalysisDirection.DOWN);
  }

  private void handleRegisterTracking(final NaviNode node, final double y,
      final COperandTreeNode operand, final AnalysisDirection direction) {
    if (!(node.getRawNode() instanceof INaviCodeNode)) {
      return; // register tracking is only possible on code nodes.
    }

    final INaviCodeNode codeNode = (INaviCodeNode) node.getRawNode();
    final double yPos = y - node.getY();
    final int row = node.positionToRow(yPos);
    final INaviInstruction instruction = CCodeNodeHelpers.lineToInstruction(codeNode, row);
    if (instruction == null) {
      return;
    }
    if (!operand.getType().equals(ExpressionType.REGISTER)) {
      return;
    }

    final Set<String> clearedRegisters = Sets.newHashSet();

    if (instruction.getArchitecture().equalsIgnoreCase("x86-32")) {
      clearedRegisters.add("eax");
    } else if (instruction.getArchitecture().equalsIgnoreCase("x86-64")) {
      clearedRegisters.add("rax");
    } else if (instruction.getArchitecture().equalsIgnoreCase("PowerPC-32")) {
      clearedRegisters.addAll(
          Lists.newArrayList("R3", "R4", "R5", "R6", "R7", "R8", "R9", "R10", "R11", "R12"));
    } else if (instruction.getArchitecture().equalsIgnoreCase("ARM-32")) {
      clearedRegisters.addAll(Lists.newArrayList("r0", "r1", "r2", "r3", "r12", "r14"));
    } else if (instruction.getArchitecture().equalsIgnoreCase("MIPS-32")) {
      clearedRegisters.addAll(Lists.newArrayList("$a0",
          "$a1",
          "$a2",
          "$a3",
          "$t0",
          "$t1",
          "$t2",
          "$t3",
          "$t4",
          "$t5",
          "$t6",
          "$t7",
          "$v0",
          "$v1"));
    } else {
      return;
    }

    final boolean trackIncoming = instruction.getOperandPosition(operand.getOperand()) != 0;

    final RegisterTrackingOptions options =
        new RegisterTrackingOptions(false, clearedRegisters, trackIncoming, direction);
    try {
      // TODO(timkornau): comment this code in once we know how to access the bottom panel.
      // final CTrackingResult result =
      CTracking.track(m_model.getGraph().getRawView(), instruction, operand.getValue(), options);
    } catch (final InternalTranslationException exception) {
      CUtilityFunctions.logException(exception);
    }

    // TODO: (timkornau@google) there is currently no way to access the bottom panel to display the
    // results. We need to somehow get access to the register tracking results container which
    // exposes a method to set a new result.

  }

  /**
   * Sets the direction of the register tracker analysis to up and then calls the
   * handleRegisterTracker function.
   *
   * @param node The {@link NaviNode} where the mouse event happened.
   * @param y The y coordinate of the mouse event.
   * @param x The x coordinate of the mouse event.
   * @param operand The operand which has been under the cursor in the event.
   */
  private void handleDoRegisterTrackingUp(final NaviNode node, final double y, final double x,
      final COperandTreeNode operand) {
    handleRegisterTracking(node, y, operand, AnalysisDirection.UP);
  }

  /**
   * Handles clicks on code nodes that should bring up the dialog for editing instruction comments.
   *
   * @param node The clicked node.
   * @param y Y-Coordinate of the mouse click.
   */
  private void handleShowInstructionComment(final NaviNode node, final double y) {
    if (!(node.getRawNode() instanceof INaviCodeNode)) {
      return;
    }

    final INaviCodeNode codeNode = (INaviCodeNode) node.getRawNode();
    final double yPos = y - node.getY();
    final int row = node.positionToRow(yPos);
    final INaviInstruction instruction = CCodeNodeHelpers.lineToInstruction(codeNode, row);
    if (instruction == null) {
      return;
    }

    CGraphDialogs.showInstructionCommentDialog(m_model.getParent(), m_model, codeNode, instruction);
  }

  /**
   * Displays a popup menu for the given node.
   *
   * @param node The node where the user clicked.
   * @param event Mouse-event that was created when the user clicked.
   * @param y The y location where the user clicked.
   * @param clickedObject The clicked object.
   * @param extensions List of objects that extend code node context menus.
   */
  private void showPopupMenu(final NaviNode node, final MouseEvent event,
      final Object clickedObject, final double y, final List<ICodeNodeExtension> extensions) {
    final boolean isActiveNode = m_model.getGraph().getEditMode().getLabelEventHandler()
        .isActiveLabel(node.getRealizer().getNodeContent());

    final JPopupMenu menu = ZyGraphPopupMenus.getPopupMenu(m_model,
        node,
        clickedObject,
        y - node.getY(),
        isActiveNode,
        extensions);

    if (menu != null) {
      menu.show(m_model.getGraph().getView(), event.getX(), event.getY());
    }
  }

  /**
   * Handles clicks on nodes.
   *
   * @param node The clicked node.
   * @param event The click event.
   * @param x The x-coordinate of the click.
   * @param y The y-coordinate of the click.
   * @param extensions List of objects that extend code node context menus.
   */
  public void nodeClicked(final NaviNode node, final MouseEvent event, final double x,
      final double y, final List<ICodeNodeExtension> extensions) {
    if (event.getButton() == MouseEvent.BUTTON3) {
      handleRightClick(node, event, x, y, extensions);
    } else if ((event.getButton() == MouseEvent.BUTTON1) && (event.getClickCount() == 2)
        && event.isControlDown()) {
      final INaviViewNode rawNode = node.getRawNode();

      if (rawNode instanceof INaviFunctionNode) {
        final INaviFunction function = ((INaviFunctionNode) rawNode).getFunction();

        CGraphOpener.showFunction(m_model.getParent(), m_model.getViewContainer(), function);
      } else if (rawNode instanceof INaviCodeNode) {
        final INaviCodeNode cnode = (INaviCodeNode) rawNode;

        final int row = node.positionToRow(y - node.getY());

        final INaviInstruction instruction = CCodeNodeHelpers.lineToInstruction(cnode, row);

        if (instruction == null) {
          return;
        }

        final Set<IAddress> references = new HashSet<IAddress>();

        for (final INaviOperandTree operand : instruction.getOperands()) {
          collectReferences(operand.getRootNode(), references);
        }

        final List<INaviFunction> functions = m_model.getViewContainer().getFunctions();

        for (final INaviFunction function : functions) {
          for (final IAddress address : references) {
            if (function.getAddress().equals(address)) {
              CGraphOpener.showFunction(m_model.getParent(), m_model.getViewContainer(), function);
            }
          }
        }
      }
    } else if (!m_model.getGraph().getEditMode().getLabelEventHandler().isActive()
        && (event.getButton() == MouseEvent.BUTTON1) && (event.getClickCount() == 2)) {
      if ((node.getRawNode() instanceof INaviGroupNode) && event.isShiftDown()) {
        final INaviGroupNode gnode = (INaviGroupNode) node.getRawNode();

        gnode.setCollapsed(!gnode.isCollapsed());
      } else {
        CGraphZoomer.zoomNode(m_model.getGraph(), node);
      }
    }
  }
}
