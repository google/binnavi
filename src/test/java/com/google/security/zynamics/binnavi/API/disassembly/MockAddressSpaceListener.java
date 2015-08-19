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
package com.google.security.zynamics.binnavi.API.disassembly;

import java.util.Date;

import com.google.security.zynamics.binnavi.API.debug.Debugger;
import com.google.security.zynamics.binnavi.API.disassembly.Address;
import com.google.security.zynamics.binnavi.API.disassembly.AddressSpace;
import com.google.security.zynamics.binnavi.API.disassembly.IAddressSpaceListener;
import com.google.security.zynamics.binnavi.API.disassembly.Module;


public final class MockAddressSpaceListener implements IAddressSpaceListener {
  public String events = "";

  @Override
  public void addedModule(final AddressSpace addressSpace, final Module module) {
    events += "addedModule;";
  }

  @Override
  public void changedDebugger(final AddressSpace addressSpace, final Debugger debugger) {
    events += "changedDebugger;";
  }

  @Override
  public void changedDescription(final AddressSpace addressSpace, final String description) {
    events += "changedDescription;";
  }

  @Override
  public void changedImageBase(final AddressSpace addressSpace, final Module module,
      final Address address) {
    events += "changedImageBase;";
  }

  @Override
  public void changedModificationDate(final AddressSpace addressSpace, final Date modificationDate) {
    events += "changedModificationDate;";
  }

  @Override
  public void changedName(final AddressSpace addressSpace, final String name) {
    events += "changedName;";
  }

  @Override
  public void closedAddressSpace(final AddressSpace addressSpace) {
    events += "closed;";
  }

  @Override
  public boolean closingAddressSpace(final AddressSpace addressSpace) {
    events += "closing;";

    return true;
  }

  @Override
  public void loadedAddressSpace(final AddressSpace addressSpace) {
    events += "loaded;";
  }

  @Override
  public void removedModule(final AddressSpace addressSpace, final Module module) {
    events += "removedModule;";
  }
}
