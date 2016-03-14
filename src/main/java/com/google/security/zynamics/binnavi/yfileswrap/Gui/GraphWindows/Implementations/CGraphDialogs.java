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
package com.google.security.zynamics.binnavi.yfileswrap.Gui.GraphWindows.Implementations;

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.Gui.GraphSettings.CGraphSettingsDialog;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CViewCommentDialog;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.CodeNodeComments.DialogEditCodeNodeComment;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.GroupNodeComments.DialogEditGroupNodeComment;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.TextNodeComments.DialogTextNodeComment;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations.CGraphLayouter;
import com.google.security.zynamics.binnavi.ZyGraph.LayoutStyle;
import com.google.security.zynamics.binnavi.disassembly.CGroupNode;
import com.google.security.zynamics.binnavi.disassembly.CTextNode;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.gui.GuiHelper;

import y.layout.circular.CircularLayouter;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.layout.orthogonal.OrthogonalLayouter;

import javax.swing.JFrame;

/**
 * Contains helper functions for showing dialogs used in the graph window.
 */
public final class CGraphDialogs {
  /**
   * You are not supposed to instantiate this class.
   */
  private CGraphDialogs() {
  }

  /**
   * Shows the graph settings dialog for a given graph.
   *
   * @param parent Parent window of the dialog.
   * @param graph Graph whose settings are edited in the dialog.
   */
  public static void showGraphSettingsDialog(final JFrame parent, final ZyGraph graph) {
    final CGraphSettingsDialog dlg =
        new CGraphSettingsDialog(parent, "Graph Settings", graph.getSettings(), false, false);

    dlg.setVisible(true);

    if (!dlg.wasCanceled()) {
      if (graph.getSettings().getLayoutSettings().getCurrentLayouter() instanceof CircularLayouter) {
        graph.getSettings().getLayoutSettings().setDefaultGraphLayout(LayoutStyle.CIRCULAR);
      } else if (graph.getSettings().getLayoutSettings().getCurrentLayouter() instanceof IncrementalHierarchicLayouter) {
        graph.getSettings().getLayoutSettings().setDefaultGraphLayout(LayoutStyle.HIERARCHIC);
      } else if (graph.getSettings().getLayoutSettings().getCurrentLayouter() instanceof OrthogonalLayouter) {
        graph.getSettings().getLayoutSettings().setDefaultGraphLayout(LayoutStyle.ORTHOGONAL);
      }

      if (dlg.needsLayouting()) {
        CGraphLayouter.refreshLayout(parent, graph);
      }
    }
  }

  /**
   * Shows a dialog where the comment of a group node can be edited.
   *
   * @param parent Parent window of the dialog.
   * @param node Group node whose comment is edited.
   */
  public static void showGroupNodeCommentDialog(final JFrame parent, final CGroupNode node) {
    final DialogEditGroupNodeComment dlg = new DialogEditGroupNodeComment(parent, node);

    GuiHelper.centerChildToParent(parent, dlg, true);

    dlg.setVisible(true);
  }

  /**
   * Shows a dialog where the comment of an instruction can be edited.
   *
   * @param parent Parent window of the dialog.
   * @param node Code node the instruction belongs to.
   * @param instruction Instruction whose comment is edited.
   */
  public static void showInstructionCommentDialog(final JFrame parent, final CGraphModel model,
      final INaviCodeNode node, final INaviInstruction instruction) {
    final DialogEditCodeNodeComment dlg =
        new DialogEditCodeNodeComment(parent, model, node, instruction);

    GuiHelper.centerChildToParent(parent, dlg, true);

    dlg.setVisible(true);
  }

  /**
   * Shows a dialog where the comment of a text node can be edited.
   *
   * @param parent Parent window of the dialog.
   * @param node Text node whose comment is edited.
   */
  public static void showTextNodeCommentDialog(final JFrame parent, final CTextNode node) {
    final DialogTextNodeComment dlg = new DialogTextNodeComment(parent, node);
    GuiHelper.centerChildToParent(parent, dlg, true);
    dlg.setVisible(true);
  }

  /**
   * Shows the user the dialog where he can modify the description string of the view.
   *
   * @param parent Parent window of the dialog.
   * @param view View whose comment can be edited.
   */
  public static void showViewDescriptionDialog(final JFrame parent, final INaviView view) {
    final CViewCommentDialog dlg =
        new CViewCommentDialog(parent, "Change view description", view.getName(), view
            .getConfiguration().getDescription());

    dlg.setVisible(true);

    if (!dlg.wasCancelled()) {
      try {
        view.getConfiguration().setName(dlg.getName());
        view.getConfiguration().setDescription(dlg.getComment());
      } catch (final Exception e) {
        CUtilityFunctions.logException(e);

        final String innerMessage = "E00114: " + "View description could not be changed";
        final String innerDescription =
            CUtilityFunctions.createDescription(
                String.format("The view description of view '%s' could not be changed.",
                    view.getName()),
                new String[] {"There was a problem with the database connection."},
                new String[] {"The view was not updated and the new view description is lost."});

        NaviErrorDialog.show(parent, innerMessage, innerDescription, e);
      }
    }
  }
}
