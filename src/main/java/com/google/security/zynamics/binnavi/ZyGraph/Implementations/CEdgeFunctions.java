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
package com.google.security.zynamics.binnavi.ZyGraph.Implementations;

import javax.swing.JFrame;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.EdgeComments.CDialogEditEdgeComment;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviEdge;
import com.google.security.zynamics.zylib.gui.GuiHelper;

/**
 * Helper class that contains functions for working with edges.
 */
public final class CEdgeFunctions {
  /**
   * You are not supposed to instantiate this class.
   */
  private CEdgeFunctions() {
  }

  /**
   * Shows the dialog for editing edge comments.
   *
   * @param parent Parent window used for dialogs.
   * @param edge The edge whose comments are edited.
   */
  public static void editEdgeComments(final JFrame parent, final NaviEdge edge) {
    Preconditions.checkNotNull(parent, "IE02115: Parent argument can not be null");
    Preconditions.checkNotNull(edge, "IE02116: Edge argument can not be null");

    final CDialogEditEdgeComment dlg = new CDialogEditEdgeComment(parent, edge.getRawEdge());

    GuiHelper.centerChildToParent(parent, dlg, true);

    dlg.setVisible(true);
  }
}
