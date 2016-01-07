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
import com.google.security.zynamics.zylib.general.ClipboardHelpers;

/**
 * Action class that can be used to copy a node content selection to the clipboard.
 */
public final class CCopySelectionToClipboard extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -6253817586261827319L;

  /**
   * The node that provides the selection.
   */
  private final NaviNode m_node;

  /**
   * Creates a new action object.
   *
   * @param node The node that provides the selection.
   */
  public CCopySelectionToClipboard(final NaviNode node) {
    super("Copy selection to clipboard");

    m_node = Preconditions.checkNotNull(node, "IE00938: Node argument can not be null");
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    final String selectedText = m_node.getRealizer().getNodeContent().getSelectedText();

    ClipboardHelpers.copyToClipboard(selectedText);
  }
}
