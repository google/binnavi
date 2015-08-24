/*
Copyright 2015 Google Inc. All Rights Reserved.

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
package com.google.security.zynamics.binnavi.Gui.Debug.ToolbarPanel.Implementations;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Gui.CProgressDialog;
import com.google.security.zynamics.binnavi.Gui.Debug.TraceOptionsDialog.CTraceOptionsDialog;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.helpers.BreakpointableNodeCounter;
import com.google.security.zynamics.binnavi.debug.helpers.EchoBreakpointCollector;
import com.google.security.zynamics.binnavi.debug.helpers.NodeBreakpointDecider;
import com.google.security.zynamics.binnavi.debug.helpers.NodeBreakpointIterator;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointManager;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceLogger;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.gui.CMessageBox;

import java.util.Set;

import javax.swing.JFrame;

/**
 * Contains the implementations of the trace functions available from the debugger GUI.
 */
public final class CTraceFunctions {
  /**
   * You are not supposed to instantiate this class.
   */
  private CTraceFunctions() {}

  /**
   * Checks arguments for validity.
   *
   * @param parent Parent argument to check.
   * @param debugger Debugger argument to check.
   * @param logger Logger argument to check.
   */
  private static void checkArguments(final JFrame parent, final IDebugger debugger,
      final TraceLogger logger) {
    Preconditions.checkNotNull(parent, "IE01566: Parent argument can not be null");
    Preconditions.checkNotNull(debugger, "IE01567: Debugger argument can not be null");
    Preconditions.checkNotNull(logger, "IE01568: Logger argument can not be null");
  }

  /**
   * Counts the number of echo breakpoints to be set in the graph.
   *
   * @param manager Breakpoint manager that sets the breakpoints.
   * @param graph Graph for which trace mode is activated.
   *
   * @return The number of echo breakpoints to be set in the graph.
   */
  private static int countEchoBreakpoints(final BreakpointManager manager, final ZyGraph graph) {
    final BreakpointableNodeCounter ebc = new BreakpointableNodeCounter(manager);
    NodeBreakpointIterator.iterate(graph, ebc);

    return ebc.getCount();
  }

  /**
   * Creates a new trace.
   *
   * @param parent Parent window used for dialogs.
   * @param logger Records trace events.
   *
   * @return The created trace.
   */
  private static TraceList createTrace(final JFrame parent, final TraceLogger logger) {
    try {
      // Generate a unique name for the new trace.
      final String name = logger.getTraceProvider().generateName();

      return logger.getTraceProvider().createTrace(name, "");
    } catch (final CouldntSaveDataException exception) {
      CUtilityFunctions.logException(exception);

      final String innerMessage = "E00095: " + "Creating the new trace failed";
      final String innerDescription = CUtilityFunctions.createDescription(
          "BinNavi could not create the new trace in the database.",
          new String[] {"There was a problem with the database connection."}, new String[] {
              "The debugger will not enter trace mode. You can try to start "
              + "trace mode again after you have resolved the database connection problem."});

      NaviErrorDialog.show(parent, innerMessage, innerDescription, exception);

      return null;
    }
  }

  /**
   * Saves a trace to the database.
   *
   * @param trace The trace to save.
   */
  private static void saveTrace(final TraceList trace) {
    try {
      trace.save();
    } catch (final CouldntSaveDataException e) {
      CUtilityFunctions.logException(e);
    }
  }

  /**
   * Shows a progress dialog and sets all the echo breakpoints necessary for the next trace.
   *
   * @param parent Parent window used for dialogs.
   * @param addresses List of addresses where echo breakpoints are set.
   * @param logger The trace logger used to log the events.
   * @param maximumHits Maximum number of hits before echo breakpoints are cleared.
   */
  private static void showStartTraceProgressWindow(final JFrame parent,
      final Set<BreakpointAddress> addresses, final TraceLogger logger, final int maximumHits) {
    // Create the new event list where the events of the current trace
    // mode are logged to.
    final TraceList trace = createTrace(parent, logger);

    // Create the object that updates the progress bar
    // on each successfully set echo breakpoint.
    final CStartTraceListener bpl = new CStartTraceListener(logger, trace, addresses, maximumHits);

    // Show the progress dialog.
    CProgressDialog.showEndless(parent, "Setting echo breakpoints", bpl);
  }

  /**
   * Starts trace mode for a given graph.
   *
   * @param parent Parent window used for dialogs.
   * @param debugger Debugger that sets the breakpoints.
   * @param graph Graph for which trace mode is activated.
   * @param logger The trace logger used to log the events.
   */
  public static void startTrace(final JFrame parent, final IDebugger debugger, final ZyGraph graph,
      final TraceLogger logger) {
    checkArguments(parent, debugger, logger);

    Preconditions.checkNotNull(graph, "IE01569: Graph argument can not be null");

    if (!debugger.isConnected()) {
      return;
    }

    if (logger.hasEchoBreakpoints()) {
      final TraceList trace = createTrace(parent, logger);

      if (trace == null) {
        return;
      }

      final TraceList oldTrace = logger.switchTargetList(trace);

      saveTrace(oldTrace);
    } else {
      final int bps = countEchoBreakpoints(debugger.getBreakpointManager(), graph);

      if (bps == 0) {
        CMessageBox.showError(parent,
            "All nodes of the graph are already covered by another active trace");
      } else {
        final CTraceOptionsDialog dlg = CTraceOptionsDialog.show(parent);

        if (!dlg.wasCancelled()) {
          final int maximumHits = dlg.getMaximumHits();

          if (maximumHits == 0) {
            return;
          }

          final EchoBreakpointCollector ebc =
              new EchoBreakpointCollector(debugger.getBreakpointManager());

          graph.iterate(new NodeBreakpointDecider(ebc));

          showStartTraceProgressWindow(parent, ebc.getBreakpoints(), logger, maximumHits);
        }
      }
    }
  }

  /**
   * Stops trace mode.
   *
   * @param parent Parent window used for dialogs.
   * @param debugger Debugger that clears the breakpoints.
   * @param logger Logger used to log the events.
   */
  public static void stopTrace(final JFrame parent, final IDebugger debugger,
      final TraceLogger logger) {
    checkArguments(parent, debugger, logger);

    if (!debugger.isConnected()) {
      return;
    }

    if (!logger.hasEchoBreakpoints()) {
      return;
    }

    // Create the object that updates the progress bar
    // on each successfully set echo breakpoint.
    final CStopTraceListener bpl = new CStopTraceListener(debugger, logger);

    // Show the progress window
    CProgressDialog.showEndless(parent, "Removing echo breakpoints", bpl);
  }
}
