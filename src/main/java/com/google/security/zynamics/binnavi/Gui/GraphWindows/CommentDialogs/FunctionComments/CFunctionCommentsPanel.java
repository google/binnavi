// Copyright 2011-2016 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.FunctionComments;

import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Generic.GenericCommentsTable;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;

import java.awt.GridLayout;

import javax.swing.JPanel;

/**
 * A panel which provides functionality to edit function comments.
 */
public class CFunctionCommentsPanel extends JPanel {
  /**
   * Used for serialization.
   */
  private final GenericCommentsTable globalCommentsTable;
  private final GenericCommentsTable localCommentsTable;

  /**
   * Creates a new panel object.
   *
   */
  public CFunctionCommentsPanel(
      final INaviFunction function, final INaviFunctionNode functionNode) {
    super(new GridLayout(1, 2, 5, 5));

    globalCommentsTable = new GenericCommentsTable(
        new CGlobalFunctionCommentAccessor(function), "Global function comment");
    add(globalCommentsTable);

    if (functionNode != null) {
      localCommentsTable = new GenericCommentsTable(
          new CLocalFunctionNodeCommentAccessor(functionNode), "Local function comment");
      add(localCommentsTable);
    } else {
      localCommentsTable = null;
    }
  }

  /**
   * Focused the editing field of the global comment.
   */
  public void focusGlobalField() {
    globalCommentsTable.requestFocusInWindow();
  }

  /**
   * Focused the editing field of the local comment.
   */
  public void focusLocalField() {
    localCommentsTable.requestFocusInWindow();
  }
}
