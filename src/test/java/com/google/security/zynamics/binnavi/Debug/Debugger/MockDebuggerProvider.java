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
package com.google.security.zynamics.binnavi.Debug.Debugger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.security.zynamics.binnavi.debug.debugger.BackEndDebuggerProvider;
import com.google.security.zynamics.binnavi.debug.debugger.DebugTargetSettings;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.DebuggerProviderListener;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;


public final class MockDebuggerProvider implements BackEndDebuggerProvider {
  private final MockDebugTarget m_debugTarget;

  public MockDebuggerProvider() {
    m_debugTarget = new MockDebugTarget();
  }

  @Override
  public void addListener(final DebuggerProviderListener listener) {
  }

  @Override
  public IDebugger getDebugger(final INaviModule module) {
    // TODO Auto-generated method stub
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public List<IDebugger> getDebuggers() {
    return new ArrayList<IDebugger>();
  }

  @Override
  public DebugTargetSettings getDebugTarget() {
    return m_debugTarget;
  }

  @Override
  public Iterator<IDebugger> iterator() {
    // TODO Auto-generated method stub
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void removeListener(final DebuggerProviderListener listener) {
    // TODO Auto-generated method stub
    throw new IllegalStateException("Not yet implemented");
  }

}
