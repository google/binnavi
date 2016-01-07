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

import com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations.CGraphSelectionExpander;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;


/**
 * Action for expanding down the node selection in a graph.
 */
public final class CActionExpandSelectionDown extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -2766690364796977149L;

  /**
   * The graph whose node selection is expanded down.
   */
  private final ZyGraph m_graph;

  /**
   * Creates a new action object.
   *
   * @param graph The graph whose node selection is expanded down.
   */
  public CActionExpandSelectionDown(final ZyGraph graph) {
    super("Expand Selection Down");

    m_graph = graph;
  }

  @Override
  public void actionPerformed(final ActionEvent Event) {
    CGraphSelectionExpander.expandSelectionDown(m_graph);
  }
}
