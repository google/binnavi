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

import com.google.security.zynamics.binnavi.API.disassembly.Address;
import com.google.security.zynamics.binnavi.API.disassembly.Function;
import com.google.security.zynamics.binnavi.API.disassembly.Module;

/**
 * Represents an indirect call. This is a call instruction that has a variable call target.
 */
public final class IndirectCall {
  /**
   * The module the indirect call instruction belongs to.
   */
  private final Module module;

  /**
   * The function the indirect call instruction belongs to.
   */
  private final Function function;

  /**
   * The address of the indirect call instruction.
   */
  private final Address address;

  /**
   * Creates a new indirect call object.
   * 
   * @param module The module the indirect call instruction belongs to.
   * @param function The function the indirect call instruction belongs to.
   * @param address The address of the indirect call instruction.
   */
  public IndirectCall(final Module module, final Function function, final Address address) {
    this.module = module;
    this.function = function;
    this.address = address;
  }

  @Override
  public boolean equals(final Object rhs) {
    if (!(rhs instanceof IndirectCall)) {
      return false;
    }

    final IndirectCall rhsCall = (IndirectCall) rhs;

    return rhsCall.address.equals(address) && rhsCall.module.equals(module);
  }

  /**
   * Returns the address of the indirect call instruction.
   * 
   * @return The address of the indirect call instruction.
   */
  public Address getAddress() {
    return address;
  }

  /**
   * Returns the function the indirect call instruction belongs to.
   * 
   * @return The function the indirect call instruction belongs to.
   */
  public Function getFunction() {
    return function;
  }

  /**
   * Returns the module the indirect call instruction belongs to.
   * 
   * @return The module the indirect call instruction belongs to.
   */
  public Module getModule() {
    return module;
  }

  @Override
  public int hashCode() {
    return address.hashCode() * module.hashCode();
  }
}
