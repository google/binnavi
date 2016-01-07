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

// ! Describes trace events.
/**
 * This enumeration contains all possible types of trace events.
 */
public enum TraceEventType {
  /**
   * Trace event was caused by a regular breakpoint.
   */
  Breakpoint,

  /**
   * Trace event was caused by an echo breakpoint.
   */
  EchoBreakpoint;

  // / @cond INTERNAL
  /**
   * Converts an internal trace event type to an API trace event type.
   *
   * @param type The trace event type to convert.
   *
   * @return The converted trace event type.
   */
  public static TraceEventType convert(final com.google.security.zynamics.binnavi.debug.models.trace.TraceEventType type) {
    switch (type) {
      case REGULAR_BREAKPOINT:
        return Breakpoint;
      case ECHO_BREAKPOINT:
        return EchoBreakpoint;
      default:
        throw new IllegalStateException("Error: Unknown event type");
    }
  }

  /**
   * Converts an API trace event type to an internal trace event type.
   *
   * @return The internal trace event type.
   */
  // / @endcond
  public com.google.security.zynamics.binnavi.debug.models.trace.TraceEventType getNative() {
    switch (this) {
      case Breakpoint:
        return com.google.security.zynamics.binnavi.debug.models.trace.TraceEventType.REGULAR_BREAKPOINT;
      case EchoBreakpoint:
        return com.google.security.zynamics.binnavi.debug.models.trace.TraceEventType.ECHO_BREAKPOINT;
      default:
        throw new IllegalStateException("Error: Unknown event type");
    }
  }
}
