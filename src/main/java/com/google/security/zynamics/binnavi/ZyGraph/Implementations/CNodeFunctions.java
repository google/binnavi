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
package com.google.security.zynamics.binnavi.ZyGraph.Implementations;

import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.InitialTab;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.CodeNodeComments.DialogEditCodeNodeComment;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.FunctionComments.CDialogEditFunctionNodeComment;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.TextNodeComments.DialogTextNodeComment;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.disassembly.CNaviViewEdge;
import com.google.security.zynamics.binnavi.disassembly.CTextNode;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.reil.ReilGraph;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.ReilTranslator;
import com.google.security.zynamics.reil.translators.StandardEnvironment;
import com.google.security.zynamics.zylib.gui.GuiHelper;
import com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType;


/**
 * Contains helper functions related to graph nodes.
 */
public final class CNodeFunctions {
  /**
   * You are not supposed to instantiate this class.
   */
  private CNodeFunctions() {
  }

  /**
   * Creates a comment editing dialog for a given view node.
   * 
   * @param node Node for which the dialog is created.
   * 
   * @return The comment editing dialog for the given node.
   */
  private static JDialog getCommentDialog(final CGraphModel model, final INaviViewNode node,
      final InitialTab initialTab) {
    if (node instanceof INaviCodeNode) {
      return new DialogEditCodeNodeComment(model, (INaviCodeNode) node, initialTab);
    } else if (node instanceof INaviFunctionNode) {
      return new CDialogEditFunctionNodeComment(model, (INaviFunctionNode) node, initialTab);
    } else {
      throw new IllegalStateException("IE02127: Unknown node type");
    }
  }

  /**
   * Transfers the comments from the old node to the two new nodes after node splitting.
   * 
   * @param node The old node.
   * @param newNode1 The upper new node.
   * @param newNode2 The lower new node.
   */
  private static void transferLocalCodeNodeComments(final INaviCodeNode node,
      final INaviCodeNode newNode1, final INaviCodeNode newNode2) {
    newNode1.getComments().initializeLocalCodeNodeComment(
        node.getComments().getLocalCodeNodeComment());

    for (final INaviInstruction naviInstruction : node.getInstructions()) {
      if (newNode1.hasInstruction(naviInstruction)) {
        newNode1.getComments().initializeLocalInstructionComment(naviInstruction,
            node.getComments().getLocalInstructionComment(naviInstruction));
      } else {
        newNode2.getComments().initializeLocalInstructionComment(naviInstruction,
            node.getComments().getLocalInstructionComment(naviInstruction));
      }
    }
  }

  /**
   * Copy REIL code for node
   * 
   * @param parent Parent used for dialogs
   * @param node The node for which to generate REIL code
   * @return The corresponding REIL graph
   */
  public static ReilGraph copyReilCode(final Window parent, final INaviCodeNode node) {
    final ReilTranslator<INaviInstruction> translator = new ReilTranslator<INaviInstruction>();

    try {
      return translator.translate(new StandardEnvironment(), node);
    } catch (final InternalTranslationException e) {
      CUtilityFunctions.logException(e);

      final String message = "E000XXX: " + "Could not show REIL code for node";
      final String description =
          CUtilityFunctions.createDescription(
              String.format("BinNavi could not show the REIL code for basic block at '%X'.",
                  node.getAddress()),
              new String[] {"The instructions could not be converted to REIL code."},
              new String[] {"You can not fix this problem yourself. Please contact "
                  + "the BinNavi support."});

      NaviErrorDialog.show(parent, message, description, e);
    }
    return null;
  }

  /**
   * Attaches a comment node to a given view node.
   * 
   * @param parent Parent used for dialogs.
   * @param view The view where the new comment node is created.
   * @param node The node the new comment node is attached to.
   */
  public static void createCommentNode(final JFrame parent, final INaviView view,
      final INaviViewNode node) {

    Preconditions.checkNotNull(parent, "IE02128: Parent argument can not be null");
    Preconditions.checkNotNull(view, "IE02129: View argument can not be null");
    Preconditions.checkNotNull(node, "IE01726: Node argument can not be null");

    // TODO (timkornau): this is just transposed from the old code
    // needs to be checked to if we still want this to be like this.

    final CTextNode source = view.getContent().createTextNode(null);
    final CNaviViewEdge edge = view.getContent().createEdge(source, node, EdgeType.TEXTNODE_EDGE);

    final DialogTextNodeComment dlg = new DialogTextNodeComment(parent, source);

    GuiHelper.centerChildToParent(parent, dlg, true);

    dlg.setVisible(true);

    final List<IComment> newComment = dlg.getComment();

    if (newComment == null) {
      view.getContent().deleteEdge(edge);
      view.getContent().deleteNode(source);
    }
  }

  /**
   * Shows a dialog to edit the comments of a node.
   * 
   * @param node The node whose comments are edited.
   * @param initialTab The initially visible tab.
   */
  public static void editNodeComments(final CGraphModel model, final INaviViewNode node,
      final InitialTab initialTab) {
    Preconditions.checkNotNull(node, "IE02131: Node argument can not be null");

    final JDialog dialog = getCommentDialog(model, node, initialTab);

    GuiHelper.centerChildToParent(model.getParent(), dialog, true);

    dialog.setVisible(true);
  }

  /**
   * Splits a node.
   * 
   * @param view View the node belongs to.
   * @param originalNode Node to split.
   * @param instruction Instruction after which the node is split.
   */
  public static void splitAfter(final INaviView view, final INaviCodeNode originalNode,
      final INaviInstruction instruction) {
    final Iterable<INaviInstruction> oldInstructions = originalNode.getInstructions();

    if (instruction == Iterables.getLast(oldInstructions)) {
      // Splitting after the last instruction of a node does not make
      // sense at all.

      return;
    }

    // Step I: Find out what instructions belong to the new upper block and what
    // instructions belong to the new lower block.

    final List<INaviInstruction> upperInstructions = new ArrayList<INaviInstruction>();
    final List<INaviInstruction> lowerInstructions = new ArrayList<INaviInstruction>();

    List<INaviInstruction> currentInstructions = upperInstructions;

    for (final INaviInstruction oldInstruction : oldInstructions) {
      currentInstructions.add(oldInstruction);

      if (oldInstruction == instruction) {
        currentInstructions = lowerInstructions;
      }
    }

    // Step II: Create the two new code nodes.

    INaviFunction parentFunction = null;

    try {
      parentFunction = originalNode.getParentFunction();
    } catch (final MaybeNullException e) {
      // No parent function
    }

    final INaviCodeNode newNode1 =
        view.getContent().createCodeNode(parentFunction, upperInstructions);
    final INaviCodeNode newNode2 =
        view.getContent().createCodeNode(parentFunction, lowerInstructions);

    newNode1.setColor(originalNode.getColor());
    newNode1.setBorderColor(originalNode.getBorderColor());
    newNode2.setColor(originalNode.getColor());

    // Step III: Transfer node comments and instruction comments from the old node
    // to the new nodes.

    transferLocalCodeNodeComments(originalNode, newNode1, newNode2);

    // Step IV: Connect the two new nodes.

    view.getContent().createEdge(newNode1, newNode2, EdgeType.JUMP_UNCONDITIONAL);

    // Step V: Recreate the incoming and outgoing edges of the old node.

    for (final INaviEdge incomingEdge : originalNode.getIncomingEdges()) {
      view.getContent().createEdge(incomingEdge.getSource(), newNode1, incomingEdge.getType());
    }

    for (final INaviEdge outgoingEdge : originalNode.getOutgoingEdges()) {
      view.getContent().createEdge(newNode2, outgoingEdge.getTarget(), outgoingEdge.getType());
    }

    // Step VI: Get rid of the old node.

    view.getContent().deleteNode(originalNode);
  }
}
