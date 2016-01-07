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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.HotKeys;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations.CGraphGrouper;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;


/**
 * Action class used to toggle the the expansion state of all selected group nodes of a graph.
 */
public final class CToggleSelectedGroupsAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -2380340724963814907L;

  /**
   * The graph whose group nodes are toggled.
   */
  private final ZyGraph m_graph;

  /**
   * Creates a new action object.
   * 
   * @param graph The graph whose group nodes are toggled.
   */
  public CToggleSelectedGroupsAction(final ZyGraph graph) {
    super("Open/Close Selected Groups");
    m_graph = Preconditions.checkNotNull(graph, "IE02841: graph argument can not be null");
    putValue(ACCELERATOR_KEY, HotKeys.GRAPH_TOGGLE_SELECTED_GROUPS_HK.getKeyStroke());
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CGraphGrouper.toggleSelectedGroups(m_graph);
  }
}
