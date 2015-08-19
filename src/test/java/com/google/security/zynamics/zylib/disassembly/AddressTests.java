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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.math.BigInteger;

/**
 * @author cblichmann@google.com (Christian Blichmann)
 */
@RunWith(JUnit4.class)
public final class AddressTests {
  @Test
  public void testCompare() {
    // Values smaller than 2^63
    assertTrue(CAddress.compare(0x10000000L, 0x87654321L) < 0);
    assertTrue(CAddress.compare(0x10000000L, 0x10000000L) == 0);
    assertTrue(CAddress.compare(0x10000000L, 0x09999999L) > 0);

    // Test large values with sign-bits set
    assertTrue(CAddress.compare(0x8000000000000000L, 0x8000000010000000L) < 0);
    assertTrue(CAddress.compare(0x8000000000000000L, 0x8000000000000000L) == 0);
    assertTrue(CAddress.compare(0x8000000010000000L, 0x8000000000000000L) > 0);

    // Test large values where sign-bits differ
    assertTrue(CAddress.compare(0x7000000000000000L, 0x8000000010000000L) < 0);
    assertTrue(CAddress.compare(0x8000000010000000L, 0x7000000000000000L) > 0);
  }

  @Test
  public void testCompareToLarge() {
    // Test large values with sign-bits set
    final CAddress compareLarge = new CAddress(0x8000000010000000L);
    assertTrue(compareLarge.compareTo(0x8000000020000000L) < 0);
    assertTrue(compareLarge.compareTo(0x8000000010000000L) == 0);
    assertTrue(compareLarge.compareTo(0x8000000000000000L) > 0);
    assertTrue(compareLarge.compareTo(new CAddress(0x8000000020000000L)) < 0);
    assertTrue(compareLarge.compareTo(new CAddress(0x8000000010000000L)) == 0);
    assertTrue(compareLarge.compareTo(new CAddress(0x8000000000000000L)) > 0);

    // MockAddress is always 0x100, so we have mixed sign-bits
    assertFalse(compareLarge.compareTo(new MockAddress()) < 0);
    assertFalse(compareLarge.compareTo(new MockAddress()) == 0);
    assertTrue(compareLarge.compareTo(new MockAddress()) > 0);

    // Test large values where sign-bits differ
    assertTrue(new CAddress(0x7000000000000000L).compareTo(0x8000000010000000L) < 0);
    assertTrue(new CAddress(0x8000000010000000L).compareTo(0x7000000000000000L) > 0);
    assertTrue(new CAddress(0x7000000000000000L).compareTo(new CAddress(0x8000000010000000L)) < 0);
    assertTrue(new CAddress(0x8000000010000000L).compareTo(new CAddress(0x7000000000000000L)) > 0);
  }

  @Test
  public void testCompareToSmall() {
    // Values smaller than 2^63
    final CAddress compareSmall = new CAddress(0x10000000L);
    assertTrue(compareSmall.compareTo(0x87654321L) < 0);
    assertTrue(compareSmall.compareTo(0x10000000L) == 0);
    assertTrue(compareSmall.compareTo(0x09999999L) > 0);
    assertTrue(compareSmall.compareTo(new CAddress(0x87654321L)) < 0);
    assertTrue(compareSmall.compareTo(new CAddress(0x10000000L)) == 0);
    assertTrue(compareSmall.compareTo(new CAddress(0x09999999L)) > 0);

    // MockAddress is always 0x100
    assertFalse(compareSmall.compareTo(new MockAddress()) < 0);
    assertFalse(compareSmall.compareTo(new MockAddress()) == 0);
    assertTrue(compareSmall.compareTo(new MockAddress()) > 0);
  }

  @Test
  public void testConstruction() {
    final CAddress fromPrimitiveLong = new CAddress(0x87654321L);
    assertEquals(fromPrimitiveLong.toLong(), 0x87654321L);

    final IAddress anIAddress = new MockAddress();
    final CAddress fromIAddress = new CAddress(anIAddress);
    assertEquals(fromIAddress.toLong(), 0x100);

    final CAddress fromCAddress = new CAddress(new CAddress(0x87654321L));
    assertEquals(fromCAddress.toLong(), 0x87654321L);

    final CAddress fromBigInteger = new CAddress(new BigInteger("87654321", 16));
    assertEquals(fromBigInteger.toLong(), 0x87654321L);

    final CAddress fromString = new CAddress("87654321", 16);
    assertEquals(fromString.toLong(), 0x87654321L);
  }

  @Test
  public void testEquals() {
    final CAddress address = new CAddress(0x87654321L);
    assertFalse(address.equals(null));
    assertFalse(address.equals("SOMESTRING"));

    assertTrue(address.equals(0x87654321L));
    assertTrue(address.equals(new CAddress(0x87654321L)));

    final CAddress addressEqualsMock = new CAddress(0x100L);
    assertTrue(addressEqualsMock.equals(new MockAddress()));
  }

  @Test
  public void testToHexString() {
    // Test conversion
    assertTrue(new CAddress(0x876543abL).toHexString().equalsIgnoreCase("876543ab"));
    assertTrue(new CAddress(0xfedcba9876543210L).toHexString().equalsIgnoreCase("fedcba9876543210"));

    // Test left-padding with zeroes
    assertEquals(new CAddress(0x00000100L).toHexString().length(), 8);
    assertEquals(new CAddress(0x100000000L).toHexString().length(), 16);
  }

  @Test
  public void testToString() {
    // This test is to make sure toString doesn't diverge from
    // toHexString(). While not strictly necessary, some parts of BinNavi
    // and REIL rely on the assumption of the two methods always returning
    // the same result.

    final CAddress small = new CAddress(0x876543abL);
    assertEquals(small.toString(), small.toHexString());

    final CAddress larger = new CAddress(0x100000000L);
    assertEquals(larger.toString(), larger.toHexString());
  }
}
