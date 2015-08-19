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
package com.google.security.zynamics.binnavi.Debug.Connection;

import java.io.IOException;

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.debug.connection.interfaces.ClientReader;


public final class CMockReader implements ClientReader {
  private int m_msgcounter = -1;

  private int m_bytecounter = 0;

  private byte[][] m_messages = new byte[0][0];

  private boolean m_doThrow;

  public CMockReader() {
  }

  public CMockReader(final boolean doThrow) {
    m_doThrow = doThrow;
  }

  public CMockReader(final byte[][] data) {
    m_messages = data.clone();
  }

  @Override
  public int available() {
    if ((m_msgcounter == -1) || (m_msgcounter > m_messages.length)) {
      return 0;
    }

    final byte[] data = m_messages[m_msgcounter];
    return data.length - m_bytecounter;
  }

  public void next() {
    m_msgcounter++;
    m_bytecounter = 0;
    try {
      Thread.sleep(350);
    } catch (final InterruptedException exception) {
      CUtilityFunctions.logException(exception);
    }
  }

  @Override
  public int read() throws IOException {
    if (m_doThrow) {
      if (m_bytecounter < 8) {
        return new byte[] {0x00, 0x00, 0x00, 15, 0, 0, 0, 0}[m_bytecounter++];
      }

      throw new IOException("Test");
    }

    while (available() == 0) {
      try {
        Thread.sleep(100);
      } catch (final InterruptedException e) {
      }
    }

    final byte[] data = m_messages[m_msgcounter];
    // System.out.printf("Reading: %02X\n", (int)data[m_bytecounter]);
    return data[m_bytecounter++] & 0xFF;
  }

  @Override
  public int read(final byte[] data, final int i, final int length) {
    final byte[] mdata = m_messages[m_msgcounter];

    System.arraycopy(mdata, m_bytecounter, data, i, length);

    m_bytecounter += length;

    return length;
  }

  public void setMessages(final byte[][] messages) {
    m_msgcounter = -1;
    m_messages = messages;
  }
}
