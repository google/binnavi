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

import java.util.Iterator;
import java.util.List;

import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;
import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceListProvider;
import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceManagerListener;


public final class MockTraceListProvider implements ITraceListProvider {
  @Override
  public void addListener(final ITraceManagerListener listener) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public TraceList createTrace(final String name, final String description) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public String generateName() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public TraceList getList(final int index) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public int getNumberOfTraceLists() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public List<TraceList> getTraces() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public boolean isLoaded() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public Iterator<TraceList> iterator() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void removeList(final TraceList list) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void removeListener(final ITraceManagerListener listener) {
    throw new IllegalStateException("Not yet implemented");
  }
}
