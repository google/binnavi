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
package com.google.security.zynamics.binnavi.debug.connection;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.debug.connection.interfaces.ClientReader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Reader class that is used for TCP/IP connections to the debug client.
 */
public final class SocketReader implements ClientReader {
  /**
   * Input stream that is used to read data from the debug client.
   */
  private final BufferedInputStream m_InputStream;

  /**
   * Creates a new socket reader object.
   *
   * @param socket The socket to read from.
   *
   * @throws IOException Thrown if the input stream of the socket can't be opened.
   */
  public SocketReader(final Socket socket) throws IOException {
    Preconditions.checkNotNull(socket, "IE00745: Socket can not be null");
    m_InputStream = new BufferedInputStream(socket.getInputStream());
  }

  /**
   * Returns the number of bytes that can be read from this input stream without blocking.
   *
   * @return The number of bytes that can be read from this input stream without blocking.
   *
   * @throws IOException If an I/O error occurs.
   */
  @Override
  public int available() throws IOException {
    return m_InputStream.available();
  }

  /**
   * Reads a single byte from the debug client.
   *
   * @throws IOException If an I/O error occurs.
   */
  @Override
  public int read() throws IOException {
    return m_InputStream.read();
  }

  /**
   * Reads a number of bytes from the debug client.
   *
   * @param data Destination buffer.
   * @param offset Offset at which to start storing bytes.
   * @param length Maximum number of bytes to read.
   *
   * @throws IOException If an I/O error occurs.
   */
  @Override
  public int read(final byte[] data, final int offset, final int length) throws IOException {
    return m_InputStream.read(data, offset, length);
  }
}
