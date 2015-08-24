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
package com.google.security.zynamics.binnavi.Gui.Debug.EventLists.Implementations;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;

import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Gui.Progress.CDefaultProgressOperation;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;
import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceEvent;
import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceListProvider;
import com.google.security.zynamics.zylib.types.common.CollectionHelpers;
import com.google.security.zynamics.zylib.types.common.ICollectionMapper;

/**
 * Contains functions for doing set operations on traces.
 */
public final class CTraceCombinationFunctions {
  /**
   * You are not supposed to instantiate this class.
   */
  private CTraceCombinationFunctions() {
  }

  /**
   * Fills a combined trace from the events of multiple input traces.
   *
   * @param newTrace The trace to fill.
   * @param traces The input events.
   * @param addresses The addresses of the events to put into the combined trace.
   */
  private static void createCombinedTrace(final TraceList newTrace, final List<TraceList> traces,
      final Set<BreakpointAddress> addresses) {
    final Set<BreakpointAddress> visitedAddresses = new LinkedHashSet<BreakpointAddress>();

    for (final TraceList trace : traces) {
      for (final ITraceEvent event : trace) {
        final BreakpointAddress address = event.getOffset();

        if (!addresses.contains(address)) {
          continue;
        }

        if (visitedAddresses.contains(address)) {
          continue;
        }

        visitedAddresses.add(address);

        newTrace.addEvent(event);
      }
    }
  }

  /**
   * Calculates the event addresses that appear exclusively in the first trace.
   *
   * @param trace1 The first input trace.
   * @param trace2 The second input trace.
   *
   * @return The addresses of those events that appear exlusively in the first input trace.
   */
  private static Set<BreakpointAddress> getDifferenceAddresses(
      final TraceList trace1, final TraceList trace2) {
    final List<TraceList> traces = Lists.newArrayList(trace1, trace2);

    final List<Collection<BreakpointAddress>> traceAddresses = getTraceAddresses(traces);

    final LinkedHashSet<BreakpointAddress> addresses =
        new LinkedHashSet<BreakpointAddress>(traceAddresses.get(0));
    addresses.removeAll(traceAddresses.get(1));

    return addresses;
  }

  /**
   * Calculates the event addresses that appear in all traces.
   *
   * @param traces The input traces.
   *
   * @return The addresses of those events that appear in all traces.
   */
  private static LinkedHashSet<BreakpointAddress> getIntersectedAddresses(
      final List<TraceList> traces) {
    final LinkedHashSet<BreakpointAddress> addresses = new LinkedHashSet<BreakpointAddress>();

    boolean first = true;

    for (final Collection<BreakpointAddress> collection : getTraceAddresses(traces)) {
      if (first) {
        addresses.addAll(collection);

        first = false;
      } else {
        addresses.retainAll(collection);
      }
    }

    return addresses;
  }

  /**
   * Returns all event addresses of the events in the given traces.
   *
   * @param traces The input traces.
   *
   * @return The addresses of the events in the traces.
   */
  private static List<Collection<BreakpointAddress>> getTraceAddresses(
      final List<TraceList> traces) {
    return CollectionHelpers.map(
        traces, new ICollectionMapper<TraceList, Collection<BreakpointAddress>>() {
          @Override
          public Collection<BreakpointAddress> map(final TraceList item) {
            return CollectionHelpers.map(
                item.getEvents(), new ICollectionMapper<ITraceEvent, BreakpointAddress>() {
                  @Override
                  public BreakpointAddress map(final ITraceEvent item) {
                    return item.getOffset();
                  }
                });
          }
        });
  }

  /**
   * Calculates the event addresses that appear in any of the traces.
   *
   * @param traces The input traces.
   *
   * @return The addresses of those events that appear in any of the traces.
   */
  private static LinkedHashSet<BreakpointAddress> getUnionizedAddresses(
      final List<TraceList> traces) {
    final LinkedHashSet<BreakpointAddress> addresses = new LinkedHashSet<BreakpointAddress>();

    for (final Collection<BreakpointAddress> collection : getTraceAddresses(traces)) {
      addresses.addAll(collection);
    }

    return addresses;
  }

  /**
   * Creates a new trace that contains exactly those events that appear in the first trace but not
   * in the second trace.
   *
   * @param parent Parent window used for dialogs.
   * @param provider Creates the new trace.
   * @param trace1 The first input trace.
   * @param trace2 The second input trace.
   */
  public static void differenceTraces(final JFrame parent, final ITraceListProvider provider,
      final TraceList trace1, final TraceList trace2) {
    new Thread() {
      @Override
      public void run() {
        try {
          final CDefaultProgressOperation operation =
              new CDefaultProgressOperation("", false, true);
          operation.getProgressPanel().setMaximum(3);

          operation.getProgressPanel().setText(String.format(
              "Creating trace difference between '%s' and '%s'", trace1.getName(),
              trace2.getName()));

          final TraceList newTrace = provider.createTrace(
              "Combined Trace", String.format("%s - %s", trace1.getName(), trace2.getName()));

          operation.getProgressPanel().next();

          createCombinedTrace(
              newTrace, Lists.newArrayList(trace1, trace2), getDifferenceAddresses(trace1, trace2));

          operation.getProgressPanel().next();

          newTrace.save();

          operation.getProgressPanel().next();
          operation.stop();
        } catch (final CouldntSaveDataException e) {
          CUtilityFunctions.logException(e);

          final String innerMessage = "E00191: " + "Could not combine debug traces";
          final String innerDescription = CUtilityFunctions.createDescription(
              "The selected traces could not be combined into a larger trace.",
              new String[] {"There was a problem with the database connection."}, new String[] {
                  "The trace list was not created. You could try to combine the lists again once the connection problem was resolved."});

          NaviErrorDialog.show(parent, innerMessage, innerDescription, e);
        }
      }
    }.start();
  }

  /**
   * Creates a new trace that contains those events that appear in all of the input traces.
   *
   * @param parent Parent window used for dialogs.
   * @param provider Creates the new trace.
   * @param traces The input traces.
   */
  public static void intersectTraces(
      final JFrame parent, final ITraceListProvider provider, final List<TraceList> traces) {
    new Thread() {
      @Override
      public void run() {
        try {
          final CDefaultProgressOperation operation =
              new CDefaultProgressOperation("", false, false);
          operation.getProgressPanel().setMaximum(3);

          operation.getProgressPanel().setText("Combining traces");

          final TraceList newTrace = provider.createTrace("Combined Trace", "");

          operation.next();

          createCombinedTrace(newTrace, traces, getIntersectedAddresses(traces));

          operation.next();

          newTrace.save();

          operation.next();
          operation.stop();
        } catch (final CouldntSaveDataException e) {
          CUtilityFunctions.logException(e);

          final String innerMessage = "E00196: " + "Could not combine debug traces";
          final String innerDescription = CUtilityFunctions.createDescription(
              "The selected traces could not be combined into a larger trace.",
              new String[] {"There was a problem with the database connection."}, new String[] {
                  "The trace list was not created. You could try to combine the lists again once the connection problem was resolved."});

          NaviErrorDialog.show(parent, innerMessage, innerDescription, e);
        }
      }
    }.start();
  }

  /**
   * Creates a new trace that contains those events that appear in any of the input traces.
   *
   * @param parent Parent window used for dialogs.
   * @param provider Creates the new trace.
   * @param traces The input traces.
   */
  public static void unionizeTraces(
      final JFrame parent, final ITraceListProvider provider, final List<TraceList> traces) {
    new Thread() {
      @Override
      public void run() {
        try {
          final CDefaultProgressOperation operation =
              new CDefaultProgressOperation("", false, false);
          operation.getProgressPanel().setMaximum(3);

          operation.getProgressPanel().setText("Combining traces");

          final TraceList newTrace = provider.createTrace("Combined Trace", "");

          operation.next();

          createCombinedTrace(newTrace, traces, getUnionizedAddresses(traces));

          operation.next();

          newTrace.save();

          operation.next();
          operation.stop();
        } catch (final CouldntSaveDataException e) {
          CUtilityFunctions.logException(e);

          final String innerMessage = "E00197: " + "Could not combine debug traces";
          final String innerDescription = CUtilityFunctions.createDescription(
              "The selected traces could not be combined into a larger trace.",
              new String[] {"There was a problem with the database connection."}, new String[] {
                  "The trace list was not created. You could try to combine the lists again once the connection problem was resolved."});

          NaviErrorDialog.show(parent, innerMessage, innerDescription, e);
        }
      }
    }.start();
  }
}
