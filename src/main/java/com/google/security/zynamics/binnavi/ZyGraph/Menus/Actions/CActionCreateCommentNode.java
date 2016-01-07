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
import com.google.security.zynamics.binnavi.ZyGraph.Implementations.CNodeFunctions;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;

/**
 * Action class used for creating new text nodes.
 */
public final class CActionCreateCommentNode extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 5312452568032434177L;

  /**
   * Parent used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * View where the comment node is created.
   */
  private final INaviView m_view;

  /**
   * Node the comment node is attached to.
   */
  private final INaviViewNode m_node;

  /**
   * Creates a new action object.
   *
   * @param parent Parent used for dialogs.
   * @param view View where the comment node is created.
   * @param node Node the comment node is attached to.
   */
  public CActionCreateCommentNode(
      final JFrame parent, final INaviView view, final INaviViewNode node) {
    super("Create Comment Node");

    m_node = Preconditions.checkNotNull(node, "IE00923: Node argument can not be null");
    m_parent = Preconditions.checkNotNull(parent, "IE02363: Parent argument can not be null");
    m_view = Preconditions.checkNotNull(view, "IE02364: View argument can not be null");
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CNodeFunctions.createCommentNode(m_parent, m_view, m_node);
  }
}
