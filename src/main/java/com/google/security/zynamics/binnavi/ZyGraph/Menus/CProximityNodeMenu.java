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
import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.CUnhideAndAddToSelectionAction;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.CUnhideAndSelectAction;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.CUnhideChildrenAction;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.CUnhideNodesAction;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.CUnhideParentsAction;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.proximity.ZyProximityNode;

/**
 * Popup menu that is shown when the user right-clicks on an info node in a graph.
 */
public final class CProximityNodeMenu extends JPopupMenu {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 4934615144672701581L;

  /**
   * Creates a new proximity node menu.
   *
   * @param parent Parent window used for dialogs.
   * @param graph Graph the proximity belong to.
   * @param node Clicked proximity node.
   */
  public CProximityNodeMenu(
      final JFrame parent, final ZyGraph graph, final ZyProximityNode<INaviViewNode> node) {
    Preconditions.checkNotNull(parent, "IE02150: Parent argument can not be null");

    Preconditions.checkNotNull(graph, "IE00972: Graph argument can't be null");

    Preconditions.checkNotNull(node, "IE00973: Node argument can't be null");

    add(CActionProxy.proxy(new CUnhideNodesAction(parent, graph, node)));

    addSeparator();

    final JMenuItem unhideParentItem = new JMenuItem(CActionProxy.proxy(
        new CUnhideParentsAction(parent, graph, node)));
    unhideParentItem.setEnabled(!node.isIncoming());
    add(unhideParentItem);

    final JMenuItem unhideChildrenItem = new JMenuItem(CActionProxy.proxy(
        new CUnhideChildrenAction(parent, graph, node)));
    unhideChildrenItem.setEnabled(node.isIncoming());
    add(unhideChildrenItem);

    addSeparator();

    add(CActionProxy.proxy(new CUnhideAndSelectAction(graph, node)));
    add(CActionProxy.proxy(new CUnhideAndAddToSelectionAction(graph, node)));
  }
}
