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
import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.CActionCollapseNode;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.CActionExpandNode;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.CEditGroupCommentAction;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.CRemoveGroupAction;
import com.google.security.zynamics.binnavi.disassembly.CGroupNode;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;

/**
 * Menu shown when the user right-clicks on a group node.
 */
public final class CGroupNodeMenu extends JPopupMenu {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 4778758575194990662L;

  /**
   * Creates a new group node menu.
   * 
   * @param parent Parent window used for dialogs.
   * @param view The view the group node belongs to.
   * @param node The group node that was clicked.
   */
  public CGroupNodeMenu(final JFrame parent, final INaviView view, final CGroupNode node) {
    Preconditions.checkNotNull(parent, "IE02138: Parent argument can not be null");
    Preconditions.checkNotNull(view, "IE02139: View argument can not be null");
    Preconditions.checkNotNull(node, "IE00971: Node argument can't be null");

    add(new JMenuItem(CActionProxy.proxy(new CEditGroupCommentAction(parent, node))));

    addSeparator();

    if (node.isCollapsed()) {
      add(new JMenuItem(CActionProxy.proxy(new CActionExpandNode(node))));
    } else {
      add(new JMenuItem(CActionProxy.proxy(new CActionCollapseNode(node))));
    }

    addSeparator();

    add(new JMenuItem(CActionProxy.proxy(new CRemoveGroupAction(view, node))));
  }
}
