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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.CodeNodeComments;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.FunctionComments.CFunctionCommentsPanel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.InitialTab;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.InstructionComments.CGlobalInstructionCommentsPanel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.InstructionComments.LocalInstructionCommentsPanel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.OKButtonPanel;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.zylib.gui.CDialogEscaper;

import java.awt.BorderLayout;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

/**
 * This class is used to let the user edit the comments of a code node. This includes global and
 * local line comments as well as global and local node comments.
 */
public final class DialogEditCodeNodeComment extends JDialog {

  /**
   * Code node that provides node and instruction comments.
   */
  private final INaviCodeNode m_codeNode;

  /**
   * Panel used to display and edit local and global comments.
   */
  private final CodeNodeCommentsPanel m_commentsPanel;

  /**
   * Panel used to display and edit global instruction comments.
   */
  private final CGlobalInstructionCommentsPanel m_globalLineCommentsPanel;

  /**
   * Panel used to display and edit local instruction comments.
   */
  private final LocalInstructionCommentsPanel m_localLineCommentsPanel;

  /**
   * Panel used to display and edit function comments.
   */
  private CFunctionCommentsPanel m_functionCommentsPanel = null;

  /**
   * Creates a new dialog that can be used to edit all code node instructions.
   *
   * @param node The node that provides the node and instruction comments.
   * @param initialTab The initially visible tab.
   */
  public DialogEditCodeNodeComment(
      final CGraphModel model, final INaviCodeNode node, final InitialTab initialTab) {
    super(model.getParent(), "Edit Node Comments", true);

    m_codeNode = Preconditions.checkNotNull(node, "IE01701: Node argument can't be null");

    new CDialogEscaper(this);

    setLayout(new BorderLayout());

    m_commentsPanel = new CodeNodeCommentsPanel(node);
    m_globalLineCommentsPanel = new CGlobalInstructionCommentsPanel(m_codeNode, model);
    m_localLineCommentsPanel = new LocalInstructionCommentsPanel(m_codeNode, model);

    try {
      m_functionCommentsPanel = new CFunctionCommentsPanel(node.getParentFunction(), null);
    } catch (final MaybeNullException exception) {
      CUtilityFunctions.logException(exception);
    }

    createGui(initialTab);
  }

  /**
   * Creates a new dialog that can be used to edit all code node instructions.
   *
   * @param parent The parent of the dialog.
   * @param node The node that provides the node and instruction comments.
   * @param instruction The instruction whose comment has the initial focus.
   */
  public DialogEditCodeNodeComment(final JFrame parent, final CGraphModel graphModel,
      final INaviCodeNode node, final INaviInstruction instruction) {
    super(parent, "Edit Node Comments", true);

    Preconditions.checkNotNull(parent, "IE01702: Parent argument can't be null");
    m_codeNode = Preconditions.checkNotNull(node, "IE01703: Node argument can't be null");

    new CDialogEscaper(this);

    setLayout(new BorderLayout());

    m_commentsPanel = new CodeNodeCommentsPanel(node);
    m_globalLineCommentsPanel = new CGlobalInstructionCommentsPanel(m_codeNode, graphModel);
    m_localLineCommentsPanel = new LocalInstructionCommentsPanel(m_codeNode, graphModel);

    createGui(InitialTab.LocalLineComments);
  }

  /**
   * Creates the GUI of the dialog.
   *
   * @param initialTab
   */
  private void createGui(final InitialTab initialTab) {
    final JTabbedPane tab = new JTabbedPane();

    tab.add("Global Line Comments", m_globalLineCommentsPanel);
    tab.add("Local Line Comments", m_localLineCommentsPanel);
    tab.add("Node Comments", m_commentsPanel);
    tab.add("Function Comments", m_functionCommentsPanel);

    add(tab, BorderLayout.CENTER);
    add(new OKButtonPanel(this), BorderLayout.SOUTH);
    pack();

    switch (initialTab) {
      case GlobalLineComments:
        tab.setSelectedIndex(0);
        break;
      case LocalLineComments:
        tab.setSelectedIndex(1);
        break;
      case LocalNodeComments:
        tab.setSelectedIndex(2);
        m_commentsPanel.focusLocalField();
        break;
      case GlobalNodeComments:
        tab.setSelectedIndex(2);
        m_commentsPanel.focusGlobalField();
        break;
      case FunctionComments:
        tab.setSelectedIndex(3);
        m_functionCommentsPanel.focusGlobalField();
        break;
      default:
        throw new IllegalStateException("IE00681: Unknown initial tab");
    }
  }
}
