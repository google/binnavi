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
package com.google.security.zynamics.binnavi.debug.connection.packets.parsers;

/**
 * Can be used to parse a textual memory string into a byte array.
 */
public final class MemoryStringParser {
  /**
   * Lookup table to maximize the performance of this operation.
   */
  private static int[] lookup1 = {
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0,
    1 << 4,
    2 << 4,
    3 << 4,
    4 << 4,
    5 << 4,
    6 << 4,
    7 << 4,
    8 << 4,
    9 << 4,
    0, 0, 0, 0, 0, 0, 0,
    10 << 4,
    11 << 4,
    12 << 4,
    13 << 4,
    14 << 4,
    15 << 4,
    16 << 4,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0,
    10 << 4,
    11 << 4,
    12 << 4,
    13 << 4,
    14 << 4,
    15 << 4,
    16 << 4,
    0, 0, 0, 0, 0, 0, 0, 0};

  /**
   * Lookup table to maximize the performance of this operation.
   */
  private static int[] lookup2 = {0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      2,
      3,
      4,
      5,
      6,
      7,
      8,
      9,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      10,
      11,
      12,
      13,
      14,
      15,
      16,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      10,
      11,
      12,
      13,
      14,
      15,
      16,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0};

  /**
   * You are not supposed to instantiate this class.
   */
  private MemoryStringParser() {}

  /**
   * Parses a memory string into a byte array.
   *
   * @param memoryString The memory string to parse.
   *
   * @return The byte array.
   */
  public static byte[] parseMemoryString(final String memoryString) {
    final byte[] memoryData = new byte[memoryString.length() / 2];

    for (int i = 0, j = 0; i < memoryString.length(); i += 2, j++) {
      memoryData[j] =
          (byte) (lookup1[memoryString.charAt(i)] + lookup2[memoryString.charAt(i + 1)]);
    }
    return memoryData;
  }
}
