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

/**
 * Enumeration that contains the type of all trace events that can occur during a debug trace.
 */
public enum TraceEventType {
  /**
   * Echo breakpoint was hit.
   */
  ECHO_BREAKPOINT,

  /**
   * Regular breakpoint was hit.
   */
  REGULAR_BREAKPOINT;

  /**
   * Converts the numeric representation of a trace event type into a trace event type object.
   *
   * @param type The numeric representation of the trace event type.
   *
   * @return The trace event type object.
   */
  public static TraceEventType parseInt(final int type) {
    if (type == 1) {
      return TraceEventType.ECHO_BREAKPOINT;
    } else if (type == 2) {
      return TraceEventType.REGULAR_BREAKPOINT;
    }
    throw new IllegalStateException("IE00826: Unknown trace event type");
  }
}
