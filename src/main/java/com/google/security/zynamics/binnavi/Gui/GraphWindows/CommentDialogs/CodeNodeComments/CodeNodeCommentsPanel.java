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

import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Generic.GenericCommentsTable;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;

import java.awt.GridLayout;

import javax.swing.JPanel;

public class CodeNodeCommentsPanel extends JPanel {

  private final GenericCommentsTable localCommentTable;
  private final GenericCommentsTable globalCommentTable;

  public CodeNodeCommentsPanel(final INaviCodeNode codeNode) {
    super(new GridLayout(1, 2, 5, 5));

    globalCommentTable = new GenericCommentsTable(new GlobalCodeNodeCommentAccessor(codeNode),
        "Local code node comment");

    localCommentTable = new GenericCommentsTable(new LocalCodeNodeCommentAccessor(codeNode),
        "Global code node comment");

    add(localCommentTable);
    add(globalCommentTable);
  }

  /**
   * Focused the editing field of the global comment.
   * TODO(thomasdullien): Focusing still needs to be sorted & made visible somehow.
   */
  public void focusGlobalField() {
  }

  /**
   * Focused the editing field of the local comment.
   * TODO(thomasdullien): Focusing still needs to be sorted & made visible somehow.
   */
  public void focusLocalField() {
  }
}
