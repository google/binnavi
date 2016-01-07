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
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations.CGraphLayouter;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;


/**
 * Action used to toggle automatic layouting.
 */
public final class CActionAutomaticLayouting extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 5877390273132344633L;

  /**
   * Graph whose automatic layouting option is toggled.
   */
  private final ZyGraph m_graph;

  /**
   * Creates a new action object.
   * 
   * @param graph Graph whose automatic layouting option is toggled.
   */
  public CActionAutomaticLayouting(final ZyGraph graph) {
    super("Automatic Layouting");
    m_graph = Preconditions.checkNotNull(graph, "IE02809: graph argument can not be null");
    putValue(ACCELERATOR_KEY, HotKeys.GRAPH_AUTOMATIC_LAYOUT_HK.getKeyStroke());
  }

  @Override
  public void actionPerformed(final ActionEvent Event) {
    CGraphLayouter.toggleAutomaticLayouting(m_graph);
  }
}
