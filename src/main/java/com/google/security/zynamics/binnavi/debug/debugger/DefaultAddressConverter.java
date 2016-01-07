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
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IAddressConverter;
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;

/**
 * Default address converter that should work for all address conversions in linear address spaces.
 */
public final class DefaultAddressConverter implements IAddressConverter {
  /**
   * Start address of the section in memory.
   */
  private final IAddress sectionMemoryStartAddress;

  /**
   * Start address of the file in memory.
   */
  private final IAddress fileMemoryStartAddress;

  /**
   * Creates a new default address converted.
   *
   * @param membase Start address of the section in memory.
   * @param filebase Start address of the file in memory.
   */
  public DefaultAddressConverter(final IAddress membase, final IAddress filebase) {
    sectionMemoryStartAddress =
        Preconditions.checkNotNull(membase, "IE00817: Memory base argument can not be null");
    fileMemoryStartAddress =
        Preconditions.checkNotNull(filebase, "IE00819: File base argument can not be null");
  }

  @Override
  public RelocatedAddress fileToMemory(final UnrelocatedAddress address) {
    Preconditions.checkNotNull(address, "IE00820: Address argument can not be null");
    return new RelocatedAddress(new CAddress(address.getAddress().toLong()
        - fileMemoryStartAddress.toLong() + sectionMemoryStartAddress.toLong()));
  }

  @Override
  public UnrelocatedAddress memoryToFile(final RelocatedAddress address) {
    Preconditions.checkNotNull(address, "IE00821: Address argument can not be null");
    return new UnrelocatedAddress(new CAddress(address.getAddress().toLong()
        - sectionMemoryStartAddress.toLong() + fileMemoryStartAddress.toLong()));
  }
}
