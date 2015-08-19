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
package com.google.security.zynamics.zylib.general;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;

@RunWith(JUnit4.class)
public final class ConvertTests {
  @Test
  public void testHexStringToAsciiString() {
    assertEquals("", Convert.hexStringToAsciiString(""));
    assertEquals("0123456789", Convert.hexStringToAsciiString("30313233343536373839"));
    assertEquals("AbCdEf", Convert.hexStringToAsciiString("416243644566"));
    assertEquals("89AB", Convert.hexStringToAsciiString("38394142"));
    assertEquals("....", Convert.hexStringToAsciiString("3839413"));
  }

  @Test
  public void testHexStringToBytes() {
    assertTrue(Arrays.equals(new byte[] {0x38, 0x39, 0x41, 0x42},
        Convert.hexStringToBytes("38394142")));
  }
}
