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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.GroupNodeComments;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.OKButtonPanel;
import com.google.security.zynamics.binnavi.disassembly.CGroupNode;
import com.google.security.zynamics.zylib.gui.CDialogEscaper;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;

public class DialogEditGroupNodeComment extends JDialog {

  private final CGroupNode groupNode;
  private final GroupNodesCommentsPanel commentsPanel;

  public DialogEditGroupNodeComment(final JFrame parent, final CGroupNode node) {
    super(parent, "Edit Group Node Comments", true);
    groupNode = Preconditions.checkNotNull(node, "IE02679: node argument can not be null");
    new CDialogEscaper(this);
    setLayout(new BorderLayout());
    commentsPanel = new GroupNodesCommentsPanel(groupNode);
    createGui();
  }

  private void createGui() {
    add(commentsPanel, BorderLayout.CENTER);
    add(new OKButtonPanel(this), BorderLayout.SOUTH);
    pack();
  }

  public List<IComment> getComment() {
    return commentsPanel.getComment();
  }
}
