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
package com.google.security.zynamics.binnavi.Gui.Debug.EventLists.Implementations;

import java.awt.Window;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.Implementations.CTaggingFunctions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;
import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CNameListGenerators;
import com.google.security.zynamics.binnavi.Gui.Progress.CDefaultProgressOperation;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointType;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;
import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceEvent;
import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceListProvider;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.gui.CMessageBox;

/**
 * Contains helper functions for working with traces.
 */
public final class CTraceFunctions {
  /**
   * You are not supposed to instantiate this class.
   */
  private CTraceFunctions() {
  }

  /**
   * Deletes a number of traces from a given event list provider.
   *
   * @param parent Parent window used for dialogs.
   * @param listProvider The event list provider that manages the event lists.
   * @param traces Indices of the traces to be deleted.
   */
  public static void deleteTrace(
      final Window parent, final ITraceListProvider listProvider, final int[] traces) {
    Preconditions.checkNotNull(parent, "IE01381: Parent argument can not be null");
    Preconditions.checkNotNull(listProvider, "IE01382: List provider argument can't be null");
    Preconditions.checkNotNull(traces, "IE01383: Traces argument can't be null");

    // At first we get the trace list objects that correspond
    // to the indices passed in the traces parameter.
    //
    // We can not delete the event lists directly or we'd have to keep
    // track of changing indices because deleting event lists from
    // the manager changes the internal event list indices.

    final List<TraceList> traceObjects = new ArrayList<TraceList>();

    for (final int trace : traces) {
      traceObjects.add(listProvider.getList(trace));
    }

    if (CMessageBox.showYesNoQuestion(parent, String.format(
        "Do you really want to delete the following traces from the database?\n\n%s",
        CNameListGenerators.getNameList(traceObjects))) == JOptionPane.YES_OPTION) {
      for (final TraceList trace : traceObjects) {
        new Thread() {
          @Override
          public void run() {
            final CDefaultProgressOperation operation =
                new CDefaultProgressOperation("", true, false);
            operation.getProgressPanel().setMaximum(1);

            operation.getProgressPanel().setText("Deleting trace" + ": " + trace.getName());

            try {
              listProvider.removeList(trace);

              operation.getProgressPanel().next();
            } catch (final CouldntDeleteException e) {
              CUtilityFunctions.logException(e);

              final String innerMessage = "E00075: " + "Could not delete trace list";
              final String innerDescription = CUtilityFunctions.createDescription(String.format(
                  "The trace list '%s' could not be deleted.", trace.getName()),
                  new String[] {
                      "There was a problem with the database connection."}, new String[] {
                      "The trace list was not deleted. You could try to delete the list again once the connection problem was resolved."});

              NaviErrorDialog.show(parent, innerMessage, innerDescription, e);
            } finally {
              operation.stop();
            }
          }
        }.start();
      }
    }
  }

  /**
   * Takes the current graph and selects all nodes of the graph where trace events of the given
   * event list occurred.
   *
   * @param graph The graph where the trace list is shown.
   * @param list The event list that is selected in the graph.
   */
  public static void selectList(final ZyGraph graph, final TraceList list) {
    Preconditions.checkNotNull(graph, "IE01384: Graph argument can not be null");
    Preconditions.checkNotNull(list, "IE01385: List argument can not be null");

    graph.selectNodes(CTraceNodeFinder.getTraceNodes(graph, list), true);
  }

  /**
   * Sets breakpoints on trace events.
   *
   * @param model Provides the active debugger.
   * @param events Events on which breakpoints are set.
   */
  public static void setBreakpoints(
      final CDebugPerspectiveModel model, final List<ITraceEvent> events) {
    final IDebugger debugger = model.getCurrentSelectedDebugger();

    if (debugger == null) {
      return;
    }

    final List<INaviModule> modules = debugger.getModules();
    final Set<BreakpointAddress> addresses = new HashSet<BreakpointAddress>();

    for (final ITraceEvent event : events) {
      final BreakpointAddress address = event.getOffset();

      if ((address.getModule() == null) || (modules.contains(address.getModule())
          && !debugger.getBreakpointManager().hasBreakpoint(BreakpointType.REGULAR, address))) {
        addresses.add(address);
      }
    }
    debugger.getBreakpointManager().addBreakpoints(BreakpointType.REGULAR, addresses);
  }

  /**
   * Changes the description of a trace list.
   *
   * @param parent Parent window used for dialogs.
   * @param eventList The trace list in question.
   * @param description The new description of the trace list.
   */
  public static void setTraceDescription(
      final JFrame parent, final TraceList eventList, final String description) {
    Preconditions.checkNotNull(eventList, "IE01387: Event list argument can't be null");
    Preconditions.checkNotNull(description, "IE01388: Description argument can't be null");

    try {
      eventList.setDescription(description);
    } catch (final CouldntSaveDataException e) {
      CUtilityFunctions.logException(e);

      final String innerMessage = "E00076: " + "Could not change trace description";
      final String innerDescription = CUtilityFunctions.createDescription(String.format(
          "The description of the trace list '%s' could not be changed.", eventList.getName()),
          new String[] {"There was a problem with the database connection."},
          new String[] {
              "The trace list keeps its old description. You could try changing the description again once the connection problem was resolved."});

      NaviErrorDialog.show(parent, innerMessage, innerDescription, e);
    }
  }

  /**
   * Changes the name of a trace list.
   *
   * @param parent Parent window used for dialogs.
   * @param eventList The trace list in question.
   * @param name The new name of the trace list.
   */
  public static void setTraceName(
      final JFrame parent, final TraceList eventList, final String name) {
    Preconditions.checkNotNull(eventList, "IE01390: Event list argument can't be null");
    Preconditions.checkNotNull(name, "IE01391: Name argument can't be null");

    try {
      eventList.setName(name);
    } catch (final CouldntSaveDataException e) {
      CUtilityFunctions.logException(e);

      final String innerMessage = "E00077: " + "Could not change trace name";
      final String innerDescription = CUtilityFunctions.createDescription(String.format(
          "The name of the trace list '%s' could not be changed.", eventList.getName()),
          new String[] {"There was a problem with the database connection."}, new String[] {
              "The trace list keeps its old name. You could try changing the name again once the connection problem was resolved."});

      NaviErrorDialog.show(parent, innerMessage, innerDescription, e);
    }
  }

  /**
   * Tags all nodes hit by an event list with a given tag.
   *
   * @param parent Parent window used for dialogs.
   * @param graph Graph whose nodes are tagged.
   * @param list List that provides the events.
   * @param tag Tag the nodes are tagged with.
   */
  public static void tagList(
      final JFrame parent, final ZyGraph graph, final TraceList list, final CTag tag) {
    final List<NaviNode> nodes = CTraceNodeFinder.getTraceNodes(graph, list);

    for (final NaviNode node : nodes) {
      CTaggingFunctions.tagNode(parent, node, tag);
    }
  }
}
