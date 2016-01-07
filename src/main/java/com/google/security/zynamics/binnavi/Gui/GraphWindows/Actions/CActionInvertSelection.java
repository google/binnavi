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
import javax.swing.Action;
import javax.swing.ImageIcon;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations.CGraphSelecter;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;


/**
 * Action object for inverting the selection of a graph.
 */
public final class CActionInvertSelection extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -5771425011458346048L;

  /**
   * Graph whose node selection is inverted.
   */
  private final ZyGraph m_graph;

  /**
   * Creates a new action object.
   * 
   * @param graph Graph whose node selection is inverted.
   * @param showIcon Flag that says whether the action should have an icon.
   */
  public CActionInvertSelection(final ZyGraph graph, final boolean showIcon) {
    super("Invert selection");
    m_graph = Preconditions.checkNotNull(graph, "IE02823: graph argument can not be null");
    if (showIcon) {
      putValue(SMALL_ICON, new ImageIcon(CMain.class.getResource("data/selinvert_up.jpg")));
    }
    putValue(Action.SHORT_DESCRIPTION, "Invert Selection");
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CGraphSelecter.invertSelection(m_graph);
  }
}
