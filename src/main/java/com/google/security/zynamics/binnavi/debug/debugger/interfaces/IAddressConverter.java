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
package com.google.security.zynamics.binnavi.debug.debugger.interfaces;

import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;

/**
 * Interface that must be implemented by all objects that want to convert addresses between file
 * base and image base.
 */
public interface IAddressConverter {
  /**
   * Converts a file address to a memory address.
   *
   * @param address The file address to convert.
   *
   * @return The converted memory address.
   */
  RelocatedAddress fileToMemory(UnrelocatedAddress address);

  /**
   * Converts a memory address to a file address.
   *
   * @param address The memory address to convert.
   *
   * @return The converted file address.
   */
  UnrelocatedAddress memoryToFile(RelocatedAddress address);
}
