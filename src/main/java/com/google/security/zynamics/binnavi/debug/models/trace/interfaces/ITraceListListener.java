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
 * Interface that must be implemented by all objects that want to be notified about changes in trace
 * lists.
 */
public interface ITraceListListener {
  /**
   * Invoked when the description of the trace list changed.
   *
   * @param traceList The trace list whose description changed.
   */
  void changedDescription(TraceList traceList);

  /**
   * Invoked when the name of the trace list changed.
   *
   * @param traceList The trace list whose name changed.
   */
  void changedName(TraceList traceList);

  /**
   * Invoked when an event was added to the trace list.
   *
   * @param trace The trace list where the event was added.
   * @param event The event that was added to the trace list.
   */
  void eventAdded(TraceList trace, ITraceEvent event);
}
