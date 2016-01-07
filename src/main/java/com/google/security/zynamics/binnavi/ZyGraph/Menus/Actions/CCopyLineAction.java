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
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.ClipboardCopier;

/**
 * Action class used to copy a line of a graph node to the clipboard.
 */
public final class CCopyLineAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 5636834037607966551L;

  /**
   * Node that provides the line to copy.
   */
  private final NaviNode m_node;

  /**
   * Index of the line to copy.
   */
  private final int m_line;

  /**
   * Creates a new copy line action.
   *
   * @param node Node that provides the line to copy.
   * @param line Index of the line to copy.
   */
  public CCopyLineAction(final NaviNode node, final int line) {
    super("Copy line to clipboard");

    m_node = Preconditions.checkNotNull(node, "IE02153: Node argument can not be null");
    Preconditions.checkPositionIndex(line, node.getRealizer().getNodeContent().getLineCount(),
        "IE02154: Line argument is out of bounds");
    m_line = line;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    ClipboardCopier.copyToClipboard(m_node, m_line);
  }
}
