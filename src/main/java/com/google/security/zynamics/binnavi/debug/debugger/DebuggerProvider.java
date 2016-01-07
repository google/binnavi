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
package com.google.security.zynamics.binnavi.debug.debugger;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.DebuggerProviderListener;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.zylib.general.ListenerProvider;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Provides debuggers for debugging a given target.
 */
public final class DebuggerProvider implements BackEndDebuggerProvider {
  /**
   * The debugging target.
   */
  private final DebugTargetSettings debugTargetSettings;

  private final List<IDebugger> debuggers = new ArrayList<>();

  /**
   * Listeners that are notified about changes in the debugger provider.
   */
  private final ListenerProvider<DebuggerProviderListener> m_listeners =
      new ListenerProvider<DebuggerProviderListener>();

  /**
   * Creates a new debugger provider object.
   *
   * @param target The debugging target.
   */
  public DebuggerProvider(final DebugTargetSettings target) {
    debugTargetSettings =
        Preconditions.checkNotNull(target, "IE01669: target argument can not be null");
  }

  /**
   * Adds a new debugger to the debugger provider.
   *
   * @param debugger The debugger to add.
   */
  public void addDebugger(final IDebugger debugger) {
    debuggers.add(
        Preconditions.checkNotNull(debugger, "IE00195: Debugger argument can not be null"));
    for (final DebuggerProviderListener listener : m_listeners) {
      try {
        listener.debuggerAdded(this, debugger);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  @Override
  public void addListener(final DebuggerProviderListener listener) {
    m_listeners.addListener(listener);
  }

  @Override
  public IDebugger getDebugger(final INaviModule module) {
    for (final IDebugger debugger : debuggers) {
      if (debugger.canDebug(module)) {
        return debugger;
      }
    }

    return null;
  }

  @Override
  public List<IDebugger> getDebuggers() {
    return Lists.newArrayList(debuggers);
  }

  @Override
  public DebugTargetSettings getDebugTarget() {
    return debugTargetSettings;
  }

  @Override
  public Iterator<IDebugger> iterator() {
    return debuggers.iterator();
  }

  /**
   * Removes a debugger from the provider.
   *
   * @param debugger The debugger to remove.
   */
  public void removeDebugger(final IDebugger debugger) {
    debuggers.remove(debugger);

    for (final DebuggerProviderListener listener : m_listeners) {
      try {
        listener.debuggerRemoved(this, debugger);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  @Override
  public void removeListener(final DebuggerProviderListener listener) {
    m_listeners.removeListener(listener);
  }
}
