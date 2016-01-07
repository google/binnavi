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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.EdgeComments;

import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Generic.GenericCommentsTable;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;

import java.awt.GridLayout;

import javax.swing.JPanel;

public class CEdgeCommentsPanel extends JPanel {

  public CEdgeCommentsPanel(final INaviEdge edge) {
    super(new GridLayout(1, 2, 5, 5));

    final GenericCommentsTable globalCommentsTable =
        new GenericCommentsTable(new CGlobalEdgeCommentAccessor(edge), "Global edge comment");

    final GenericCommentsTable localCommentsTable =
        new GenericCommentsTable(new CLocalEdgeCommentAccessor(edge), "Local edge comment");

    add(globalCommentsTable);
    add(localCommentsTable);
  }
}
