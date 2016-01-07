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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Generic;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.ICommentAccessor;
import com.google.security.zynamics.zylib.gui.CodeDisplay.CodeDisplay;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JPanel;

/**
 * The panel for "generic" comments (per-node or per-edge, both local and global).
 */
public class GenericCommentsTable extends JPanel {
  private final GenericCommentsTableModel commentsModel;
  private final CodeDisplay codeDisplay;
  private final String title;

  public GenericCommentsTable(final ICommentAccessor accessor, String title) {
    super(new BorderLayout());
    Preconditions.checkNotNull(accessor, "Error: argument 'accessor' can not be null");
    Preconditions.checkNotNull(title, "Error: argument 'title' can not be null");

    setFocusable(true);
    commentsModel = new GenericCommentsTableModel(accessor);
    codeDisplay = new CodeDisplay(commentsModel);
    this.title = title;
    add(codeDisplay);
  }

  public List<IComment> getComment() {
    return commentsModel.getComments();
  }
}
