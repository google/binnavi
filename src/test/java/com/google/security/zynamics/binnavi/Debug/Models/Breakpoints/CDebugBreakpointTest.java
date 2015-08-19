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
package com.google.security.zynamics.binnavi.Debug.Models.Breakpoints;

import static org.junit.Assert.assertEquals;

import com.google.security.zynamics.binnavi.Common.CommonTestObjects;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.Breakpoint;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointType;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.math.BigInteger;


/**
 * Test class for the breakpoint information class.
 */
@RunWith(JUnit4.class)
public final class CDebugBreakpointTest {
  /**
   * Tests whether all getter and setter methods work properly for regular breakpoints.
   */
  @Test
  public void testRegularBreakpoint() {
    final Breakpoint bp =
        new Breakpoint(BreakpointType.REGULAR, CommonTestObjects.BP_ADDRESS_123);

    // Test whether the constructor worked properly.
    assertEquals(BreakpointType.REGULAR, bp.getType());
    assertEquals(null, bp.getDescription());
    assertEquals(BigInteger.valueOf(0x123), bp.getAddress().getAddress().getAddress()
        .toBigInteger());

    assertEquals(BreakpointType.REGULAR, bp.getType());
    assertEquals(null, bp.getDescription());
    assertEquals(BigInteger.valueOf(0x123), bp.getAddress().getAddress().getAddress()
        .toBigInteger());

    assertEquals(BreakpointType.REGULAR, bp.getType());
    assertEquals(null, bp.getDescription());
    assertEquals(BigInteger.valueOf(0x123), bp.getAddress().getAddress().getAddress()
        .toBigInteger());

    // Test the comment setter method.
    bp.setDescription("foo");

    assertEquals(BreakpointType.REGULAR, bp.getType());
    assertEquals("foo", bp.getDescription());
    assertEquals(BigInteger.valueOf(0x123), bp.getAddress().getAddress().getAddress()
        .toBigInteger());
  }
}
