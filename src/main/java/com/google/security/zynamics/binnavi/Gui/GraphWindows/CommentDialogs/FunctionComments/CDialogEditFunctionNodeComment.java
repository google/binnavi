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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.FunctionComments;

import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.InitialTab;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.OKButtonPanel;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.zylib.gui.CDialogEscaper;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JDialog;

/**
 * Dialog used for editing the comments of a function node.
 */
public final class CDialogEditFunctionNodeComment extends JDialog {

  /**
   * Panel where local and global comments can be edited.
   */
  private final CFunctionCommentsPanel m_commentsPanel;

  /**
   * Creates a new dialog object.
   *
   * @param node Node whose comments are edited.
   * @param initialTab The initially visible tab.
   */
  public CDialogEditFunctionNodeComment(
      final CGraphModel model, final INaviFunctionNode node, final InitialTab initialTab) {
    super(model.getParent(), "Edit Function Node Comments", true);

    new CDialogEscaper(this);
    setLayout(new BorderLayout());
    m_commentsPanel = new CFunctionCommentsPanel(node.getFunction(), node);
    add(m_commentsPanel, BorderLayout.CENTER);
    add(new OKButtonPanel(this), BorderLayout.SOUTH);
    final Dimension dim = getPreferredSize();
    if (dim.height < 300) {
      dim.height = 300;
    }
    dim.width = 600;

    setPreferredSize(dim);
    setMinimumSize(dim);
    pack();

    if (initialTab == InitialTab.GlobalNodeComments) {
      m_commentsPanel.focusGlobalField();
    } else {
      m_commentsPanel.focusLocalField();
    }
  }
}
