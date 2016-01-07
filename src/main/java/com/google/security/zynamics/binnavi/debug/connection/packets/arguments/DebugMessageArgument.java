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
package com.google.security.zynamics.binnavi.debug.connection.packets.arguments;

import com.google.security.zynamics.zylib.strings.Commafier;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;


/**
 * Base class for all arguments that can be used in messages sent to the debug client. This class is
 * used to build debug command arguments on a high level and then to turn them into serialized byte
 * arrays that can be sent to the debug client.
 */
public class DebugMessageArgument {
  /**
   * Byte data of the message argument.
   */
  private final List<Byte> byteData = new ArrayList<>();

  /**
   * Creates a new debug message argument.
   *
   * @param type Argument identifier.
   */
  protected DebugMessageArgument(final DebugArgumentType type) {
    // Append the identifier at the beginning of the argument data.
    appendInt(type.getValue());
  }

  /**
   * Prepends an integer number to the beginning of a message argument.
   *
   * @param list The list in question.
   * @param number The number to add.
   */
  private static void prependInteger(final List<Byte> list, final int number) {
    list.add(0, (byte) (number & 0xFF));
    list.add(0, (byte) (number >> 8 & 0xFF));
    list.add(0, (byte) (number >> 16 & 0xFF));
    list.add(0, (byte) (number >> 24 & 0xFF));
  }

  /**
   * Appends a byte array to the argument.
   *
   * @param data The data to append to the argument.
   */
  protected final void appendBytes(final byte[] data) {
    for (final byte element : data) {
      byteData.add(element);
    }
  }

  /**
   * Adds an integer number to the message argument.
   *
   * @param number The number to add.
   */
  protected final void appendInt(final int number) {
    for (int i = 3; i >= 0; --i) {
      byteData.add(Byte.valueOf((byte) (number >> 8 * i & 0xFF)));
    }
  }

  /**
   * Adds a long number to the message argument.
   *
   * @param number The number to add.
   */
  protected final void appendLong(final BigInteger number) {
    for (int i = 7; i >= 0; --i) {
      byteData.add(Byte.valueOf(
          (byte) (number.shiftRight(8 * i).and(BigInteger.valueOf(0xFF)).longValue())));
    }
  }

  /**
   * Returns the byte representation of the debug message argument. This byte representation can be
   * sent directly to the debug client.
   *
   * @return The byte representation of the debug message argument.
   */
  public final List<Byte> getBytes() {
    final List<Byte> list = new ArrayList<>(byteData);

    // Prepend the size of the additional argument data.
    prependInteger(list, list.size() - 4);

    return list;
  }

  @Override
  public String toString() {
    return Commafier.commafy(getBytes(), " | ");
  }
}
