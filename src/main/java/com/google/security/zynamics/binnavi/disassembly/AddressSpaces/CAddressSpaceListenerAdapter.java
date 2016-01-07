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
package com.google.security.zynamics.binnavi.disassembly.AddressSpaces;

import com.google.security.zynamics.binnavi.disassembly.INaviAddressSpace;

/**
 * A listener adapter for address space listeners that do not want to implement all methods of the
 * regular address space listener.
 */
public class CAddressSpaceListenerAdapter implements IAddressSpaceListener {
  @Override
  public void closed(final INaviAddressSpace addressSpace, final CAddressSpaceContent content) {
    // Empty default implementation
  }

  @Override
  public boolean closing(final INaviAddressSpace addressSpace) {
    // Empty default implementation
    return true;
  }

  @Override
  public void loaded(final INaviAddressSpace addressSpace) {
    // Empty default implementation
  }

  @Override
  public boolean loading(final AddressSpaceLoadEvents event, final int counter) {
    // Empty default implementation
    return true;
  }
}
