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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Describes the memory regions of the target process.
 */
public final class MemoryMap implements Iterable<MemorySection> {
  /**
   * List of readable memory sections in the target process.
   */
  private final List<MemorySection> memorySections;

  /**
   * Creates a new memory map object.
   *
   * @param sections List of readable memory sections in the target process.
   */
  public MemoryMap(final List<MemorySection> sections) {
    Preconditions.checkNotNull(sections, "IE00747: Section list can not be null");
    for (final MemorySection section : sections) {
      Preconditions.checkNotNull(section, "IE00748: Section can not be null");
    }
    // TODO: Make sure that the sections don't intersect.
    memorySections = new ArrayList<>(sections);
  }

  /**
   * Searches for the memory section that contains a given offset.
   *
   * @param offset The offset to search for.
   *
   * @return The memory section with the given offset or null if no such section exists.
   */
  public MemorySection findOffset(final BigInteger offset) {
    Preconditions.checkNotNull(offset, "IE00749: Offset argument can not be null");
    for (final MemorySection section : memorySections) {
      if ((section.getStart().toBigInteger().compareTo(offset) <= 0)
          && (section.getEnd().toBigInteger().compareTo(offset) >= 0)) {
        return section;
      }
    }
    return null;
  }

  /**
   * Returns the number of memory sections in the memory map.
   *
   * @return The number of memory sections in the memory map.
   */
  public int getNumberOfSections() {
    return memorySections.size();
  }

  /**
   * Returns the memory sections with the given index from the memory map.
   *
   * @param index The index of the memory section.
   *
   * @return The memory section at the specified index.
   */
  public MemorySection getSection(final int index) {
    Preconditions.checkArgument((index >= 0) && (index < memorySections.size()),
        "IE00750: Invalid section index");
    return memorySections.get(index);
  }

  @Override
  public Iterator<MemorySection> iterator() {
    return memorySections.iterator();
  }
}
