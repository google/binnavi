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
package com.google.security.zynamics.binnavi.ZyGraph.Menus;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.CActionDeleteNode;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.CEditTextAction;
import com.google.security.zynamics.binnavi.disassembly.CTextNode;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;

/**
 * Context menu shown when the user right-clicks on comment nodes.
 */
public final class CTextNodeMenu extends JPopupMenu {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 7712714629881443414L;

  /**
   * Creates a new text menu.
   *
   * @param parent Parent window used for dialogs.
   * @param view View the clicked node belongs to.
   * @param node The clicked node.
   */
  public CTextNodeMenu(final JFrame parent, final INaviView view, final CTextNode node) {
    Preconditions.checkNotNull(parent, "IE02151: Parent argument can not be null");

    Preconditions.checkNotNull(view, "IE02152: View argument can not be null");

    Preconditions.checkNotNull(node, "IE00975: Node argument can't be null");

    add(new JMenuItem(CActionProxy.proxy(new CEditTextAction(parent, node))));

    addSeparator();

    add(CActionProxy.proxy(new CActionDeleteNode(view, node)));
  }
}
