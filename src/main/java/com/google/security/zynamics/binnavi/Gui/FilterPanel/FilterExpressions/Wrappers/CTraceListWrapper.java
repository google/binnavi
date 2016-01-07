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
package com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions.Wrappers;

import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;

/**
 * Wraps a trace list object for filtering.
 */
public final class CTraceListWrapper implements IFilterWrapper<TraceList>, INamedElement {
  /**
   * The wrapped trace list object.
   */
  private final TraceList m_traceList;

  /**
   * Creates a new wrapper object.
   * 
   * @param traceList The wrapped trace list object.
   */
  public CTraceListWrapper(final TraceList traceList) {
    m_traceList = traceList;
  }

  @Override
  public String getDescription() {
    return m_traceList.getDescription();
  }

  @Override
  public String getName() {
    return m_traceList.getName();
  }

  @Override
  public TraceList unwrap() {
    return m_traceList;
  }
}
