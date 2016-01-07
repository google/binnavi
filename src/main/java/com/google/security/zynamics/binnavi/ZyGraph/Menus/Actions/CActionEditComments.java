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
package com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.InitialTab;
import com.google.security.zynamics.binnavi.ZyGraph.Implementations.CNodeFunctions;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;


/**
 * Action class that is used for editing the comments of a node.
 */
public final class CActionEditComments extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -4080151583826789327L;

  private final CGraphModel m_model;

  /**
   * The node whose comments are edited.
   */
  private final INaviViewNode m_node;

  /**
   * Creates a new action object.
   * 
   * @param node The node whose comments are edited.
   */
  public CActionEditComments(final CGraphModel model, final INaviViewNode node) {
    super("Edit Comments");
    m_model = Preconditions.checkNotNull(model, "Error: model argument can not be null");
    m_node = Preconditions.checkNotNull(node, "Error: node argument can not be null");
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CNodeFunctions.editNodeComments(m_model, m_node, InitialTab.GlobalLineComments);
  }
}
