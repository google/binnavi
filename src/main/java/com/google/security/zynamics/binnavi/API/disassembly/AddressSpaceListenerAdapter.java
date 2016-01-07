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
package com.google.security.zynamics.binnavi.API.disassembly;

import java.util.Date;

import com.google.security.zynamics.binnavi.API.debug.Debugger;


// / Adapter class for address spaces
/**
 * Adapter class that can be used by objects that want to listen on address spaces but only need to
 * process few events.
 */
public class AddressSpaceListenerAdapter implements IAddressSpaceListener {
  @Override
  public void addedModule(final AddressSpace addressSpace, final Module module) {
    // Adapter method
  }

  @Override
  public void changedDebugger(final AddressSpace addressSpace, final Debugger debugger) {
    // Adapter method
  }

  @Override
  public void changedDescription(final AddressSpace addressSpace, final String description) {
    // Adapter method
  }

  @Override
  public void changedImageBase(
      final AddressSpace addressSpace, final Module module, final Address address) {
    // Adapter method
  }

  @Override
  public void changedModificationDate(
      final AddressSpace addressSpace, final Date modificationDate) {
    // Adapter method
  }

  @Override
  public void changedName(final AddressSpace addressSpace, final String name) {
    // Adapter method
  }

  @Override
  public void closedAddressSpace(final AddressSpace addressSpace) {
    // Adapter method
  }

  @Override
  public boolean closingAddressSpace(final AddressSpace addressSpace) {
    return true;
  }

  @Override
  public void loadedAddressSpace(final AddressSpace addressSpace) {
    // Adapter method
  }

  @Override
  public void removedModule(final AddressSpace addressSpace, final Module module) {
    // Adapter method
  }
}
