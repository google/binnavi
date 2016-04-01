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
package com.google.security.zynamics.reil.interpreter;

import java.math.BigInteger;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.general.memmanager.Memory;

/**
 * Simulates memory used by the REIL interpreter.
 */
public class ReilMemory {

  private final Memory memory = new Memory();

  /**
   * Endianness of the memory
   */
  final Endianness endianness;

  /**
   * Creates a new simulated REIL memory object with the given endianness.
   * 
   * @param endianness The memory layout of the memory
   */
  public ReilMemory(final Endianness endianness) {
    this.endianness =
        Preconditions.checkNotNull(endianness, "Error: Argument endianness can't be null");
  }

  private byte[] getData(final long value, final int length) {
    if (endianness == Endianness.LITTLE_ENDIAN) {
      return getDataLittle(value, length);
    } else {
      return getDataBig(value, length);
    }
  }

  private byte[] getDataBig(final long value, final int length) {

    if (length > 8 || length <= 0)
      throw new IllegalArgumentException("Invalid data length");
    byte result[] = new byte[length];
    long mask = 1;
    for (int i = 0; i < length; i++) {
      result[length - 1 - i] = (byte) ((value & 0xFFL * mask) >> 8 * i);
      mask *= 0x100;
    }

    return result;
  }

  private byte[] getDataLittle(final long value, final int length) {

    if (length > 8 || length <= 0)
      throw new IllegalArgumentException("Invalid data length");
    byte result[] = new byte[length];
    long mask = 1;
    for (int i = 0; i < length; i++) {
      result[i] = (byte) ((value & 0xFFL * mask) >> 8 * i);
      mask *= 0x100;
    }

    return result;
  }

  private BigInteger loadBig(final byte[] data) {
   switch (data.length) {
      case 1:
        return BigInteger.valueOf(data[0] & 0xFF);
      case 2:
        return BigInteger.valueOf((data[1] & 0xFF) + ((data[0] & 0xFF) * 0x100));
      case 4:
        return BigInteger.valueOf((data[3] & 0xFF) + ((data[2] & 0xFF) * 0x100) + ((data[1] & 0xFF) * 0x100L * 0x100)
            + ((data[0] & 0xFF) * 0x100L * 0x100 * 0x100));
      case 8:
        return BigInteger.valueOf(
            (data[7] & 0xFF) 
            + ((data[6] & 0xFF) * 0x100) 
            + ((data[5] & 0xFF) * 0x100L * 0x100)
            + ((data[4] & 0xFF) * 0x100L * 0x100 * 0x100) 
            + ((data[3] & 0xFF) * 0x100L * 0x100 * 0x100 * 0x100)
            + ((data[2] & 0xFF) * 0x100L * 0x100 * 0x100 * 0x100 * 0x100) 
            + ((data[1] & 0xFF) * 0x100L * 0x100 * 0x100 * 0x100 * 0x100 * 0x100))
            .add(BigInteger.valueOf(data[0] & 0xFF).shiftLeft(56));
      default:
        throw new IllegalArgumentException("Not yet implemented");
    }
  }

  private BigInteger loadLittle(final byte[] data) {
    switch (data.length) {
      case 1:
        return BigInteger.valueOf(data[0] & 0xFF);
      case 2:
        return BigInteger.valueOf((data[0] & 0xFF) + ((data[1] & 0xFF) * 0x100));
      case 3:
        return BigInteger.valueOf((data[0] & 0xFF) + ((data[1] & 0xFF) * 0x100) + ((data[2] & 0xFF) * 0x100 * 0x100));
      case 4:
        return BigInteger.valueOf((data[0] & 0xFF) + ((data[1] & 0xFF) * 0x100) + ((data[2] & 0xFF) * 0x100L * 0x100)
            + ((data[3] & 0xFF) * 0x100L * 0x100 * 0x100));
      case 8:
        BigInteger t = BigInteger
            .valueOf((data[0] & 0xFF) + ((data[1] & 0xFF) * 0x100) + ((data[2] & 0xFF) * 0x100 * 0x100)
                + ((data[3] & 0xFF) * 0x100L * 0x100 * 0x100) + ((data[4] & 0xFF) * 0x100L * 0x100 * 0x100 * 0x100)
                + ((data[5] & 0xFF) * 0x100L * 0x100 * 0x100 * 0x100 * 0x100)
                + ((data[6] & 0xFF) * 0x100L * 0x100 * 0x100 * 0x100 * 0x100 * 0x100))
            .add(BigInteger.valueOf(data[7] & 0xFF).shiftLeft(56));
        return t;
      default:
        throw new IllegalArgumentException("Not yet implemented");
    }
  }

  /**
   * Returns the total amount of allocated memory.
   * 
   * @return The total amount of allocated memory
   */
  public int getAllocatedMemory() {
    return memory.getMemorySize();
  }

  /**
   * Loads a value from memory
   * 
   * @param address The address of the value (must be >= 0)
   * @param length Length of the value
   * 
   * @return The value
   */
  public BigInteger load(final long address, final int length) {
    Preconditions.checkArgument(address >= 0, "Error: Argument address can't be less than 0");
    Preconditions.checkArgument(length > 0, "Error: Argument length must be bigger than 0");

    if (endianness == Endianness.LITTLE_ENDIAN) {
      return loadLittle(memory.getData(address, length));
    } else {
      return loadBig(memory.getData(address, length));
    }
  }

  /**
   * Prints the contents of the REIL memory.
   * 
   */
  public void printMemory() {
    memory.printMemory();
  }

  /**
   * Stores a value in memory
   * 
   * @param address Address where the value is stored (must be >= 0)
   * @param value Value to store
   * @param length Length of the value
   */
  public void store(final long address, final long value, final int length) {
    Preconditions.checkArgument(address >= 0, "Error: Argument address can't be less than 0");
    Preconditions.checkArgument(length > 0, "Error: Argument length must be bigger than 0");
    memory.store(address, getData(value, length));
  }
}
