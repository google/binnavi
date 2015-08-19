/*
Copyright 2014 Google Inc. All Rights Reserved.

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
package com.google.security.zynamics.binnavi.Debug.Models.Trace;

import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;
import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceEvent;
import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceListListener;


public final class MockListListener implements ITraceListListener {
  public String events = "";

  @Override
  public void changedDescription(final TraceList traceList) {
    throw new RuntimeException("Not yet implemented");
  }

  @Override
  public void changedName(final TraceList traceList) {
    throw new RuntimeException("Not yet implemented");
  }

  @Override
  public void eventAdded(final TraceList trace, final ITraceEvent event) {
    events += event.getOffset().getAddress().getAddress().toHexString() + ":" + event + ";";
  }
}
