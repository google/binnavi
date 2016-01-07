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

import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryMap;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemorySection;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.Pair;

import java.math.BigInteger;

/**
 * Helper class for calculating the ranges to request for a given memory range.
 */
public final class MemoryRangeCalculator {
  /**
   * You are not supposed to instantiate this class.
   */
  private MemoryRangeCalculator() {
  }

  /**
   * Calculates the range of memory to request from the debug client. For performance reasons this
   * range is different from the range specified by the user.
   *
   * @param offset The start offset of the range according to the user.
   * @param size The number of bytes to request according to the user.
   * @param sectionStart Beginning of the section the offset belongs to.
   * @param sectionEnd End of the section the offset belongs to.
   *
   * @return A pair that contains the real start offset and the real size information of the
   *         request.
   */
  public static Pair<IAddress, Integer> calculateRequestRange(final BigInteger offset,
      final int size, final IAddress sectionStart, final IAddress sectionEnd) {
    // To smoothen scrolling we try to load a range that is at max
    // +- 3 requested ranges.

    final BigInteger availableBefore = offset.subtract(sectionStart.toBigInteger());
    final BigInteger availableAfter =
        sectionEnd.toBigInteger().subtract(offset).add(BigInteger.ONE);

    final BigInteger loadBefore = availableBefore.compareTo(BigInteger.valueOf(3L * size)) == -1
        ? availableBefore : BigInteger.valueOf(3L * size);
    final BigInteger loadAfter = availableAfter.compareTo(BigInteger.valueOf(3L * size)) == -1
        ? availableAfter : BigInteger.valueOf(3L * size);

    final BigInteger realOffset = offset.subtract(loadBefore);
    final int realSize = (int) (loadBefore.add(loadAfter)).longValue();

    return new Pair<IAddress, Integer>(new CAddress(realOffset), realSize);
  }

  /**
   * Calculates the range of memory to request from the debug client. For performance reasons this
   * range is different than the range specified by the user.
   *
   * @param debugger The debugger from which the memory is loaded.
   * @param offset The start offset of the range according to the user.
   * @param size The number of bytes to request according to the user.
   *
   * @return A pair that contains the real start offset and the real size information of the
   *         request.
   */
  public static Pair<IAddress, Integer> calculateRequestRange(
      final IDebugger debugger, final BigInteger offset, final int size) {
    final MemoryMap mmap = debugger.getProcessManager().getMemoryMap();

    final MemorySection section = mmap.findOffset(offset);

    if (section == null) {
      return new Pair<IAddress, Integer>(new CAddress(offset), size);
    } else {
      return calculateRequestRange(offset, size, section.getStart(), section.getEnd());
    }
  }
}
