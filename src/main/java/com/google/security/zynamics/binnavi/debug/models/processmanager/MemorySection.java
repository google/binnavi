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

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.disassembly.IAddress;

/**
 * Describes a section of accessible memory in the target process. Each byte of the section is
 * assumed to be readable.
 */
public final class MemorySection {
  /**
   * Start offset of the memory section.
   */
  private final IAddress memorySectionStartAddress;

  /**
   * Ends offset of the memory section.
   */
  private final IAddress memorySectionEndAddress;

  /**
   * Creates a new memory section description object.
   *
   * @param start The start offset of the memory section.
   * @param end The last offset of the memory section.
   *
   * @throws IllegalArgumentException Thrown if either parameter is negative or end <= start.
   */
  public MemorySection(final IAddress start, final IAddress end) {
    memorySectionStartAddress =
        Preconditions.checkNotNull(start, "IE00753: Section start can not be null");
    memorySectionEndAddress =
        Preconditions.checkNotNull(end, "IE00754: Section end can not be null");
    Preconditions.checkArgument(end.toBigInteger().compareTo(start.toBigInteger()) != -1,
        "IE00755: Section end can not come before section start");
  }

  /**
   * Returns the last offset that belongs to the memory section.
   *
   * @return The last offset that belongs to the memory section.
   */
  public IAddress getEnd() {
    return memorySectionEndAddress;
  }

  /**
   * Returns the size of the memory section in bytes.
   *
   * @return The size of the memory section.
   */
  public int getSize() {
    return (int) (memorySectionEndAddress.toLong() - memorySectionStartAddress.toLong()) + 1;
  }

  /**
   * Returns the first offset that belongs to the memory section.
   *
   * @return The first offset that belongs to the memory section.
   */
  public IAddress getStart() {
    return memorySectionStartAddress;
  }

  /**
   * Turns the memory section description into a readable string.
   *
   * @return The memory section description as a readable string.
   */
  @Override
  public String toString() {
    // TODO: Not fit for 64bit
    return String.format("%s - %s", memorySectionStartAddress, memorySectionEndAddress);
  }
}
