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

import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphPanel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.JGraphTab;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations.CGraphCloser;


/**
 * Action for closing all but one of the graph panels of a graph window.
 */
public final class CActionCloseOthers extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -6463184586183395466L;

  /**
   * The tab the graph belongs to.
   */
  private final JGraphTab m_tab;

  /**
   * The graph panel NOT to close.
   */
  private final CGraphPanel m_graphPanel;

  /**
   * Creates a new action object.
   *
   * @param tab The tab the graph belongs to.
   * @param graphPanel The graph panel NOT to close.
   */
  public CActionCloseOthers(final JGraphTab tab, final CGraphPanel graphPanel) {
    super("Close Others");

    m_tab = tab;
    m_graphPanel = graphPanel;
  }

  @Override
  public void actionPerformed(final ActionEvent Event) {
    CGraphCloser.closeOthers(m_tab, m_graphPanel);
  }
}
