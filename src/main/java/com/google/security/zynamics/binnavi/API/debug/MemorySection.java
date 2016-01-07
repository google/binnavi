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

import com.google.security.zynamics.binnavi.API.disassembly.Address;

// / Single allocated section of the target memory.
/**
 * Describes a single allocated memory section of the target process memory.
 */
public final class MemorySection {
  /**
   * The wrapped internal memory section object.
   */
  private final com.google.security.zynamics.binnavi.debug.models.processmanager.MemorySection
      section;

  // / @cond INTERNAL
  /**
   * Creates a new API memory section object.
   *
   * @param section The wrapped internal memory section object.
   */
  // / @endcond
  public MemorySection(
      final com.google.security.zynamics.binnavi.debug.models.processmanager.MemorySection section) {
    this.section = section;
  }

  // ! Last offset of the section.
  /**
   * Returns the last offset of the memory section.
   *
   * @return The last offset of the memory section.
   */
  public Address getEnd() {
    return new Address(section.getEnd().toBigInteger());
  }

  // ! First offset of the section.
  /**
   * Returns the first offset of the memory section.
   *
   * @return The first offset of the memory section.
   */
  public Address getStart() {
    return new Address(section.getStart().toBigInteger());
  }

  // ! Printable representation of the memory section.
  /**
   * Returns a string representation of the memory section.
   *
   * @return A string representation of the memory section.
   */
  @Override
  public String toString() {
    return String.format("Memory Section [%s - %s]", getStart().toHexString(),
        getEnd().toHexString());
  }
}
