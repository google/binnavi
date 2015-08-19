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
package com.google.security.zynamics.reil.translators;

import static org.junit.Assert.assertEquals;

import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.translators.TranslationHelpers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TranslationHelpersTest {
  @Test
  public void testZeroMaskHighestBitBYTE() {
    final long mask = TranslationHelpers.generateZeroMask(7, 1, OperandSize.BYTE);
    assertEquals(0x7FL, mask);
  }

  @Test
  public void testZeroMaskHighestBitDWORD() {
    final long mask = TranslationHelpers.generateZeroMask(31, 1, OperandSize.DWORD);
    assertEquals(0x7FFFFFFFL, mask);
  }

  @Test
  public void testZeroMaskHighestBitQWORD() {
    final long mask = TranslationHelpers.generateZeroMask(63, 1, OperandSize.QWORD);
    assertEquals(0x7FFFFFFFFFFFFFFFL, mask);
  }

  @Test
  public void testZeroMaskHighestBitWORD() {
    final long mask = TranslationHelpers.generateZeroMask(15, 1, OperandSize.WORD);
    assertEquals(0x7FFFL, mask);
  }

  @Test
  public void testZeroMaskLowestBitDWORD() {
    final long mask = TranslationHelpers.generateZeroMask(0, 1, OperandSize.DWORD);
    assertEquals(0xFFFFFFFEL, mask);
  }

  @Test
  public void testZeroMaskSomeBits1() {
    final long mask = TranslationHelpers.generateZeroMask(16, 16, OperandSize.DWORD);
    assertEquals(0x0000FFFFL, mask);
  }

  @Test
  public void testZeroMaskSomeBits2() {
    final long mask = TranslationHelpers.generateZeroMask(0, 16, OperandSize.DWORD);
    assertEquals(0xFFFF0000L, mask);
  }

  @Test
  public void testZeroMaskSomeBits3() {
    final long mask = TranslationHelpers.generateZeroMask(8, 16, OperandSize.DWORD);
    assertEquals(0xFF0000FFL, mask);
  }

  @Test
  public void testZeroMaskSomeBits4() {
    final long mask = TranslationHelpers.generateZeroMask(1, 30, OperandSize.DWORD);
    assertEquals(0x80000001L, mask);
  }
}
