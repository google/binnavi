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

import javax.swing.JPopupMenu;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphModel;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.CCopySelectionToClipboard;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.CSearchForSelection;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.CSelectNodesWithSelection;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;

/**
 * Menu used for nodes with an active content selection.
 */
public final class CContentSelectionMenu extends JPopupMenu {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 8418472929717673959L;

  /**
   * Creates a new menu object.
   *
   * @param model Graph model of the graph the node belongs to.
   * @param node The node for which the menu is created.
   */
  public CContentSelectionMenu(final CGraphModel model, final NaviNode node) {
    Preconditions.checkNotNull(model, "IE00968: Model argument can not be null");
    Preconditions.checkNotNull(node, "IE00969: Node argument can not be null");

    add(new CCopySelectionToClipboard(node));
    add(new CSearchForSelection(model.getGraphPanel()
        .getToolbar().getSearchPanel().getSearchField(), node));
    add(new CSelectNodesWithSelection(model.getGraph(), node));
  }
}
