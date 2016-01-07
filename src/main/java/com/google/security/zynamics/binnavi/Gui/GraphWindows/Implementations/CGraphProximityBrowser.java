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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations;

import javax.swing.JFrame;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.CProgressDialog;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.CEndlessHelperThread;

/**
 * Contains functions to working with the proximity browser of a graph.
 */
public final class CGraphProximityBrowser {
  /**
   * You are not supposed to instantiate this class.
   */
  private CGraphProximityBrowser() {
  }

  /**
   * Toggles the state of proximity browsing in the graph.
   *
   * @param parent Parent window used to display dialogs.
   * @param graph The graph where proximity browsing is switched on or off.
   */
  public static void toggleProximityBrowsing(final JFrame parent, final ZyGraph graph) {
    Preconditions.checkNotNull(parent, "IE01750: Parent argument can not be null");

    Preconditions.checkNotNull(graph, "IE01751: Graph argument can not be null");

    if (graph.getSettings().getProximitySettings().getProximityBrowsing()) {
      CProgressDialog.showEndless(
          parent, "Switching off proximity browsing", new ProximityWaiter(graph));
    } else {
      CProgressDialog.showEndless(
          parent, "Switching on proximity browsing", new ProximityWaiter(graph));
    }
  }

  /**
   * Class to display a progress dialog while proximity browsing is turned on or off.
   */
  private static class ProximityWaiter extends CEndlessHelperThread {
    /**
     * Graph whose proximity browsing is toggled.
     */
    private final ZyGraph m_graph;

    /**
     * Creates a new thread object.
     *
     * @param graph Graph whose proximity browsing is toggled.
     */
    private ProximityWaiter(final ZyGraph graph) {
      m_graph = graph;
    }

    @Override
    public void runExpensiveCommand() {
      m_graph.getSettings().getProximitySettings().setProximityBrowsing(
          !m_graph.getSettings().getProximitySettings().getProximityBrowsing());
    }
  }
}
