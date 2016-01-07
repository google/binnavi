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
import com.google.common.collect.ImmutableList;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceEvent;

import java.util.List;

/**
 * Trace event that can occur during a debug trace.
 */
public final class TraceEvent implements ITraceEvent {
  /**
   * Thread ID of the thread that is the source of the event.
   */
  private final long traceEventSourceThreadId;

  /**
   * Address of the trace event.
   */
  private final BreakpointAddress traceEventBreakpointAddress;

  /**
   * Type of the trace event.
   */
  private final TraceEventType traceEventType;

  /**
   * Additional values that describe the trace event.
   */
  private final ImmutableList<TraceRegister> traceValues;

  /**
   * Creates a new trace event.
   *
   * @param tid Thread ID of the thread that is the source of the event.
   * @param address The address of the trace event.
   * @param type The type of the trace event.
   * @param values values Additional values that describe the trace event.
   */
  public TraceEvent(final long tid, final BreakpointAddress address, final TraceEventType type,
      final List<TraceRegister> values) {
    traceEventBreakpointAddress =
        Preconditions.checkNotNull(address, "IE00775: Address argument can not be null");
    traceEventType = Preconditions.checkNotNull(type, "IE00776: Type argument can not be null");
    traceEventSourceThreadId = tid;
    traceValues = ImmutableList.copyOf(values);
  }

  @Override
  public BreakpointAddress getOffset() {
    return traceEventBreakpointAddress;
  }

  @Override
  public long getThreadId() {
    return traceEventSourceThreadId;
  }

  @Override
  public TraceEventType getType() {
    return traceEventType;
  }

  /**
   * Returns the additional values that describe this event.
   *
   * @return The additional values that describe this event.
   */
  @Override
  public ImmutableList<TraceRegister> getRegisterValues() {
    return traceValues;
  }
}
