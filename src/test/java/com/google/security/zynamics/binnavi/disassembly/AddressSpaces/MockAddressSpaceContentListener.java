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
package com.google.security.zynamics.binnavi.disassembly.AddressSpaces;


import com.google.security.zynamics.binnavi.disassembly.INaviAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.IAddressSpaceContentListener;
import com.google.security.zynamics.zylib.disassembly.IAddress;

public class MockAddressSpaceContentListener implements IAddressSpaceContentListener {
  public String events = "";

  @Override
  public void addedModule(final INaviAddressSpace addressSpace, final INaviModule module) {
    events += "addedModule;";
  }

  @Override
  public void changedImageBase(final INaviAddressSpace addressSpace, final INaviModule module,
      final IAddress address) {
    events += "changedImageBase;";
  }

  @Override
  public void removedModule(final INaviAddressSpace addressSpace, final INaviModule module) {
    events += "removedModule;";
  }
}
