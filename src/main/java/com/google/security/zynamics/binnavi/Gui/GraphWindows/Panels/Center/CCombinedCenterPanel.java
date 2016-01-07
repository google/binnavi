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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.Center;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;

import java.awt.BorderLayout;

import javax.swing.JPanel;

/**
 * This class displays a graph view and keeps the displayed graph view updated whenever something
 * relevant happens. Examples of relevant events are changes in the selected debug perspective or
 * debugger events.
 */
public final class CCombinedCenterPanel extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 1322486732498910932L;

  /**
   * Synchronizes the displayed graph view with the underlying graph model.
   */
  private final CGraphSynchronizer m_graphSynchronizer;

  /**
   * Creates a new combined center panel.
   *
   * @param graph The graph to display.
   * @param debugPerspective The debug perspective to be synchronized with the displayed graph.
   */
  public CCombinedCenterPanel(final ZyGraph graph, final CDebugPerspectiveModel debugPerspective) {
    super(new BorderLayout());

    Preconditions.checkNotNull(graph, "IE01223: Graph argument can not be null");
    Preconditions.checkNotNull(
        debugPerspective, "IE01224: Debug perspective argument can not be null");

    add(graph.getViewAsComponent());

    m_graphSynchronizer = new CGraphSynchronizer(graph, debugPerspective);
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    m_graphSynchronizer.dispose();
  }
}
