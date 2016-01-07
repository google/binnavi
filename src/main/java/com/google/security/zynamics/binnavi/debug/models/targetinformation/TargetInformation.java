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
package com.google.security.zynamics.binnavi.debug.models.targetinformation;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that contains information about the target architecture and the target process.
 */
public final class TargetInformation {
  /**
   * Size of addresses of the target platform in bytes.
   */
  private final int targetPlatformAddressByteSize;

  /**
   * Descriptions of the registers of the target architecture.
   */
  private final List<RegisterDescription> registerDescriptions;

  /**
   * Options supported by the connected debug client.
   */
  private final DebuggerOptions debuggerOptions;

  /**
   * Private constructor. Use parse() to create CTargetInformation objects.
   *
   * @param addressSize Size of addresses of the target platform in bytes.
   * @param registers Descriptions of the registers of the target architecture.
   * @param debuggerOptions Options supported by the connected debug client.
   *
   * @throws IllegalArgumentException Thrown if the address size argument is not positive or any of
   *         the arguments are null.
   */
  public TargetInformation(final int addressSize, final List<RegisterDescription> registers,
      final DebuggerOptions debuggerOptions) {
    Preconditions.checkArgument(addressSize > 0, "IE01039: Address size value must be positive");
    registerDescriptions = new ArrayList<>(
        Preconditions.checkNotNull(registers, "IE01303: Registers argument can not be null"));
    this.debuggerOptions = Preconditions.checkNotNull(debuggerOptions,
        "IE01306: Debugger options argument can not be null");
    targetPlatformAddressByteSize = addressSize;
  }

  /**
   * Returns the address size of the target platform.
   *
   * @return The address size of the target platform.
   */
  public int getAddressSize() {
    return targetPlatformAddressByteSize;
  }

  /**
   * Returns the debugger options object that describes the debug client.
   *
   * @return The debugger options object that describes the debug client.
   */
  public DebuggerOptions getDebuggerOptions() {
    return debuggerOptions;
  }

  /**
   * Returns the description of the registers of the target platform.
   *
   * @return The description of the registers of the target platform.
   */
  public List<RegisterDescription> getRegisters() {
    return new ArrayList<>(registerDescriptions);
  }
}
