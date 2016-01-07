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
import javax.swing.JFrame;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.HotKeys;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations.CGraphProximityBrowser;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;


/**
 * Action used to toggle proximity browsing in a graph.
 */
public final class CActionProximityBrowsing extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 2398734393067804429L;

  /**
   * Parent used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * Graph whose proximity browsing is toggled.
   */
  private final ZyGraph m_graph;

  /**
   * Creates a new action object.
   * 
   * @param parent Parent used for dialogs.
   * @param graph Graph whose proximity browsing is toggled.
   */
  public CActionProximityBrowsing(final JFrame parent, final ZyGraph graph) {
    super("Proximity Browsing");
    m_parent = Preconditions.checkNotNull(parent, "IE02826: parent argument can not be null");
    m_graph = Preconditions.checkNotNull(graph, "IE02827: graph argument can not be null");
    putValue(ACCELERATOR_KEY, HotKeys.GRAPH_PROXIMITY_BROWSING_HK.getKeyStroke());
  }

  @Override
  public void actionPerformed(final ActionEvent Event) {
    CGraphProximityBrowser.toggleProximityBrowsing(m_parent, m_graph);
  }
}
