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
package com.google.security.zynamics.binnavi.API.debug;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.API.disassembly.Address;
import com.google.security.zynamics.binnavi.APIHelpers.ApiObject;

// ! Module inside a debugged target process.
/**
 * Represents a module that is loaded into the memory of the target process. This module can be
 * either the target module itself or dynamically loaded libraries that were loaded into the address
 * space of the target process.
 */
public final class MemoryModule implements
    ApiObject<com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryModule> {
  /**
   * Wrapped internal memory module.
   */
  private final com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryModule
      module;

  // / @cond INTERNAL
  /**
   * Creates a new API memory module object.
   *
   * @param module The wrapped internal memory module object.
   */
  // / @endcond
  public MemoryModule(
      final com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryModule module) {
    this.module = Preconditions.checkNotNull(module, "Error: Module argument can not be null");
  }

  @Override
  public com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryModule getNative() {
    return module;
  }

  // ! Base address of the module.
  /**
   * Returns the base address of the module.
   *
   * @return The base address of the module.
   */
  public Address getBaseAddress() {
    return new Address(module.getBaseAddress().getAddress().toLong());
  }

  // ! Name of the module.
  /**
   * Returns the name of the module.
   *
   * @return The name of the module.
   */
  public String getName() {
    return module.getName();
  }

  // ! Size of the module.
  /**
   * Returns the size of the module in bytes.
   *
   * @return The size of the module in bytes.
   */
  public long getSize() {
    return module.getSize();
  }
}
