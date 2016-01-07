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

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.OKButtonPanel;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.zylib.gui.CDialogEscaper;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.JFrame;

/**
 * Dialog used for editing the comments of an edge.
 */
public final class CDialogEditEdgeComment extends JDialog {

  /**
   * Creates a new dialog object.
   *
   * @param parent Parent window of the dialog.
   * @param edge The edge whose comments are edited.
   */
  public CDialogEditEdgeComment(final JFrame parent, final INaviEdge edge) {
    super(parent, "Edit Edge Comments", true);

    Preconditions.checkNotNull(edge, "IE02386: edge argument can not be null");
    new CDialogEscaper(this);
    setLayout(new BorderLayout());
    add(new CEdgeCommentsPanel(edge), BorderLayout.CENTER);
    add(new OKButtonPanel(this), BorderLayout.SOUTH);
    final Dimension dim = getPreferredSize();
    if (dim.height < 300) {
      dim.height = 300;
    }
    dim.width = 600;

    setPreferredSize(dim);
    setMinimumSize(dim);
    pack();
  }
}
