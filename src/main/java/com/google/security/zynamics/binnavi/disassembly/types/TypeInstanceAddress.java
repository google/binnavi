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
package com.google.security.zynamics.binnavi.disassembly.types;

import java.math.BigInteger;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import java.util.Objects;

/**
 * Describes the address of a {@link TypeInstance} within a {@link Section},
 * i.e. an address consisting of a base address plus an offset.
 */
public class TypeInstanceAddress implements Comparable<TypeInstanceAddress> {

  private final IAddress baseAddress;
  private final long offset;
  private final long virtualAddress;

  public TypeInstanceAddress(final IAddress address, final long offset) {
    this.baseAddress = Preconditions.checkNotNull(address);
    this.offset = offset;
    this.virtualAddress = address.toBigInteger().add(BigInteger.valueOf(offset)).longValue();
  }

  @Override
  public int compareTo(final TypeInstanceAddress rhs) {
    final int result = getBaseAddress().compareTo(rhs.getBaseAddress());
    if (result == 0) {
      return (int) (offset - rhs.getOffset());
    } else {
      return result;
    }
  }

  @Override
  public boolean equals(final Object o) {
    if (!(o instanceof TypeInstanceAddress)) {
      return false;
    }
    final TypeInstanceAddress instanceAddress = (TypeInstanceAddress) o;
    return Objects.equals(offset, instanceAddress.offset)
        && Objects.equals(baseAddress, instanceAddress.baseAddress);
  }

  /**
   * Returns the base address.
   *
   * @return The base address.
   */
  public IAddress getBaseAddress() {
    return baseAddress;
  }

  /**
   * Returns the offset relative to the base address.
   *
   * @return The offset relative to the base address.
   */
  public long getOffset() {
    return offset;
  }

  /**
   * Returns the actual virtual address described by this instance, i.e. the
   * address of the section plus the section offset.
   *
   * @return The actual virtual address of the type instance.
   */
  public long getVirtualAddress() {
    return virtualAddress;
  }

  @Override
  public int hashCode() {
    return Objects.hash(baseAddress, offset);
  }
}