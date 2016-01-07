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
package com.google.security.zynamics.binnavi.debug.models.processmanager;

import com.google.security.zynamics.zylib.disassembly.IAddress;

/**
 * Contains helper classes for working with processes.
 */
public final class ProcessHelpers {
  /**
   * You are not supposed to instantiate this class.
   */
  private ProcessHelpers() {}

  /**
   * Returns the memory section of a memory map that contains a given address.
   *
   * @param map The map to search through.
   * @param address The address to look for.
   *
   * @return The memory section with the given address or null if there is no such section.
   */
  public static MemorySection getSectionWith(final MemoryMap map, final IAddress address) {
    for (final MemorySection section : map) {
      if (address.toLong() >= section.getStart().toLong()
          && address.toLong() < section.getEnd().toLong()) {
        return section;
      }
    }
    return null;
  }
}
