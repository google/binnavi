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

import java.util.Date;

import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.disassembly.INaviAddressSpace;


/**
 * Adapter class for classes that require only a few address space configuration listener
 * notifications.
 */
public class CAddressSpaceConfigurationListenerAdapter implements
    IAddressSpaceConfigurationListener {
  @Override
  public void changedDebugger(final INaviAddressSpace addressSpace, final DebuggerTemplate debugger) {
    // Empty default implementation
  }

  @Override
  public void changedDescription(final INaviAddressSpace addressSpace, final String description) {
    // Empty default implementation
  }

  @Override
  public void changedModificationDate(final CAddressSpace addressSpace, final Date modificationDate) {
    // Empty default implementation
  }

  @Override
  public void changedName(final INaviAddressSpace addressSpace, final String name) {
    // Empty default implementation
  }
}
