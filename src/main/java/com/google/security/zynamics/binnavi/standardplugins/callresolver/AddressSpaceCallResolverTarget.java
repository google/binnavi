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
import com.google.security.zynamics.binnavi.API.disassembly.AddressSpace;
import com.google.security.zynamics.binnavi.API.disassembly.AddressSpaceListenerAdapter;
import com.google.security.zynamics.binnavi.API.disassembly.Module;
import com.google.security.zynamics.binnavi.API.disassembly.View;


/**
 * This class encapsulates an address space to act like a call resolver target.
 */
public final class AddressSpaceCallResolverTarget implements ICallResolverTarget {
  /**
   * The encapsulated address space.
   */
  private final AddressSpace addressSpace;

  /**
   * Listeners that are notified about changes in the call resolver target.
   */
  private final List<ICallResolverTargetListener> listeners =
      new ArrayList<ICallResolverTargetListener>();

  /**
   * Converts events of the address space into events of the call resolver target.
   */
  private final InternalAddressSpaceListener internalAddressSpaceListener =
      new InternalAddressSpaceListener();

  /**
   * Creates a new call resolver target.
   * 
   * @param addressSpace The encapsulated address space.
   */
  public AddressSpaceCallResolverTarget(final AddressSpace addressSpace) {
    this.addressSpace = addressSpace;

    addressSpace.addListener(internalAddressSpaceListener);
  }

  @Override
  public void addListener(final ICallResolverTargetListener listener) {
    listeners.add(listener);
  }

  @Override
  public View createView() {
    return addressSpace.getProject().createView("Dynamic Trace View", "");
  }

  @Override
  public Debugger getDebugger() {
    return addressSpace.getDebugger();
  }

  @Override
  public List<IndirectCall> getIndirectCalls() {
    final List<IndirectCall> indirectCalls = new ArrayList<IndirectCall>();

    for (final Module module : addressSpace.getModules()) {
      indirectCalls.addAll(IndirectCallFinder.find(module));
    }

    return indirectCalls;
  }

  @Override
  public List<Module> getModules() {
    return addressSpace.getModules();
  }

  @Override
  public void removeListener(final ICallResolverTargetListener listener) {
    listeners.remove(listener);
  }

  /**
   * Converts events of the address space into events of the call resolver target.
   */
  private class InternalAddressSpaceListener extends AddressSpaceListenerAdapter {
    // TODO (timkornau): Handle closing address space

    @Override
    public void changedDebugger(final AddressSpace addressSpace, final Debugger debugger) {
      for (final ICallResolverTargetListener listener : listeners) {
        try {
          listener.changedDebugger(AddressSpaceCallResolverTarget.this, debugger);
        } catch (final Exception exception) {
          exception.printStackTrace();
        }
      }
    }
  }
}
