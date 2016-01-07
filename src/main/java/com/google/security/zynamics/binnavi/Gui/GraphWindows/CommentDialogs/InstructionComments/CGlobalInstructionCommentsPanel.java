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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.InstructionComments;

import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphModel;
import com.google.security.zynamics.binnavi.disassembly.CCodeNode;
import com.google.security.zynamics.binnavi.disassembly.CommentManager.CommentScope;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.zylib.gui.CodeDisplay.CodeDisplay;

import java.awt.BorderLayout;

import javax.swing.JPanel;

public class CGlobalInstructionCommentsPanel extends JPanel {
  private final InstructionCommentsDataModel commentsModel;
  private final CodeDisplay codeDisplay;

  public CGlobalInstructionCommentsPanel(final INaviCodeNode codeNode,
      final CGraphModel graphModel) {
    super(new BorderLayout());
    setFocusable(true);
    commentsModel =
        new InstructionCommentsDataModel((CCodeNode) codeNode, graphModel, CommentScope.GLOBAL);
    codeDisplay = new CodeDisplay(commentsModel);
    add(codeDisplay);
  }
}
