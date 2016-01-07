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

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceListProvider;

import java.util.List;

/**
 * Base class for trace provider classes.
 */
public abstract class AbstractTraceProvider implements ITraceListProvider {
  /**
   * Determines whether there exists a trace with the given name.
   *
   * @param name The name of the trace.
   * @param traces List of traces to search through.
   * @return Returns if a trace with the given name exists in the trace.
   */
  private static boolean hasTrace(final String name, final List<TraceList> traces) {
    return !Collections2.filter(traces, new Predicate<TraceList>() {
      @Override
      public boolean apply(final TraceList trace) {
        return trace.getName().equals(name);
      }
    }).isEmpty();
  }

  @Override
  public String generateName() {
    // Generate unique trace names of the form "Trace X"
    final List<TraceList> traces = getTraces();
    int counter = 0;
    do {
      final String name = "Trace" + counter;
      if (!hasTrace(name, traces)) {
        return name;
      }
      ++counter;
    } while (true);
  }
}
