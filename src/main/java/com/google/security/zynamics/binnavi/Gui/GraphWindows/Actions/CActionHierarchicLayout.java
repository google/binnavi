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
import javax.swing.JFrame;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations.CGraphLayouter;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;


/**
 * Action class for layouting a graph in a hierarchic way.
 */
public final class CActionHierarchicLayout extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 8989183936943233084L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * Graph to be layouted.
   */
  private final ZyGraph m_graph;

  /**
   * Creates a new action object.
   * 
   * @param parent Parent window used for dialogs.
   * @param graph Graph to be layouted.
   */
  public CActionHierarchicLayout(final JFrame parent, final ZyGraph graph) {
    super("Hierarchical Layout");

    m_parent = Preconditions.checkNotNull(parent, "IE02821: parent argument can not be null");
    m_graph = Preconditions.checkNotNull(graph, "IE02822: graph argument can not be null");

    putValue(Action.SMALL_ICON, new ImageIcon(CMain.class.getResource("data/layhier_up.jpg")));
    putValue(Action.SHORT_DESCRIPTION, "Hierarchical Layout");
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CGraphLayouter.doHierarchicLayout(m_parent, m_graph);
  }
}
