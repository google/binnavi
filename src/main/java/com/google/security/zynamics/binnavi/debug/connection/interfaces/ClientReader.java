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
package com.google.security.zynamics.binnavi.debug.connection.interfaces;

import java.io.IOException;

/**
 * Interface that must be implemented by all classes that want to read from the debug client.
 */
public interface ClientReader {
  /**
   * Returns the number of bytes that can be read from this input stream without blocking.
   *
   * @return The number of bytes that can be read from this input stream without blocking.
   *
   * @throws IOException If an I/O error occurs.
   */
  int available() throws IOException;

  /**
   * Reads a single byte from the debug client.
   *
   * @return The read byte.
   *
   * @throws IOException If an I/O error occurs.
   */
  int read() throws IOException;

  /**
   * Reads a number of bytes from the debug client.
   *
   * @param data Destination buffer.
   * @param offset Offset at which to start storing bytes.
   * @param length Maximum number of bytes to read.
   *
   * @return The number of bytes read, or -1 if the end of the stream has been reached.
   *
   * @throws IOException If an I/O error occurs.
   */
  int read(byte[] data, int offset, int length) throws IOException;
}
