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
package com.google.security.zynamics.binnavi.API.disassembly;

import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceEvent;

import java.util.ArrayList;
import java.util.List;


// / A single debug event.
/**
 * Represents a single debug event.
 */
public final class TraceEvent {
  /**
   * The wrapped internal trace event.
   */
  private final ITraceEvent m_event;

  // / @cond INTERNAL
  /**
   * Creates a new API trace event object.
   *
   * @param event The wrapped internal trace event.
   */
  // / @endcond
  public TraceEvent(final ITraceEvent event) {
    m_event = event;
  }

  // ! Address of the debug event.
  /**
   * Returns the address of the debug event.
   *
   * @return The address of the debug event.
   */
  public Address getAddress() {
    return new Address(m_event.getOffset().getAddress().getAddress().toBigInteger());
  }

  // ! Recorded register values.
  /**
   * Returns the register values that were created when the event happened.
   *
   * @return The recorded register values.
   */
  public List<TraceRegister> getTraceValues() {
    final ArrayList<TraceRegister> registers = new ArrayList<TraceRegister>();
    for (final com.google.security.zynamics.binnavi.debug.models.trace.TraceRegister register :
        m_event.getRegisterValues()) {
      registers.add(new TraceRegister(register));
    }

    return registers;
  }

  // ! Type of the event.
  /**
   * Returns the type of the trace event.
   *
   * @return The type of the trace event.
   */
  public TraceEventType getType() {
    return TraceEventType.convert(m_event.getType());
  }

  // ! Printable representation of the trace event.
  /**
   * Returns a string representation of the trace event.
   *
   * @return A string representation of the trace event.
   */
  @Override
  public String toString() {
    return String.format("Trace Event [%s : %s]", getType(), getAddress());
  }
}
