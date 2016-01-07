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

import com.google.security.zynamics.binnavi.API.debug.MemoryModule;
import com.google.security.zynamics.binnavi.API.disassembly.Address;
import com.google.security.zynamics.binnavi.API.disassembly.Function;

/**
 * Represents a resolved function. This is the target function that was called by a dynamic function
 * call.
 */
public final class ResolvedFunction {
  /**
   * The address of the resolved function. This attribute is null if the function attribute is not
   * null.
   */
  private final Address address;

  /**
   * The function object of the resolved function.
   * 
   * This value is optional because not all functions are necessarily known can be resolved.
   */
  private final Function function;

  /**
   * The memory module of the resolved function.
   * 
   * This value is optional because not all executed functions necessarily lie in proper memory
   * modules.
   */
  private final MemoryModule memoryModule;

  /**
   * Creates a new function object if only the address of the resolved function could be determined.
   * 
   * @param address The address of the resolved function.
   */
  public ResolvedFunction(final Address address) {
    this.address = address;
    this.function = null;
    this.memoryModule = null;
  }

  /**
   * Creates a new function object if the function could be resolved completely.
   * 
   * @param function The function object of the resolved function. This argument can be null.
   */
  public ResolvedFunction(final Function function) {
    assert function != null;

    this.address = function.getAddress();
    this.function = function;
    this.memoryModule = null;
  }

  /**
   * Creates a new function object if the memory module and the function address could be resolved.
   * 
   * @param memoryModule The memory module the address belongs to.
   * @param address The resolved function address.
   */
  public ResolvedFunction(final MemoryModule memoryModule, final Address address) {
    assert address != null;
    assert memoryModule != null;

    this.address = address;
    this.function = null;
    this.memoryModule = memoryModule;
  }

  @Override
  public boolean equals(final Object rhs) {
    return (rhs instanceof ResolvedFunction) && ((ResolvedFunction) rhs).address.equals(address);
  }

  /**
   * Returns the address of the resolved function.
   * 
   * @return The address of the resolved function.
   */
  public Address getAddress() {
    return address;
  }

  /**
   * Returns the function object of the resolved function.
   * 
   * @return The function object of the resolved function or null if the resolved function is
   *         unknown.
   */
  public Function getFunction() {
    return function;
  }

  /**
   * Returns the memory module the resolved address belongs to.
   * 
   * @return The memory module the resolved address belongs to or null.
   */
  public MemoryModule getMemoryModule() {
    return memoryModule;
  }

  @Override
  public int hashCode() {
    return address.hashCode();
  }
}
