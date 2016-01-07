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
import javax.swing.JPopupMenu;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.CActionDeleteEdge;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.CActionEditEdgeComments;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.CZoomSourceAction;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.CZoomSourceTargetAction;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.CZoomTargetAction;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviEdge;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;

/**
 * Popup menu that is shown when the user right-clicks on an edge of a graph.
 */
public final class CEdgeMenu extends JPopupMenu {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 7019873992260715303L;

  /**
   * Creates a new edge menu object.
   *
   * @param parent Parent window used for dialogs.
   * @param graph Graph the edge belongs to.
   * @param edge The edge for which the menu is created.
   */
  public CEdgeMenu(final JFrame parent, final ZyGraph graph, final NaviEdge edge) {
    Preconditions.checkNotNull(parent, "IE02135: Parent argument can not be null");
    Preconditions.checkNotNull(graph, "IE02136: Graph argument can not be null");
    Preconditions.checkNotNull(edge, "IE02137: Edge argument can not be null");

    add(CActionProxy.proxy(new CZoomSourceTargetAction(graph, edge)));
    add(CActionProxy.proxy(new CZoomSourceAction(graph, edge)));
    add(CActionProxy.proxy(new CZoomTargetAction(graph, edge)));
    addSeparator();
    add(CActionProxy.proxy(new CActionDeleteEdge(graph.getRawView(), edge.getRawEdge())));
    addSeparator();
    add(CActionProxy.proxy(new CActionEditEdgeComments(parent, edge)));
  }
}
