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
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.AddressSpaceLoadEvents;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpaceContent;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.IAddressSpaceListener;

public final class MockAddressSpaceListener implements IAddressSpaceListener {
  public String events = "";

  @Override
  public void closed(final INaviAddressSpace addressSpace, final CAddressSpaceContent content) {
    return;
  }

  @Override
  public boolean closing(final INaviAddressSpace addressSpace) {
    return true;
  }

  @Override
  public void loaded(final INaviAddressSpace addressSpace) {
    events += "loadedAddressSpace;";
  }

  @Override
  public boolean loading(final AddressSpaceLoadEvents event, final int counter) {
    return true;
  }
}
