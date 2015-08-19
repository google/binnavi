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
package com.google.security.zynamics.binnavi.disassembly;

import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Debug.Models.Trace.MockTraceListProvider;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerProvider;
import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceListProvider;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainerListener;
import com.google.security.zynamics.zylib.general.Pair;

import java.util.List;

public final class MockViewContainer implements IViewContainer {

  private final ITraceListProvider m_traceProvider = new MockTraceListProvider();

  @Override
  public Object getNative() {

    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void addListener(final IViewContainerListener listener) {

    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public boolean containsModule(final INaviModule module) {

    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public INaviView createView(final String name, final String description) {

    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void deleteView(final INaviView view) {

    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void dispose() {

    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public List<INaviAddressSpace> getAddressSpaces() {

    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public IDatabase getDatabase() {

    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public DebuggerProvider getDebuggerProvider() {

    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public CFunction getFunction(final INaviView view) {
    return null;
  }

  @Override
  public List<INaviFunction> getFunctions() {

    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public List<INaviModule> getModules() {

    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public String getName() {

    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public List<Pair<INaviView, CTag>> getTaggedViews() {

    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public List<INaviView> getTaggedViews(final CTag object) {

    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public ITraceListProvider getTraceProvider() {
    return m_traceProvider;
  }

  @Override
  public List<INaviView> getUserViews() {

    return null;
  }

  @Override
  public INaviView getView(final INaviFunction function) {

    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public int getViewCount() {

    return 0;
  }

  @Override
  public List<INaviView> getViews() {

    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public List<INaviView> getViewsWithAddresses(
      final List<UnrelocatedAddress> address, final boolean all) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public boolean isLoaded() {
    return true;
  }

  @Override
  public void removeListener(final IViewContainerListener listener) {

    throw new IllegalStateException("Not yet implemented");
  }
}
