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

// / Used to listen on traces.
/**
 * Interface that can be implemented by objects that want to be notified about changes in trace
 * objects.
 */
public interface ITraceListener {
  // ! Signals a new trace event.
  /**
   * Invoked after a new event was added to the trace. This generally happens when a breakpoint was
   * hit during an active trace.
   *
   * @param trace The trace where the event was added.
   * @param event The event that was added to the trace.
   */
  void addedEvent(Trace trace, TraceEvent event);

  // ! Signals a change in the trace description.
  /**
   * Invoked after the description string of the trace changed.
   *
   * @param trace The trace whose description changed.
   * @param description The new description string of the trace.
   */
  void changedDescription(Trace trace, String description);

  // ! Signals a change in the trace name.
  /**
   * Invoked after the name string of the trace changed.
   *
   * @param trace The trace whose name changed.
   * @param name The new name string of the trace.
   */
  void changedName(Trace trace, String name);
}
