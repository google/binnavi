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
package com.google.security.zynamics.binnavi.standardplugins.callresolver;

import java.util.ArrayList;
import java.util.List;

import com.google.security.zynamics.binnavi.API.debug.Debugger;
import com.google.security.zynamics.binnavi.API.disassembly.Module;
import com.google.security.zynamics.binnavi.API.disassembly.ModuleListenerAdapter;
import com.google.security.zynamics.binnavi.API.disassembly.View;


/**
 * This class encapsulates a module to act like a call resolver target.
 */
public final class ModuleCallResolverTarget implements ICallResolverTarget {
  /**
   * The encapsulated module.
   */
  private final Module module;

  /**
   * Listeners that are notified about changes in the call resolver target.
   */
  private final List<ICallResolverTargetListener> listeners =
      new ArrayList<ICallResolverTargetListener>();

  /**
   * Converts events of the module into events of the call resolver target.
   */
  private final InternalModuleListener internalModuleListener = new InternalModuleListener();

  /**
   * Creates a new call resolver target.
   * 
   * @param module The encapsulated module.
   */
  public ModuleCallResolverTarget(final Module module) {
    this.module = module;

    module.addListener(internalModuleListener);
  }

  @Override
  public void addListener(final ICallResolverTargetListener listener) {
    listeners.add(listener);
  }

  @Override
  public View createView() {
    return module.createView("Dynamic Trace View", "");
  }

  @Override
  public Debugger getDebugger() {
    return module.getDebugger();
  }

  @Override
  public List<IndirectCall> getIndirectCalls() {
    return IndirectCallFinder.find(module);
  }

  @Override
  public List<Module> getModules() {
    final List<Module> modules = new ArrayList<Module>();

    modules.add(module);

    return modules;
  }

  @Override
  public void removeListener(final ICallResolverTargetListener listener) {
    listeners.remove(listener);
  }

  /**
   * Converts events of the module into events of the call resolver target.
   */
  private class InternalModuleListener extends ModuleListenerAdapter {
    @Override
    public void changedDebugger(final Module module, final Debugger debugger) {
      for (final ICallResolverTargetListener listener : listeners) {
        try {
          listener.changedDebugger(ModuleCallResolverTarget.this, debugger);
        } catch (final Exception exception) {
          exception.printStackTrace();
        }
      }
    }
  }
}
