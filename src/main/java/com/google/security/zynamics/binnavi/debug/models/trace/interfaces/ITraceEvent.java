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
package com.google.security.zynamics.binnavi.debug.models.trace.interfaces;

import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceRegister;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceEventType;

import java.util.List;

/**
 * Interface that must be implemented by all trace events that are recorded during a debug trace.
 */
public interface ITraceEvent {
  /**
   * Returns the offset of the event.
   *
   * @return The offset of the event.
   */
  BreakpointAddress getOffset();

  /**
   * Returns the TID that triggered the event.
   *
   * @return The TID that triggered the event.
   */
  long getThreadId();

  /**
   * Returns the type of the event.
   *
   * @return The type of the event.
   */
  TraceEventType getType();

  /**
   * Returns the additional values that describe this event.
   *
   * @return The additional values that describe this event.
   */
  List<TraceRegister> getRegisterValues();
}
