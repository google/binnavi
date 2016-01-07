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
package com.google.security.zynamics.binnavi.Gui.Debug.Notifier;

import java.util.ArrayList;
import java.util.List;

import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphWindow;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.IGraphContainerWindow;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.IGraphPanel;
import com.google.security.zynamics.binnavi.Gui.WindowManager.CWindowManager;
import com.google.security.zynamics.binnavi.debug.debugger.BackEndDebuggerProvider;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.views.CViewHelpers;


/**
 * Contains code for finding panels that contain instructions with certain addresses.
 */
public final class CPanelFinder {
  /**
   * You are not supposed to instantiate this class.
   */
  private CPanelFinder() {
  }

  /**
   * Returns all graph panels that show a graph that contains a given address.
   *
   * @param debugger The active debugger.
   * @param address The address to search for.
   *
   * @return A list of graph panels that contains the address.
   */
  private static List<IGraphPanel> collectPanelsWithAddress(
      final IDebugger debugger, final UnrelocatedAddress address) {
    final List<IGraphPanel> panels = new ArrayList<IGraphPanel>();

    for (final IGraphContainerWindow window : CWindowManager.instance().getOpenWindows()) {
      for (final IGraphPanel graphPanel : window) {
        final BackEndDebuggerProvider debuggerProvider =
            graphPanel.getModel().getDebuggerProvider();

        for (final IDebugger d : debuggerProvider) {
          if (d == debugger && CViewHelpers.containsAddress(
              graphPanel.getModel().getGraph().getRawView(), address)) {
            panels.add(graphPanel);
          }
        }
      }
    }

    return panels;
  }

  /**
   * Returns the graph panel that contains a given address.
   *
   * @param debugger The active debugger.
   * @param address The address to search for.
   *
   * @return The graph panel that contains the address or null if there is no such graph panel.
   */
  public static IGraphPanel getPanelWithAddress(
      final IDebugger debugger, final UnrelocatedAddress address) {
    final List<IGraphPanel> panels = collectPanelsWithAddress(debugger, address);

    if (panels.isEmpty()) {
      return null;
    } else {
      final List<IGraphPanel> activeWindowPanels = new ArrayList<IGraphPanel>();
      final List<IGraphPanel> inactiveWindowPanels = new ArrayList<IGraphPanel>();

      for (final IGraphPanel panel : panels) {
        final CGraphWindow parent = panel.getModel().getParent();

        if (parent.isActive() && parent.isActiveGraph(panel)) {
          return panel;
        } else if (parent.isActive()) {
          activeWindowPanels.add(panel);
        } else {
          inactiveWindowPanels.add(panel);
        }
      }

      if (activeWindowPanels.isEmpty()) {
        // Arbitrarily choose a panel from a non-active window
        return inactiveWindowPanels.get(0);
      } else {
        // Arbitrarily choose a panel from the active window
        return activeWindowPanels.get(0);
      }
    }
  }

}
