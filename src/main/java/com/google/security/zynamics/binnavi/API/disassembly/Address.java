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
package com.google.security.zynamics.binnavi.API.disassembly;

import java.math.BigInteger;

// / Represents addresses throughout the plugin API.
/**
 * Represents an address.
 */
public final class Address {
  /**
   * Big integer value of the address.
   */
  private final BigInteger m_value;

  // / @cond INTERNAL
  /**
   * Creates a new address object.
   * 
   * @param value Big integer value of the object.
   */
  // / @endcond
  public Address(final BigInteger value) {
    m_value = value;
  }

  // ! Creates a new address.
  /**
   * Creates a new address object.
   * 
   * @param value The long value of the address.
   */
  public Address(final long value) {
    m_value = BigInteger.valueOf(value);
  }

  /**
   * Returns the BigInteger value of a given object.
   * 
   * @param rhs The object to convert to a big integer.
   * 
   * @return The big integer value of the given object.
   */
  private static BigInteger getBigInteger(final Object rhs) {
    if (rhs instanceof Integer) {
      final Integer irhs = (Integer) rhs;

      return BigInteger.valueOf(irhs.intValue());
    } else if (rhs instanceof BigInteger) {
      final BigInteger irhs = (BigInteger) rhs;

      return irhs;
    } else if (rhs instanceof Address) {
      final Address irhs = (Address) rhs;

      return irhs.m_value;
    } else {
      throw new IllegalArgumentException("Error: Can not convert '" + rhs + "' to a numeric value");
    }
  }

  // / @cond INTERNAL

  /**
   * Used to support ADD operations on addresses in Python scripts.
   * 
   * @param rhs The value to add to the address.
   * 
   * @return The resulting address value.
   */
  public Address __add__(final Object rhs) {
    return new Address(m_value.add(getBigInteger(rhs)));
  }

  /**
   * Used to support AND operations on addresses in Python scripts.
   * 
   * @param rhs The value to and with the address.
   * 
   * @return The resulting address value.
   */
  public Address __and__(final Object rhs) {
    return new Address(m_value.and(getBigInteger(rhs)));
  }

  /**
   * Used to support == operations on addresses in Python scripts.
   * 
   * @param rhs The value to compare with the address.
   * 
   * @return The result of the comparison.
   */
  public boolean __eq__(final Object rhs) {
    return getBigInteger(this).equals(getBigInteger(rhs));
  }

  /**
   * Used to support >= operations on addresses in Python scripts.
   * 
   * @param rhs The value to compare with the address.
   * 
   * @return The result of the comparison.
   */
  public boolean __ge__(final Object rhs) {
    return getBigInteger(this).compareTo(getBigInteger(rhs)) >= 0;
  }

  /**
   * Used to support > operations on addresses in Python scripts.
   * 
   * @param rhs The value to compare with the address.
   * 
   * @return The result of the comparison.
   */
  public boolean __gt__(final Object rhs) {
    return getBigInteger(this).compareTo(getBigInteger(rhs)) > 0;
  }

  /**
   * Used to support <= operations on addresses in Python scripts.
   * 
   * @param rhs The value to compare with the address.
   * 
   * @return The result of the comparison.
   */
  public boolean __le__(final Object rhs) {
    return getBigInteger(this).compareTo(getBigInteger(rhs)) <= 0;
  }

  /**
   * Returns the long value of the address in Python scripts.
   * 
   * @return The long value of the address.
   */
  public long __long__() {
    return m_value.longValue();
  }

  /**
   * Used to support << operations on addresses in Python scripts.
   * 
   * @param rhs The shift distance.
   * 
   * @return The resulting address value.
   */
  public Address __lshift__(final Object rhs) {
    return new Address(m_value.shiftLeft(getBigInteger(rhs).intValue()));
  }

  /**
   * Used to support < operations on addresses in Python scripts.
   * 
   * @param rhs The value to compare with the address.
   * 
   * @return The result of the comparison.
   */
  public boolean __lt__(final Object rhs) {
    return getBigInteger(this).compareTo(getBigInteger(rhs)) < 0;
  }

  /**
   * Used to support MUL operations on addresses in Python scripts.
   * 
   * @param rhs The value to multiply with the address.
   * 
   * @return The resulting address value.
   */
  public Address __mul__(final Object rhs) {
    return new Address(m_value.multiply(getBigInteger(rhs)));
  }

  /**
   * Used to support != operations on addresses in Python scripts.
   * 
   * @param rhs The value to compare with the address.
   * 
   * @return The result of the comparison.
   */
  public boolean __ne__(final Object rhs) {
    return !getBigInteger(this).equals(getBigInteger(rhs));
  }

  /**
   * Used to support OR operations on addresses in Python scripts.
   * 
   * @param rhs The value to OR with the address.
   * 
   * @return The resulting address value.
   */
  public Address __or__(final Object rhs) {
    return new Address(m_value.or(getBigInteger(rhs)));
  }

  /**
   * Used to support reverse ADD operations on addresses in Python scripts.
   * 
   * @param rhs The value to add to the address.
   * 
   * @return The resulting address value.
   */
  public Address __radd__(final Object rhs) {
    return new Address(m_value.add(getBigInteger(rhs)));
  }

  /**
   * Used to support reverse AND operations on addresses in Python scripts.
   * 
   * @param rhs The value to and with the address.
   * 
   * @return The resulting address value.
   */
  public Address __rand__(final Object rhs) {
    return new Address(m_value.and(getBigInteger(rhs)));
  }

  /**
   * Used to support reverse MUL operations on addresses in Python scripts.
   * 
   * @param rhs The value to multiply with the address.
   * 
   * @return The resulting address value.
   */
  public Address __rmul__(final Object rhs) {
    return new Address(m_value.multiply(getBigInteger(rhs)));
  }

  /**
   * Used to support reverse OR operations on addresses in Python scripts.
   * 
   * @param rhs The value to OR with the address.
   * 
   * @return The resulting address value.
   */
  public Address __ror__(final Object rhs) {
    return new Address(m_value.or(getBigInteger(rhs)));
  }

  /**
   * Used to support >> operations on addresses in Python scripts.
   * 
   * @param rhs The shift distance.
   * 
   * @return The resulting address value.
   */
  public Address __rshift__(final Object rhs) {
    return new Address(m_value.shiftRight(getBigInteger(rhs).intValue()));
  }

  /**
   * Used to support reverse SUB operations on addresses in Python scripts.
   * 
   * @param rhs The value to subtract the address from.
   * 
   * @return The resulting address value.
   */
  public Address __rsub__(final Object rhs) {
    return new Address(getBigInteger(rhs).subtract(m_value));
  }

  /**
   * Used to support reverse XOR operations on addresses in Python scripts.
   * 
   * @param rhs The value to XOR the address with.
   * 
   * @return The resulting address value.
   */
  public Address __rxor__(final Object rhs) {
    return new Address(m_value.xor(getBigInteger(rhs)));
  }

  /**
   * Used to support SUB operations on addresses in Python scripts.
   * 
   * @param rhs The value to subtract from the address.
   * 
   * @return The resulting address value.
   */
  public Address __sub__(final Object rhs) {
    return new Address(m_value.subtract(getBigInteger(rhs)));
  }

  /**
   * Used to support XOR operations on addresses in Python scripts.
   * 
   * @param rhs The value to XOR with the address.
   * 
   * @return The resulting address value.
   */
  // / @endcond
  public Address __xor__(final Object rhs) {
    return new Address(m_value.xor(getBigInteger(rhs)));
  }

  // ! Compares two address objects.
  /**
   * Compares the address to another object.
   * 
   * @param rhs The object to compare the address to.
   * 
   * @return True, if the address is compared to another address object with the same long value.
   *         False, otherwise.
   */
  @Override
  public boolean equals(final Object rhs) {
    if (!(rhs instanceof Address)) {
      return false;
    }

    return m_value.equals(((Address) rhs).m_value);
  }

  @Override
  public int hashCode() {
    return m_value.hashCode();
  }

  // ! Hexadecimal representation of the address.
  /**
   * Returns the hexadecimal string representation of the address.
   * 
   * @return A hex string of the address.
   */
  public String toHexString() {
    return m_value.toString(16);
  }

  // ! Long representation of the address.
  /**
   * Returns the long value of the address.
   * 
   * @return The long value of the address.
   */
  public long toLong() {
    return m_value.longValue();
  }

  // ! Printable representation of the address.
  /**
   * Converts the address to a printable string.
   * 
   * @return The address as printable string.
   */
  @Override
  public String toString() {
    return toHexString();
  }
}
