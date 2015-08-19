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
package com.google.security.zynamics.binnavi.disassembly.Modules;

import java.util.ArrayList;
import java.util.List;

import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;
import com.google.security.zynamics.binnavi.disassembly.Modules.CTraceContainer;
import com.google.security.zynamics.binnavi.disassembly.Modules.ITraceContainerListener;


public class MockTraceContainerListener implements ITraceContainerListener {
  public String eventList = "";

  public List<TraceList> addedTraces = new ArrayList<TraceList>();

  public List<TraceList> deletedTraces = new ArrayList<TraceList>();

  @Override
  public void addedTrace(final CTraceContainer container, final TraceList trace) {
    eventList += "addedTrace/";

    addedTraces.add(trace);
  }

  @Override
  public void deletedTrace(final CTraceContainer container, final TraceList trace) {
    eventList += "deletedTrace/";

    deletedTraces.add(trace);
  }
}
