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
package com.google.security.zynamics.binnavi.Gui.Debug.BreakpointTable.Implementations;

import java.awt.Window;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.Debug.BreakpointTable.CBreakpointTableHelpers;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphWindow;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.IGraphPanel;
import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CViewSearcher;
import com.google.security.zynamics.binnavi.Gui.WindowManager.CWindowManager;
import com.google.security.zynamics.binnavi.ZyGraph.Implementations.ZyZoomHelpers;
import com.google.security.zynamics.binnavi.debug.debugger.BackEndDebuggerProvider;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointManager;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointStatus;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointType;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.Pair;

/**
 * Contains the implementations of the actions available from the breakpoint toolbar.
 */
public final class CBreakpointFunctions {
  /**
   * You are not supposed to instantiate this class.
   */
  private CBreakpointFunctions() {
  }

  /**
   * Checks whether any of the input arguments is null and throws an exception if it is.
   * 
   * @param debuggerProvider The debugger provider object to check for null.
   */
  private static void checkArguments(final BackEndDebuggerProvider debuggerProvider) {
    Preconditions.checkNotNull(debuggerProvider,
        "IE02235: Debugger provider argument can not be null");
  }

  /**
   * Checks whether any of the input arguments is null and throws an exception if it is.
   * 
   * @param debuggerProvider The debugger provider object to check for null.
   * @param rows The rows argument to check.
   */
  private static void checkArguments(final BackEndDebuggerProvider debuggerProvider,
      final int[] rows) {
    checkArguments(debuggerProvider);
    Preconditions.checkNotNull(rows, "IE02250: Rows argument can't be null");
  }

  /**
   * Tests whether all breakpoints set in the specified rows are disabled.
   * 
   * @param debuggerProvider Provides the debuggers where breakpoints can be set.
   * @param rows Rows that identify the breakpoints.
   * 
   * @return True, if all selected breakpoints are disabled. False, otherwise.
   */
  public static boolean allDisabled(final BackEndDebuggerProvider debuggerProvider,
      final int[] rows) {
    checkArguments(debuggerProvider, rows);

    for (final int row : rows) {
      final Pair<IDebugger, Integer> breakpoint =
          CBreakpointTableHelpers.findBreakpoint(debuggerProvider, row);

      final BreakpointManager manager = breakpoint.first().getBreakpointManager();
      final int breakpointIndex = breakpoint.second();

      if (manager.getBreakpointStatus(BreakpointType.REGULAR, breakpointIndex) != BreakpointStatus.BREAKPOINT_DISABLED) {
        return false;
      }
    }

    return true;
  }

  /**
   * Tests whether not all breakpoints set in the specified rows are disabled.
   * 
   * @param debuggerProvider Provides the debuggers where breakpoints can be set.
   * @param rows Rows that identify the breakpoints.
   * 
   * @return True, if not all selected breakpoints are disabled. False, otherwise.
   */
  public static boolean allNotDisabled(final BackEndDebuggerProvider debuggerProvider,
      final int[] rows) {
    checkArguments(debuggerProvider, rows);

    for (final int row : rows) {
      final Pair<IDebugger, Integer> breakpoint =
          CBreakpointTableHelpers.findBreakpoint(debuggerProvider, row);

      final BreakpointManager manager = breakpoint.first().getBreakpointManager();
      final int breakpointIndex = breakpoint.second();

      if (manager.getBreakpointStatus(BreakpointType.REGULAR, breakpointIndex) == BreakpointStatus.BREAKPOINT_DISABLED) {
        return false;
      }
    }

    return true;
  }

  /**
   * Zooms to a given breakpoint.
   * 
   * @param parent Parent window for dialogs.
   * @param graph Graph shown in the window of the breakpoint table.
   * @param container View container of the graph.
   * @param address Address to zoom to.
   */
  public static void zoomToBreakpoint(final Window parent, final ZyGraph graph,
      final IViewContainer container, final BreakpointAddress address) {
    final IAddress addr = address.getAddress().getAddress();

    if (!ZyZoomHelpers.zoomToAddress(graph, addr, address.getModule(), true)) {
      for (final CGraphWindow window : CWindowManager.instance()) {
        for (final IGraphPanel graphPanel : window) {
          if (ZyZoomHelpers.zoomToAddress(graphPanel.getModel().getGraph(), addr,
              address.getModule(), true)) {
            window.activate(graphPanel);
            window.toFront();

            return;
          }
        }
      }

      CViewSearcher.searchView(parent, container, addr);
    }
  }
}
