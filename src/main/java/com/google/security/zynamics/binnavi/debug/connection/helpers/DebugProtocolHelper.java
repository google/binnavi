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
package com.google.security.zynamics.binnavi.debug.connection.helpers;

import com.google.security.zynamics.binnavi.Log.NaviLogger;
import com.google.security.zynamics.binnavi.debug.connection.interfaces.ClientReader;
import com.google.security.zynamics.binnavi.debug.connection.packets.arguments.DebugArgumentType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * This class provides a collection of helper classes used for reading values from a binary stream
 * into values used by the debug protocol classes.
 */
public final class DebugProtocolHelper {
  /**
   * You are not supposed to instantiate this class.
   */
  private DebugProtocolHelper() {}

  /**
   * Reads a number of bytes from an input stream.
   *
   * @param inputStream The input stream to read from.
   * @param length The number of bytes to read.
   *
   * @return The bytes that were read from the input stream.
   *
   * @throws IOException Thrown if the bytes could not be read.
   */
  private static byte[] readBytes(final ClientReader inputStream, final int length)
      throws IOException {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final byte[] data = new byte[length];

    int read = 0;

    while (read < length) {
      final int retval = inputStream.read(data, 0, length - read);
      baos.write(data, 0, retval);
      read += retval;
    }

    return baos.toByteArray();
  }

  /**
   * Reads a DWORD value from a buffered input stream and compares it to an expected value.
   *
   * @param inputStream The input stream from where the DWORD value is read.
   * @param expectedValue The expected value of the DWORD.
   *
   * @return The read DWORD value.
   *
   * @throws IOException Thrown if reading from the stream fails.
   */
  private static long readDWord(final ClientReader inputStream, final long expectedValue)
      throws IOException {
    final long value = readDWord(inputStream);

    if (value != expectedValue) {
      NaviLogger.severe("Error: Received value does not match expected value");
    }

    return value;
  }

  /**
   * Reads an address from an input stream.
   *
   * @param inputStream The input stream to read from.
   *
   * @return The address that was read from the input stream.
   *
   * @throws IOException Thrown if reading from the input stream fails.
   */
  public static long readAddress(final ClientReader inputStream) throws IOException {
    // read address from socket
    readDWord(inputStream, 8); // discard length
    readDWord(inputStream, DebugArgumentType.ADDRESS.getValue());

    final long addressHigh32 = readDWord(inputStream);
    final long addressLow32 = readDWord(inputStream);
    final long address =
        (addressLow32 & 0x00000000FFFFFFFFL) + (addressHigh32 << 32L & 0xFFFFFFFF00000000L);
    // mask out sign bit and add high bits

    return address;
  }

  /**
   * Reads a number of bytes from an input stream.
   *
   * @param inputStream The input stream to read from.
   *
   * @return The bytes that were read from the input stream.
   *
   * @throws IOException Thrown if reading from the stream fails.
   */
  public static byte[] readData(final ClientReader inputStream) throws IOException {
    final int length = (int) readDWord(inputStream);
    readDWord(inputStream, DebugArgumentType.DATA.getValue());

    return readBytes(inputStream, length);
  }

  /**
   * Reads a DWORD value from a buffered input stream.
   *
   * @param inputStream The input stream from where the DWORD value is read.
   *
   * @return The read DWORD value.
   *
   * @throws IOException Thrown if reading from the stream fails.
   */
  public static long readDWord(final ClientReader inputStream) throws IOException {
    long dword, readByte;
    readByte = inputStream.read();

    if (readByte == -1) {
      throw new IOException("End of input stream");
    }

    dword = (readByte & 0xFF) << 24 & 0xFF000000;
    readByte = inputStream.read();

    if (readByte == -1) {
      throw new IOException("End of input stream");
    }

    dword |= (readByte & 0xFF) << 16 & 0x00FF0000;
    readByte = inputStream.read();

    if (readByte == -1) {
      throw new IOException("End of input stream");
    }

    dword |= (readByte & 0xFF) << 8 & 0x0000FF00;
    readByte = inputStream.read();

    if (readByte == -1) {
      throw new IOException("End of input stream");
    }

    dword |= (readByte & 0xFF) & 0x000000FF;
    return dword;
  }

  /**
   * Reads a thread ID from a given input stream.
   *
   * @param inputStream The input stream to read from.
   *
   * @return The thread ID that was read from the input stream.
   *
   * @throws IOException Thrown if reading from the input stream fails.
   */
  public static long readThreadId(final ClientReader inputStream) throws IOException {
    // read TID from socket
    readDWord(inputStream, 4); // discard length
    readDWord(inputStream, DebugArgumentType.INTEGER.getValue());

    return readDWord(inputStream);
  }
}
