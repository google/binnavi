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
package com.google.security.zynamics.binnavi.debug.models.trace;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Log.NaviLogger;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.EchoBreakpointHitReply;
import com.google.security.zynamics.binnavi.debug.debugger.DebugEventListenerAdapter;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerHelpers;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugEventListener;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.helpers.EchoBreakpointCollector;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.Breakpoint;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointManager;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointManagerListenerAdapter;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointType;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ProcessManagerListenerAdapter;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.RegisterValue;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.ThreadRegisters;
import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceListProvider;
import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceLoggerListener;
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.general.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class contains all the code that is necessary to log event lists for a single trace.
 */
public final class TraceLogger {
  /**
   * The event list that is currently active for the graph
   */
  private TraceList eventList;

  /**
   * List of the currently active echo breakpoints
   */
  private final Map<BreakpointAddress, Integer> activeEchoBreakpoints = new HashMap<>();

  /**
   * Breakpoint manager that is used to set and remove echo breakpoints.
   */
  private final BreakpointManager breakpointManager;

  /**
   * Event list manager the new event list is added to.
   */
  private final ITraceListProvider traceProvider;

  /**
   * Connection from where the debug events come.
   */
  private final IDebugger debugger;

  /**
   * Keeps track of the breakpoints set in the target process and updates the current trace if
   * necessary.
   */
  private final InternalBreakpointManagerListener m_breakpointManagerListener =
      new InternalBreakpointManagerListener();

  /**
   * Keeps track of the target process and updates the current trace if necessary.
   */
  private final InternalProcessListener m_processListener = new InternalProcessListener();

  /**
   * List of listeners that are notified about trace events.
   */
  private final ListenerProvider<ITraceLoggerListener> listeners =
      new ListenerProvider<ITraceLoggerListener>();

  /**
   * This lock is used to synchronize removal of listeners from different threads.
   */
  private final Lock lock = new ReentrantLock();

  /**
   * Listens on echo breakpoint hits and puts events into the logged trace.
   */
  private final IDebugEventListener m_debuggerListener = new DebugEventListenerAdapter() {
    private Pair<ThreadRegisters, BreakpointAddress> getAddress(
        final EchoBreakpointHitReply reply) {
      for (final ThreadRegisters threadRegisters : reply.getRegisterValues()) {
        if (reply.getThreadId() == threadRegisters.getTid()) {
          for (final RegisterValue registerValue : threadRegisters) {
            if (registerValue.isPc()) {
              final BreakpointAddress address = DebuggerHelpers.getBreakpointAddress(debugger,
                  new RelocatedAddress(new CAddress(registerValue.getValue())));
              return new Pair<ThreadRegisters, BreakpointAddress>(threadRegisters, address);
            }
          }
        }
      }

      throw new IllegalStateException();
    }

    @Override
    public void receivedReply(final EchoBreakpointHitReply reply) {
      lock.lock();
      final Pair<ThreadRegisters, BreakpointAddress> addressPair = getAddress(reply);
      final BreakpointAddress address = addressPair.second();

      // If trace mode is active and the echo breakpoint that was hit
      // is part of the current graph, the event is added to the current
      // event list.

      if (hasEchoBreakpoint(address)) {
        NaviLogger.info("Adding echo breakpoint event %s to event list %s",
            address.getAddress().getAddress().toHexString(), eventList.getName());
        final List<TraceRegister> registers = new ArrayList<TraceRegister>();
        for (final RegisterValue registerValue : addressPair.first()) {
          registers.add(new TraceRegister(registerValue.getName(),
              new CAddress(registerValue.getValue()), registerValue.getMemory()));
        }
        final List<TraceRegister> valueSet = Lists.newArrayList(registers);
        final TraceEvent newEvent =
            new TraceEvent(reply.getThreadId(), address, TraceEventType.ECHO_BREAKPOINT, valueSet);
        eventList.addEvent(newEvent);
        final Integer count = activeEchoBreakpoints.get(address);
        if (count != null) {
          final int remaining = count - 1;
          if (remaining <= 0) {
            breakpointManager.removeBreakpoints(BreakpointType.ECHO, Sets.newHashSet(address));
          } else {
            activeEchoBreakpoints.put(address, remaining);
          }
        }
      } else {
        NaviLogger.info("Unknown echo breakpoint event for address [%s]",
            address.getAddress().getAddress().toHexString());
      }
      lock.unlock();
    }
  };

  /**
   * Create a new trace logger object.
   *
   * @param traceProvider The event list provider where the event list of the new trace is added.
   * @param debugger The debugger object that is recording the trace.
   */
  public TraceLogger(final ITraceListProvider traceProvider, final IDebugger debugger) {
    this.traceProvider =
        Preconditions.checkNotNull(traceProvider, "IE00785: Trace provider can not be null");
    this.debugger = Preconditions.checkNotNull(debugger, "IE00786: Debugger can not be null");
    breakpointManager = debugger.getBreakpointManager();
  }

  /**
   * Determines whether there is an echo breakpoint with the given address in the current trace.
   *
   * @param address The breakpoint address to check.
   *
   * @return True, if there is an echo breakpoint with the given address in the trace.
   */
  private boolean hasEchoBreakpoint(final BreakpointAddress address) {
    return activeEchoBreakpoints.containsKey(address);
  }

  /**
   * Removes all added listeners.
   */
  private void removeListeners() {
    debugger.removeListener(m_debuggerListener);
    breakpointManager.removeListener(m_breakpointManagerListener);
    debugger.getProcessManager().removeListener(m_processListener);
  }

  /**
   * Returns the number of active echo breakpoints which were not yet hit.
   *
   * @return The number of active echo breakpoints.
   */
  public int activeEchoBreakpointCount() {
    return activeEchoBreakpoints.size();
  }

  /**
   * Adds a listener object to the trace logger.
   *
   * @param listener The listener object to add.
   */
  public void addListener(final ITraceLoggerListener listener) {
    listeners.addListener(listener);
  }

  /**
   * Returns the trace that was recorded by the logger.
   *
   * @return The trace that was recorded by the logger.
   */
  public TraceList getTrace() {
    return eventList;
  }

  /**
   * Returns the trace provider of the logger.
   *
   * @return The trace provider of the logger.
   */
  public ITraceListProvider getTraceProvider() {
    return traceProvider;
  }

  /**
   * Determines whether there are echo breakpoints in the current trace.
   *
   * @return True, if there are echo breakpoints that were not yet hit.
   */
  public boolean hasEchoBreakpoints() {
    return !activeEchoBreakpoints.isEmpty();
  }

  /**
   * Removes a listener object from the trace logger.
   *
   * @param listener The listener object to remove.
   */
  public void removeListener(final ITraceLoggerListener listener) {
    listeners.removeListener(listener);
  }

  /**
   * Starts an event trace.
   *
   * @param trace Trace list where the recorded events are stored.
   * @param relocatedAddresses List of addresses where echo breakpoints are put
   * @param maximumHits Maximum number of hits before an echo breakpoint is removed.
   */
  public void start(final TraceList trace, final Set<BreakpointAddress> relocatedAddresses,
      final int maximumHits) {
    Preconditions.checkNotNull(relocatedAddresses, "IE00762: Address list can not be null");
    Preconditions.checkArgument(!relocatedAddresses.isEmpty(),
        "IE00787: Address list can not be empty");
    for (final BreakpointAddress address : relocatedAddresses) {
      Preconditions.checkNotNull(address, "IE00788: Address list contains invalid elements");
    }
    lock.lock(); // Lock required because while breakpoints are added, previously set breakpoints
                 // can be hit => Comodification exception.
    eventList = trace;
    // The trace logger must handle debug events
    debugger.addListener(m_debuggerListener);
    debugger.getProcessManager().addListener(m_processListener);
    breakpointManager.addListener(m_breakpointManagerListener);
    NaviLogger.info("Starting new event list with name %s", trace.getName());
    final Set<BreakpointAddress> collectedAddresses = new HashSet<BreakpointAddress>();
    for (final BreakpointAddress address : relocatedAddresses) {
      if (EchoBreakpointCollector.isBlocked(breakpointManager, address)) {
        continue;
      }
      if (!debugger.isConnected()) {
        lock.unlock();
        return;
      }
      collectedAddresses.add(address);
    }
    breakpointManager.addBreakpoints(BreakpointType.ECHO, collectedAddresses);
    for (final BreakpointAddress address : collectedAddresses) {
      try {
        // Add the echo breakpoint to the list of active echo breakpoints
        activeEchoBreakpoints.put(address, maximumHits);
        for (final ITraceLoggerListener listener : listeners) {
          listener.addedBreakpoint();
        }
      } catch (final IllegalArgumentException exception) {
        // This is possible in case of the following race condition:
        //
        // 1. m_bpManager.hasEchoBreakpoint(address) => false
        // 2. Echo breakpoint is set at address by another thread
        // 3. This thread tries to set an echo breakpoint at address
        CUtilityFunctions.logException(exception);
      }
    }
    if (activeEchoBreakpoints.isEmpty()) {
      // Can happen if all given addresses are blocked
      removeListeners();
    }
    lock.unlock();
  }

  /**
   * Stops the trace mode.
   *
   * @return The echo breakpoints that were removed.
   */
  public Set<BreakpointAddress> stop() {
    NaviLogger.info("Finalizing event list %s with %d events", eventList.getName(),
        eventList.getEventCount());
    // Nothing to do if all echo breakpoints were hit.
    if (activeEchoBreakpointCount() == 0) {
      return new HashSet<BreakpointAddress>();
    }
    lock.lock();
    // No more events please
    removeListeners();
    // Remove all echo breakpoints which were not hit.
    // Copy is necessary to avoid a ConcurrentModificationException
    final Set<BreakpointAddress> ebps = new HashSet<>(activeEchoBreakpoints.keySet());
    breakpointManager.removeBreakpoints(BreakpointType.ECHO, ebps);
    try {
      for (final ITraceLoggerListener listener : listeners) {
        listener.removedBreakpoint();
      }
    } catch (final IllegalArgumentException exception) {
      // This can happen if the debugger quits while
      // we are removing echo breakpoints.
    }
    activeEchoBreakpoints.clear();
    lock.unlock();
    for (final ITraceLoggerListener listener : listeners) {
      try {
        listener.finished(eventList);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
    return ebps;
  }

  /**
   * Changes the trace list that receives events.
   *
   * @param trace The new trace list that receives events.
   *
   * @return The old trace list that previously received events.
   */
  public TraceList switchTargetList(final TraceList trace) {
    final TraceList oldList = eventList;
    eventList = trace;
    return oldList;
  }

  /**
   * Updates the trace list in case of changing breakpoints.
   */
  private class InternalBreakpointManagerListener extends BreakpointManagerListenerAdapter {
    @Override
    public void breakpointsRemoved(final Set<Breakpoint> breakpoints) {
      if (!debugger.isConnected()) {
        return;
      }
      final Set<BreakpointAddress> echoBreakpoints = Sets.newHashSet();
      for (final Breakpoint breakpoint : breakpoints) {
        if (breakpoint.getType() == BreakpointType.ECHO) {
          echoBreakpoints.add(breakpoint.getAddress());
          activeEchoBreakpoints.remove(breakpoint.getAddress());
        }
      }
      for (final ITraceLoggerListener listener : listeners) {
        listener.removedBreakpoint();
      }
      if (activeEchoBreakpointCount() == 0) {
        removeListeners();
        for (final ITraceLoggerListener listener : listeners) {
          listener.finished(eventList);
        }
      }
      NaviLogger.info("Removed %d echo breakpoints from the breakpoint manager",
          echoBreakpoints.size());
    }
  }

  /**
   * Keeps track of the target process and updates the current trace if necessary.
   */
  private class InternalProcessListener extends ProcessManagerListenerAdapter {
    @Override
    public void detached() {
      // No more events please
      removeListeners();
      activeEchoBreakpoints.clear();
      for (final ITraceLoggerListener listener : listeners) {
        listener.finished(eventList);
      }
    }
  }
}
