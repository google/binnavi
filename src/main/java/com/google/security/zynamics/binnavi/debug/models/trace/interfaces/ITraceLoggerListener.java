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

import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;

/**
 * Interface for objects that want to be notified about events received during trace logging.
 */
public interface ITraceLoggerListener {
  /**
   * Invoked after a breakpoint was added to the active trace.
   */
  void addedBreakpoint();

  /**
   * Invoked after a trace was finished.
   *
   * @param trace The trace that was finished.
   */
  void finished(TraceList trace);

  /**
   * Invoked after a breakpoint was removed from the active trace.
   */
  void removedBreakpoint();
}
