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
import javax.swing.JFrame;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.disassembly.CTextNode;
import com.google.security.zynamics.binnavi.yfileswrap.Gui.GraphWindows.Implementations.CGraphDialogs;

/**
 * Action class used for editing the text of a comment node.
 */
public final class CEditTextAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -1420586158654166872L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * Node whose text is changed.
   */
  private final CTextNode m_node;

  /**
   * Creates a new edit text action.
   * 
   * @param parent Parent window used for dialogs.
   * @param node Node whose text is changed.
   */
  public CEditTextAction(final JFrame parent, final CTextNode node) {
    super("Edit Text");
    m_parent = Preconditions.checkNotNull(parent, "IE02156: Parent argument can not be null");
    m_node = Preconditions.checkNotNull(node, "IE02157: Node argument can not be null");
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CGraphDialogs.showTextNodeCommentDialog(m_parent, m_node);
  }
}
