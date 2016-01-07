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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.TextNodeComments;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.OKButtonPanel;
import com.google.security.zynamics.binnavi.disassembly.INaviTextNode;
import com.google.security.zynamics.zylib.gui.CDialogEscaper;

import java.awt.BorderLayout;
import java.awt.Window;
import java.util.List;

import javax.swing.JDialog;

/**
 * Dialog that is used for modifying the content of a text node.
 */
public final class DialogTextNodeComment extends JDialog {

  private final TextNodeCommentsPanel m_commentsPanel;

  /**
   * Creates a new dialog object.
   *
   * @param owner The owner window of the dialog.
   * @param textNode The initial text content shown in the dialog.
   */
  public DialogTextNodeComment(final Window owner, final INaviTextNode textNode) {
    super(owner, "Text Node Comments", ModalityType.APPLICATION_MODAL);
    Preconditions.checkNotNull(owner, "IE02711: owner argument can not be null");
    Preconditions.checkNotNull(textNode, "IE02712: textNode argument can not be null");
    new CDialogEscaper(this);
    setLayout(new BorderLayout());
    m_commentsPanel = new TextNodeCommentsPanel(textNode);
    createGui();
  }

  private void createGui() {
    add(m_commentsPanel, BorderLayout.CENTER);
    add(new OKButtonPanel(this), BorderLayout.SOUTH);
    pack();
  }

  public List<IComment> getComment() {
    return m_commentsPanel.getComment();
  }
}
