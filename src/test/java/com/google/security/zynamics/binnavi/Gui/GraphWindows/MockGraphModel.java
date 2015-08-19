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
package com.google.security.zynamics.binnavi.Gui.GraphWindows;

import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Debug.Debugger.MockDebuggerProvider;
import com.google.security.zynamics.binnavi.ZyGraph.ZyGraphFactory;
import com.google.security.zynamics.binnavi.disassembly.MockViewContainer;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;


public final class MockGraphModel implements IGraphModel {
  private final MockDebuggerProvider m_debuggerProvider;
  private final IViewContainer m_viewContainer;

  public MockGraphModel() {
    m_debuggerProvider = new MockDebuggerProvider();
    m_viewContainer = new MockViewContainer();
  }

  @Override
  public IDatabase getDatabase() {
    // TODO Auto-generated method stub
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public MockDebuggerProvider getDebuggerProvider() {
    return m_debuggerProvider;
  }

  @Override
  public ZyGraph getGraph() {
    return ZyGraphFactory.get();
  }

  @Override
  public CGraphWindow getParent() {
    return null;
  }

  @Override
  public IViewContainer getViewContainer() {
    return m_viewContainer;
  }

}
