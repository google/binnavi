/*
Copyright 2014 Google Inc. All Rights Reserved.

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
package com.google.security.zynamics.zylib.disassembly;

import java.math.BigInteger;

public class MockAddress implements IAddress {
  private final BigInteger address;

  public MockAddress() {
    this(0x100);
  }

  public MockAddress(final long address) {
    this.address = BigInteger.valueOf(address);
  }

  @Override
  public int compareTo(final IAddress o) {
    return this.address.compareTo(o.toBigInteger());
  }

  @Override
  public boolean equals(final Object object) {
    return this.equals(object);
  }

  @Override
  public BigInteger toBigInteger() {
    return address;
  }

  @Override
  public String toHexString() {
    return String.format("%08X", address);
  }

  @Override
  public long toLong() {
    return address.longValue();
  }

  @Override
  public String toString() {
    return toHexString();
  }
}
